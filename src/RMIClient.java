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
    private Client client;
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
    
    /*
    private void register(BufferedReader br) throws RemoteException {
        String username = "", password = "";
        while (true) {
            // get username and password
            try {
                System.out.print("\n### REGISTER ###\n  Username: ");
                username = br.readLine();
                while (username.length() < 4 || username.length() > 20 || username.equals("Anon")) {
                    System.out.print("[CLIENT] Username must be between 4 and 20 characters and it can't be Anon\n  Username: ");
                    username = br.readLine();
                }
    
                System.out.print("  Password: ");
                password = br.readLine();
                while (password.length() < 4 || password.length() > 20) {
                    System.out.print("[CLIENT] Password must be between 4 and 20 characters\n  Password: ");
                    password = br.readLine();
                }
    
            } catch (IOException e) {
                System.out.println("[EXCEPTION] IOException");
                e.printStackTrace();
            }
    
            // System.out.println("[CLIENT] Registering: " + username + " " + password);
    
            ArrayList<String> res = this.serverInterface.checkRegister(username, password);
    
            if (res.get(0).equals("true")) {
                // register success
                System.out.println("\n[CLIENT] Registration success!");
    
                // admin or not
                this.client = new Client(username, res.get(1).equals("true"));
    
                System.out.println("[CLIENT] Logged in as " + this.client.username);
                return;
            } else {
                System.out.println("[ERROR] Registration failed: " + res.get(2));
                System.out.print("[CLIENT] Try again? (y/n): ");
                try {
                    String choice = br.readLine();
                    while (!choice.equals("y") && !choice.equals("n")) {
                        System.out.println("[CLIENT] Invalid choice");
                        System.out.print("[CLIENT] Try again? (y/n): ");
                        choice = br.readLine();
                    }
                    if (choice.equals("n")) {
                        return;
                    }
                } catch (IOException e) {
                    System.out.println("[EXCEPTION] IOException");
                    e.printStackTrace();
                }
            }
        }
    }
    
     */
    
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
                        String username = "", password = "";
                        int resp = login(username, password, br);
                        if (resp == -1) {
                            System.out.println("A sair...");
                            System.exit(0);
                            break;
                        } else if (resp == 1) userType = 1;
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
                    System.out.println("----Logged in----");
                    
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
            System.out.println("adeus");
        }
    }
    
    private int login(String username, String password, BufferedReader br) throws IOException {
        System.out.println("\n----Login----");
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Username: ");
                username = br.readLine();
    
                while (username.length() < 4 || username.length() > 20) {
                    System.out.print("Username errado (3-20 carateres): ");
                    username = br.readLine();
                }
            } catch (Exception e) {
                System.out.println("[EXCEPTION] Erro ao ler o username: " + e);
            }
            
            try {
                System.out.print("Password: ");
                password = br.readLine();
    
                while (password.length() < 1 || password.length() > 20) {
                    System.out.print("Password errada (1-20 carateres): ");
                    password = br.readLine();
                }
            } catch (Exception e) {
                System.out.println("[EXCEPTION] Erro ao ler a password: " + e);
            }
            
            int checked = this.serverInterface.checkLogin(username, password);
            if (checked == -1) {
                System.out.println("[CLIENT] Login falhou: erro no servidor");
                //br.close();
                return -1;
            }
            
            if (checked == 1) {
                System.out.println("[CLIENT] Login bem sucedido!");
                //br.close();
                return 1;
            } else {
                System.out.println("[CLIENT] Login falhou: usuario ou senha errados");
                
                String choice = "";
                do {
                    System.out.print("[CLIENT] Tentar novamente (s/n)? ");
                    try {
                        choice = br.readLine();
                    } catch (Exception e) {
                        System.out.println("[EXCEPTION] Erro: " + e);
                    }
                } while (!choice.equalsIgnoreCase("s") && !choice.equalsIgnoreCase("n"));
                if (choice.equalsIgnoreCase("n")) {
                    //br.close();
                    return 0;
                }
            }
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
