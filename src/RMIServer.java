package src;

import src.interfaces.RMIClientInterface;
import src.interfaces.RMIServerInterface;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {
    public ArrayList<RMIClientInterface> clientes;
    public RMIServerInterface host;
    
    public RMIServer(RMIServerInterface hPrincipal/*, String bRMIregistry, String bRMIhost, int bRMIport, String uRMIregistry, String uRMIhost, int uRMIport*/) throws RemoteException {
        super();
        
        this.host = hPrincipal;
        this.clientes = new ArrayList<>();
        /*
        this.bRMIregistry = bRMIregistry;
        this.bRMIhost = bRMIhost;
        this.bRMIport = bRMIport;
        
        this.uRMIregistry = uRMIregistry;
        this.uRMIhost = uRMIhost;
        this.uRMIport = uRMIport;
         */
        
        int rmiPort = 7000;
        String rmiHost = "192.168.1.100";
        String rmiRegistryName = "OPyThaOn";
        while (true) {
            try {
                Registry r = LocateRegistry.createRegistry(rmiPort);
                System.setProperty("java.rmi.server.hostname", rmiHost);
                r.rebind(rmiRegistryName, this);
                System.out.println("[SERVER] Running on " + rmiHost + ":" + rmiPort + "->" + rmiRegistryName);
                
                run();
            } catch (Exception e) {
                System.out.println("[EXCEPTION] Nao conseguiu criar registry. A tentar novamente num segundo...");
                try {
                    sleep(1000);
                    this.host = (RMIServerInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegistryName);
                    //his.backUpCreate(rmiPort, rmiHost, rmiRegistryName); //barrels
                } catch (InterruptedException | NotBoundException e2) {
                    System.out.println("[EXCEPTION] " + e2);
                    return;
                }
            }
        }
    }
    
    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("[SERVER] Server preparado.");
            while (true) {
                String a = sc.nextLine();
                this.print_on_all_clients(a);
            }
        } catch (Exception re) {
            System.out.println("Exception in RMIServer.main: " + re);
        }
    }
    
    public static void main(String[] args) {
		System.getProperties().put("java.security.policy", "policy.all");
        
        try {
            new RMIServer(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void pesquisa(String s) throws RemoteException {
        System.out.println("> " + s);
        print_on_all_clients(s);
    }
    
    public void print_on_all_clients(String s){
        for (RMIClientInterface c: clientes) {
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
    
    /*
    @Override
    public boolean ping() throws RemoteException {
        return true;
    }
    */
}