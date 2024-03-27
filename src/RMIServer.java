package src;

import src.interfaces.RMIClientInterface;
import src.interfaces.RMIServerInterface;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.Scanner;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {
    //static Hello_C_I client;
    
    //private IClient clientCallback;
    
    public ArrayList<RMIClientInterface> clientes;
    
    public RMIServer() throws RemoteException {
        super();
        clientes = new ArrayList<>();
        
        LocateRegistry.createRegistry(1099);
        
        try {
            Naming.rebind("OPyThaOn", (RMIServerInterface) this);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        run();
    }
    
    public void run(){
        
        String a;
        try (Scanner sc = new Scanner(System.in)) {
            //User user = new User();
            System.out.println("Hello Server ready.");
            while (true) {
                System.out.print("> ");
                a = sc.nextLine();
                this.print_on_all_clients(a);
                //client.print_on_client(a);
            }
        } catch (Exception re) {
            System.out.println("Exception in HelloImpl.main: " + re);
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
