package src;

import src.interfaces.RMIClientInterface;
import src.interfaces.RMIServerInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class RMIClient extends UnicastRemoteObject implements RMIClientInterface {
    
    private RMIServerInterface h;
    
    RMIClient() throws RemoteException {
        super();
        
        try {
            h = (RMIServerInterface) Naming.lookup("XPTO");
        } catch (NotBoundException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    
        run();
        
    }
    
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
    
    public void atualizaAdminPage(String s) throws RemoteException {
        System.out.println("> " + s);
    }
    
    public static void main(String[] args) {
		System.getProperties().put("java.security.policy", "policy.all");
        
        try {
            RMIClient c = new RMIClient();
            
        }catch (Exception ex){
            ex.printStackTrace();
        }
        
        
        
    }

}
