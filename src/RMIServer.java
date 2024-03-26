package src;

import src.interfaces.RMIServerInterface;
import src.interfaces.RMIClientInterface;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {
    public ArrayList<RMIClientInterface> clientes;
    
    public RMIServer() throws RemoteException {
        super();
        clientes = new ArrayList<>();
        
        LocateRegistry.createRegistry(1099);
        
        try {
            Naming.rebind("XPTO", (RMIServerInterface)this);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        run();
    }
    
    public void run(){
        
        String a;
        try (Scanner sc = new Scanner(System.in)) {
            //User user = new User();
            System.out.println("[RMI SERVER] Server ready");
            while (true) {
                System.out.print("> ");
                a = sc.nextLine();
                this.print_on_all_clients(a);
                //client.print_on_client(a);
            }
        } catch (Exception re) {
            System.out.println("Exception in RMIServer.main: " + re);
        }
    }
    
    public void pesquisa(String s) throws RemoteException {
        System.out.println("> " + s);
        print_on_all_clients(s);
    }
    
    public void print_on_all_clients(String s){
        for (RMIClientInterface c:clientes) {
            try {
                c.atualizaAdminPage(s);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public void subscribe(RMIClientInterface c) throws RemoteException {
        //System.out.println("Subscribing " + name);
        System.out.print("client subscribed");
        //client = c;
        clientes.add(c);
    }
    
    // =======================================================
    
    public static void main(String args[]) {
        //String a;

		/*
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new RMISecurityManager());
		*/
        try {
            RMIServer h = new RMIServer();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        
    }
}
