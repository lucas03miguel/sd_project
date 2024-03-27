package src;

import src.interfaces.RMIClientInterface;
import src.interfaces.RMIServerInterface;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class RMIClient extends UnicastRemoteObject {
    private final int keepAliveTime = 5000;
    private final String rmiHost;
    private final int rmiPort;
    private final String rmiRegistryName;
    private final Client client;
    private static RMIServerInterface serverInterface;
    
    RMIClient(String rmiHost, int rmiPort, String rmiRegistryName, Client client, RMIServerInterface serverInterface) throws RemoteException {
        super();
        this.rmiHost = rmiHost;
        this.rmiPort = rmiPort;
        this.rmiRegistryName = rmiRegistryName;
        this.client = client;
        this.serverInterface = serverInterface;
    }
    
    /*
    public void run(){
        
        String a;
        
        try (Scanner sc = new Scanner(System.in)) {
            //User user = new User();
            //subscribe on gateway
            h.subscribe((RMIClientInterface) this);
            System.out.println("Client sent subscription to server");
            
            while (true) {
                System.out.print("> ");
                a = sc.nextLine();
                h.pesquisa(a);
            }
            
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }
     */
    
    
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        
        String rmiHost = "127.0.0.1";
        int rmiPort = 7000;
        String rmiRegistryName = "//127.0.0.1:7000/OPyThaOn";
        
        try {
            System.out.println("[CLIENT] Configuração: " + rmiHost + ":" + rmiPort + " " + rmiRegistryName);
            RMIServerInterface svInterface = (RMIServerInterface) Naming.lookup(rmiRegistryName);
            System.out.println("[CLIENT] Conectado!!!");
            
            Client client = new Client("Anon", false);
            RMIClient rmi_client = new RMIClient(rmiHost, rmiPort, rmiRegistryName, client, svInterface);
            //rmi_client.menu();
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String a = sc.nextLine();
                serverInterface.pesquisa(a);
            }
        }catch (Exception ex){
            ex.printStackTrace();
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
