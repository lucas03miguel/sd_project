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
import java.util.Scanner;
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
            System.out.println("[CLIENT] Configuração: " + rmiHost + ":" + rmiPort + " " + rmiRegistryName);
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
    
    private void login(BufferedReader br) throws RemoteException {
        String username = "", pwd = "";
    
        while (username.length() < 4 || username.length() > 20 || pwd.length() < 4 || pwd.length() > 20) {
            System.out.print("Enter username (4-20 characters): ");
            try {
                username = br.readLine();
            } catch (IOException e) {
                System.out.println("Error reading username: " + e.getMessage());
                continue;
            }
    
            System.out.print("Enter password (4-20 characters): ");
            try {
                pwd = br.readLine();
            } catch (IOException e) {
                System.out.println("Error reading password: " + e.getMessage());
                continue;
            }
    
            if (username.length() < 4 || username.length() > 20) {
                System.out.println("Username must be between 4 and 20 characters.");
            }
    
            if (pwd.length() < 4 || pwd.length() > 20) {
                System.out.println("Password must be between 4 and 20 characters.");
            }
        }
    }
    
    private void printMenu(int userType) {
        System.out.println("\n----Menu----");
        
        // login or register
        if (userType == 0) {
            System.out.println("1. Login");
            System.out.println("2. Register");
        // user logged - main menu
        } else if (userType == 1) {
            System.out.println("1. Search Links");
            System.out.println("2. Index New URL");
            System.out.println("3. Barrels List");
            System.out.println("4. Downloaders List");
            System.out.println("5. Top 10 searches");
            System.out.println("6. Logout");
        }
        
        System.out.println("e. Exit");
        System.out.println("------------");
        System.out.print("Choice: ");
    }
    
    private void menu() {
        try (Scanner sc = new Scanner(System.in)) {
            int userType = 0;
            while (true) {
                printMenu(userType);
                String choice = sc.nextLine();
                
                if (userType == 0) {
                    if (choice.equals("1")) {
                        System.out.println("----Login----");
                        System.out.print("Enter username: ");
                        String username = sc.nextLine();
                        System.out.print("Enter password: ");
                        String password = sc.nextLine();
                        //userType = serverInterface.login(username, password);
                    } else if (choice.equals("2")) {
                    
                    } else if (choice.equals("e")) {
                        System.out.println("A sair...");
                        System.exit(0);
                        break;
                    } else {
                        System.out.println("Invalid choice");
                    }
                } else if (userType == 1) {
                    System.out.println("### Logged in ###");
                }
                
                
                switch (choice) {
                    case "1":
                        System.out.println("### Search Links ###");
                        System.out.print("Enter search query: ");
                        String searchQuery = sc.nextLine();
                        serverInterface.pesquisa(searchQuery);
                        break;
                    case "2":
                        System.out.println("### Index New URL ###");
                        System.out.print("Enter URL: ");
                        String url = sc.nextLine();
                        //serverInterface.indexar(url);
                        break;
                    case "3":
                        if (userType == 0) {
                            System.out.println("### Login ###");
                            System.out.print("Enter username: ");
                            String username = sc.nextLine();
                            System.out.print("Enter password: ");
                            String password = sc.nextLine();
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
                            String regUsername = sc.nextLine();
                            System.out.print("Enter password: ");
                            String regPassword = sc.nextLine();
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
                        System.out.println("Exiting...");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Exceção na main: " + e);
        }
    }
    
    public void atualizaAdminPage(String s) throws RemoteException {
        System.out.println("> " + s);
    }
    
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
