package src;

import src.interfaces.RMIClientInterface;
import src.interfaces.RMIServerInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RMIClient extends UnicastRemoteObject {
    private final int keepAliveTime = 5000;
    private final String rmiHost;
    private final int rmiPort;
    private final String rmiRegistryName;
    //private final Client client;
    private RMIServerInterface serverInterface;
    
    public RMIClient(String rmiHost, int rmiPort, String rmiRegistryName, RMIServerInterface serverInterface) throws RemoteException {
        super();
        this.rmiHost = rmiHost;
        this.rmiPort = rmiPort;
        this.rmiRegistryName = rmiRegistryName;
        
        //this.client = client;
        this.serverInterface = serverInterface;
        
        try {
            System.out.println("[CLIENT] Configuração: " + rmiHost + ":" + rmiPort + "->" + rmiRegistryName);
            System.out.println("[CLIENT] Conectado!!!");
            
            run();
        }catch (Exception e){
            System.out.println("[CLIENT] Erro ao conectar ao servidor: " + e);
        }
        //run();
        
    }
    
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        
        try {
            String rmiHost = "127.0.0.1";
            int rmiPort = 7000;
            String rmiRegistryName = "OPyThaOn";
            //Client client = new Client("Antonio", false);
            //RMIServerInterface svInterface = (RMIServerInterface) Naming.lookup(rmiRegistryName);
            RMIServerInterface svInterface = (RMIServerInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegistryName);
            new RMIClient(rmiHost, rmiPort, rmiRegistryName, svInterface);
        } catch (Exception e) {
            System.out.println("[CLIENT] Erro ao conectar ao servidor: " + e);
        }
        System.out.println("oi");
        
    }
    
    public void run(){
        try {
            this.menu();
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Exceção na main: " + e);
        }
    }
    
    private void login(String username, String password) throws RemoteException {
        while (true) {
            while (username.length() < 4 || username.length() > 20) {
                System.out.print("Username errado (4-20 carateres): ");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                    username = br.readLine();
                } catch (Exception e) {
                    System.out.println("Erro a ler o username: " + e);
                }
            }
    
            while (password.length() < 1 || password.length() > 20) {
                System.out.print("Password errrada (1-20 carateres): ");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                    password = br.readLine();
                } catch (Exception e) {
                    System.out.println("Erro a ler a password: " + e);
                }
            }
    
            ArrayList<String> checked = this.serverInterface.checkLogin(username, password);
            if (checked == null) {
                System.out.println("[CLIENT] Login falhou: servidor em baixo");
                return;
            }
            
            if (checked.get(0).equals("true")) {
                boolean admin = checked.get(1).equals("true");
                this.client = new Client(username, admin);
        
                if (admin) {
                    System.out.println("[CLIENT] Login successful as admin");
                } else {
                    System.out.println("[CLIENT] Login successful");
                }
        
                return;
            } else {
                System.out.println("[CLIENT] Login failed: " + checked.get(2));
                String choice = "";
                while (!choice.equals("y") && !choice.equals("n")) {
                    System.out.print("[CLIENT] Try again? (y/n): ");
                    try {
                        choice = br.readLine();
                    } catch (IOException e) {
                        System.out.println("[EXCEPTION] IOException");
                        e.printStackTrace();
                    }
                }
                if (choice.equals("n")) {
                    return;
                }
            }
        }
    }
    
    private void printMenu(int userType) {
        System.out.println("\n----Menu----");
        
        // login or register
        if (userType == 0) {
            System.out.println("1. Login");
            System.out.println("2. Registar");
        // user logged - main menu
        } else if (userType == 1) {
            System.out.println("1. Pesquisar");
            System.out.println("2. Indexar novo URL");
            System.out.println("3. Lista dos barrels");
            System.out.println("4. Lista dos downloaders");
            System.out.println("5. Top 10 searches");
            System.out.println("6. Logout");
        }
        
        System.out.println("e. Sair");
        System.out.println("------------");
        System.out.print("Opção: ");
    }
    
    private void menu() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            int userType = 0;
            while (true) {
                if (userType == 0) {
                    printMenu(userType);
                    String choice = br.readLine();
                    if (choice.equals("1")) {
                        System.out.println("----Login----");
                        System.out.print("Username: ");
                        String username = br.readLine();
                        System.out.print("Password: ");
                        String password = br.readLine();
                        login(username, password);
                        userType = 1;
                    } else if (choice.equals("2")) {
                        System.out.println("----Registar----");
                        System.out.print("Username: ");
                        String username = br.readLine();
                        System.out.print("Password: ");
                        String password = br.readLine();
                        //userType = serverInterface.register(username, password);
                        userType = 1;
                    } else if (choice.equalsIgnoreCase("s")) {
                        System.out.println("A sair...");
                        System.exit(0);
                        break;
                    } else {
                        System.out.println("Escolha inválida!");
                    }
                }
                
                if (userType == 1) {
                    printMenu(userType);
                    String choice = br.readLine();
                    //System.out.println("----Logged in----");
                    
                    switch (choice) {
                        case "1":
                            System.out.println("<----Pesquisar---->");
                            System.out.print("Insira pesquisa: ");
                            String searchQuery = br.readLine();
                            serverInterface.pesquisa(searchQuery);
                            break;
                        case "2":
                            System.out.println("<----Indexar novo URL---->");
                            System.out.print("Enter URL: ");
                            String url = br.readLine();
                            //serverInterface.indexar(url);
                            break;
                        case "3":
                            if (userType == 0) {
                                System.out.println("### Login ###");
                                System.out.print("Enter username: ");
                                String username = br.readLine();
                                System.out.print("Enter password: ");
                                String password = br.readLine();
                                //userType = serverInterface.login(username, password);
                            } else {
                                System.out.println("### Logout ###");
                                userType = 0;
                            }
                            break;
                        case "4":
                            if (userType == 0) {
                                System.out.println("### Register ###");
                                System.out.print("Enter username: ");
                                String regUsername = br.readLine();
                                System.out.print("Enter password: ");
                                String regPassword = br.readLine();
                                //userType = serverInterface.register(regUsername, regPassword);
                            } else if (userType == 1) {
                                System.out.println("### Downloaders List ###");
                                //serverInterface.downloadersList();
                            }
                            break;
                        case "5":
                            if (userType == 1) {
                                System.out.println("### Top 10 searches ###");
                                //serverInterface.top10();
                            }
                            break;
                        case "6":
                            if (userType == 1) {
                                System.out.println("### Logout ###");
                                userType = 0;
                            }
                            break;
                        case "e":
                            System.out.println("A sair...");
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Invalid choice");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Exceção na main: " + e);
        }
    }
    
    /*
    
    public void printar(String s) throws RemoteException {
        System.out.println("> " + s);
    }
    */
    
    private void serverErrorHandling() {
        System.out.println("[EXCEPTION] Não conseguiu conectar ao server.");
        while (true) {
            try {
                System.out.println("[CLIENT] Tentando reconectar...");
                //Thread.sleep(keepAliveTime);
                serverInterface = (RMIServerInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegistryName);
                
                System.out.println("[CLIENT] Reconectado!");
                this.menu();
                break;
            } catch (Exception e) {
                System.out.println("[EXCEPTION] Nao conseguiu conectar ao server: " + e);
            }
        }
    }
    
}
