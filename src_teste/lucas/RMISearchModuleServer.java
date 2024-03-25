package src_teste.lucas;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashSet;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author Daniela e Lucas
 * @version 1.0
 * @since 2016-05-01
 * @see RMISearchModuleServer- Classe que implementa o Search Module e comunica
 *      com os Barrels usando RMI, este RMI Search Module não tem de
 *      armazenar quaisquer dados, dependendo inteiramente dos Storage Barrels para satisfazer os pedidos
 *      dos clientes.
 *
 */
public class RMISearchModuleServer extends UnicastRemoteObject implements SearchModule, IBarrels, QueueInterface{
    IBarrels Ibarrel;
    QueueInterface queue;
    /**
     * Construtor da classe RMISearchModuleServer
     * 
     * @throws RemoteException
     */
    public RMISearchModuleServer() throws RemoteException {
        super();
        initializeIBarrels();
    }

    /**
     * Método main que inicia o servidor RMI do Search Module
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            // Cria um novo RMI Search Module
            RMISearchModuleServer module = new RMISearchModuleServer();

            Registry r = LocateRegistry.createRegistry(7001);

            r.rebind("rmi_first", module);

            System.out.println("RMI Search Module Server is ready.");

        } catch (Exception e) {
            // Imprime uma mensagem de erro
            System.out.println("RMI Search Module Server failed: " + e);
        }

    }

    
    public void searchModule() throws RemoteException {}

    private void initializeIBarrels() throws RemoteException{
        try{
            this.Ibarrel = (IBarrels) LocateRegistry.getRegistry(7000).lookup("rmi_barrel");
            this.queue = (QueueInterface) LocateRegistry.getRegistry(6969).lookup("Queue");
        }
        catch(NotBoundException e){
            System.out.println("Not able to connect by RMI");
        }
    }

    public String readUserInfo(String username, String password) throws RemoteException{
        return this.Ibarrel.readUserInfo(username, password);
    }

    
    public String writeUserInfo(String username, String password) throws RemoteException{
        return this.Ibarrel.writeUserInfo(username, password);
    }


    public HashSet<String> searchLink(String link) throws RemoteException {
        return this.Ibarrel.searchLink(link);
    }

    
    public HashSet<String> searchWord(String word) throws RemoteException {
        return this.Ibarrel.searchWord(word);
    }

    
    public HashSet<String> searchInfo(String link) throws RemoteException {
        return this.Ibarrel.searchInfo(link);
    }

    public void indexLink(String s) throws RemoteException {
        this.queue.putLink(s);
    }

    public String getLink() throws RemoteException{
        return this.queue.getLink();
    }

    @Override
    public void putLink(String s) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'putLink'");
    }

    
 

}
