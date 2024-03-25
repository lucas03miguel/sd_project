package src_teste.lucas;

import java.io.IOException;
import java.rmi.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashSet;

/**
 * @author Daniela e Lucas
 * @version 1.0
 * @since 2016-05-01
 * @see SearchModule- Classe que implementa o Search Module e comunica com os Storage Barrels usando RMI,
 * este RMI Search Module não tem de armazenar quaisquer dados,
 * dependendo inteiramente dos Storage Barrels para satisfazer os pedidos dos clientes.
 *
 */
public interface SearchModule extends Remote {

    public void searchModule() throws RemoteException;

    public String readUserInfo(String username, String password) throws RemoteException;

    public String writeUserInfo(String username, String password) throws RemoteException;

    public HashSet<String> searchWord(String word) throws java.rmi.RemoteException;

    public HashSet<String> searchLink(String link) throws java.rmi.RemoteException;

    public HashSet<String> searchInfo(String link) throws java.rmi.RemoteException;

    public void indexLink(String link) throws java.rmi.RemoteException;

    

    


}
