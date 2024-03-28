package src;

import src.interfaces.RMIClientInterface;
import src.interfaces.RMIServerInterface;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {
    HashMap<String, RMIClientInterface> clientes;
    public RMIServerInterface host;
    
    public RMIServer(RMIServerInterface hPrincipal) throws RemoteException {
        super();
        
        this.host = hPrincipal;
        this.clientes = new HashMap<String, RMIClientInterface>();
        
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
    
    public void print_on_all_clients(String s) {
        for (RMIClientInterface c : clientes.values()) {
            try {
                c.atualizaAdminPage(s);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public void subscribe(RMIClientInterface c) throws RemoteException {
        System.out.print("client subscribed");
        clientes.put(c.toString(), (RMIClientInterface) new Client(c.toString(), false));
    }

    private void updateClient(String username, RMIClientInterface client) throws RemoteException {
        if (client == null) {
            this.clientes.remove(username);
        } else {
            if (this.clientes.containsKey(username)) {
                this.clientes.replace(username, client);
            } else {
                this.clientes.put(username, client);
            }
        }
    }

    public ArrayList<String> checkLogin(String username, String password) throws RemoteException {
        ArrayList<String> res = new ArrayList<>(Arrays.asList("true", "false", "Login successful"));
        System.out.println("[SERVER] Login status: " + res);

        String message = res.get(2);

        if (res.get(0).equals("failure")) {
            // login unsuccessful and not admin
            return new ArrayList<String>(Arrays.asList("false", "false", message));
        }
        String admin = res.get(1);

        RMIClientInterface c = (RMIClientInterface) new Client(username, Boolean.parseBoolean(admin));
        this.updateClient(username, c);

        // login successful and not admin
        return new ArrayList<String>(Arrays.asList("true", admin, message));
    }
}
