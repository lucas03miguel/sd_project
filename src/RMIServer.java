package src;

import src.interfaces.RMIClientInterface;
import src.interfaces.RMIServerInterface;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {
    HashMap<String, Client> clientes;
    public RMIServerInterface host;
    
    public RMIServer(RMIServerInterface hPrincipal) throws RemoteException {
        super();
        this.host = hPrincipal;
        this.clientes = new HashMap<>();
        
        int rmiPort = 7000;
        String rmiHost = "192.168.1.100";
        String rmiRegistryName = "OPyThaOn";
        while (true) {
            try {
                Registry r = LocateRegistry.createRegistry(rmiPort);
                System.setProperty("java.rmi.server.hostname", rmiHost);
                r.rebind(rmiRegistryName, this);
                System.out.println("[SERVER] A correr em " + rmiHost + ":" + rmiPort + "->" + rmiRegistryName);
                
                //meter cena dos barrels e tals
                
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
        try {
            System.out.println("[SERVER] Server preparado.");
            while (true);
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
        //print_on_all_clients(s);
    }
    
    /*
    public void print_on_all_clients(String s) {
        //for (RMIClientInterface c : clientes.values()) {
        try {
            for (String key : clientes.keySet()) {
                clientes.get(key).print_on_client(s);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        //}
    }
    
    public void subscribe(RMIClientInterface c) throws RemoteException {
        System.out.print("client subscribed");
        clientes.put(c.toString(), new Client(c.toString(), false));
    }

    private void updateClient(String username, Client client) throws RemoteException {
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
    */

    public int checkLogin(String username, String password) throws RemoteException {
        //TODO: Modificar esta shit porque nao esta bem. temos que usar o fucking barril
        /*
        ArrayList<String> res = new ArrayList<>(Arrays.asList("true", "false", "Login successful"));
        //-------------------------------------------------
        System.out.println("[SERVER] Login status: " + res);

        String message = res.get(2);

        if (res.get(0).equals("failure")) {
            // login unsuccessful and not admin
            return new ArrayList<>(Arrays.asList("false", "false", message));
        }
        String pass = res.get(1);
        
        Client c = new Client(username, pass);
        this.updateClient(username, c);

        // login successful and not admin
        return new ArrayList<>(Arrays.asList("true", pass, message));
        */
        int validLogins = 0;
        String filePath = "./files/users.txt";
        
        Path dirPath = Path.of("./files");
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
                System.out.println("Diretório de users criado: " + dirPath);
            } catch (Exception e) {
                System.err.println("Erro ao criar o diretório de users: " + e);
                return -1;
            }
        }
        
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                System.out.println("Ficheiro de users criado: " + filePath);
            } catch (Exception e) {
                System.err.println("Erro ao criar o ficheiro de users: " + e);
                return -1;
            }
        }
    
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    validLogins = 1;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler do ficheiro de users: " + e);
            return -1;
        }
        
        return validLogins;
    }

    
    public String checkRegisto(String username, String password) throws RemoteException {
    String filePath = "./files/users.txt";

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
        bw.write(username + " " + password + "\n");
        System.out.println("O user adicionado foi: " + username + " " + password);
        bw.flush();
        System.out.println("User adicionado com sucesso. hadshasdhahsdhasdhasdhadhsahdsh");
    } catch (Exception e) {
        System.err.println("Erro ao escrever no ficheiro de users: " + e);
        return "Erro no lado do servidor";
    }

    return "User adicionado com sucesso";
}
    

}
