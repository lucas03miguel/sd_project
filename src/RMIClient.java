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
    private final Client client;
    private RMIServerInterface serverInterface;
    
    public RMIClient(String rmiHost, int rmiPort, String rmiRegistryName, Client client, RMIServerInterface serverInterface) throws RemoteException {
        super();
        this.rmiHost = rmiHost;
        this.rmiPort = rmiPort;
        this.rmiRegistryName = rmiRegistryName;
        this.client = client;
        this.serverInterface = serverInterface;
        
        try {
            System.out.println("[CLIENT] Configuração: " + rmiHost + ":" + rmiPort + " " + rmiRegistryName);
            System.out.println("[CLIENT] Conectado!!!");
            
            run();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        
        run();
        
    }
    
    
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        
        try {
            String rmiHost = "127.0.0.1";
            int rmiPort = 7000;
            String rmiRegistryName = "OPyThaOn";
            Client client = new Client("Antonio", false);
            //RMIServerInterface svInterface = (RMIServerInterface) Naming.lookup(rmiRegistryName);
            RMIServerInterface svInterface = (RMIServerInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegistryName);
            new RMIClient(rmiHost, rmiPort, rmiRegistryName, client, svInterface);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

    private void printMenus(int userType) {
        // login or register
        if (userType == 0) {
            System.out.println("\n### Login Menu ###\n1.Search Links\n2.Index New URL\n  3.Login\n  4.Register\n  e.Exit\n --> Choice: ");
            return;
        }
        // admin - main menu
        else if (userType == 1) {
            System.out.print("\n### Admin Panel ###\n1.Search Links\n2.Index New URL\n3.Barrels List\n4.Downloaders List\n5.Top 10 searches\n  6.Logout\n  e.Exit\n --> Choice: ");
            return;
        }

        // user - main menu
        else if (userType == 2) {
            System.out.print("\n### User Panel ###\n1.Search Links\n2.Index New URL\n  3.Logout\n  e.Exit\n --> Choice: ");
            return;
        }

        else {
            System.out.println("Invalid user type");
        }

    }

    private void menu() {
        try (Scanner sc = new Scanner(System.in)) {
            int userType = 0;
            while (true) {
                printMenus(userType);
                String choice = sc.nextLine();
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
                        System.out.println("### Login ###");
                        System.out.print("Enter username: ");
                        String username = sc.nextLine();
                        System.out.print("Enter password: ");
                        String password = sc.nextLine();
                        //userType = serverInterface.login(username, password);
                        break;
                    case "4":
                        System.out.println("### Register ###");
                        System.out.print("Enter username: ");
                        String regUsername = sc.nextLine();
                        System.out.print("Enter password: ");
                        String regPassword = sc.nextLine();
                        //userType = serverInterface.register(regUsername, regPassword);
                        break;
                    case "5":
                        System.out.println("### Barrels List ###");
                        //serverInterface.barrelsList();
                        break;
                    case "6":
                        System.out.println("### Downloaders List ###");
                        //serverInterface.downloadersList();
                        break;
                    case "7":
                        System.out.println("### Top 10 searches ###");
                        //serverInterface.top10();
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

    
    public void run(){
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String a = sc.nextLine();
                serverInterface.pesquisa(a);
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
                //this.menu();
                break;
            } catch (RemoteException | NotBoundException e1) {
                System.out.println("[EXCEPTION] Nao conseguiu conectar ao server: " + e1.getMessage());
            }
        }
    }
}
