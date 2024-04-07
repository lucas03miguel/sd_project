/**
 * @author Lucas e Simão
 */
package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Interface RMIServerInterface que extende a interface Remote.
 */
public interface RMIServerInterface extends Remote {
    HashMap<String, Integer> obterTopSearches() throws RemoteException;
    
    int checkLogin(String username, String password) throws RemoteException;
    String checkRegisto(String username, String password) throws RemoteException;
    String indexar(String url) throws RemoteException;
    List<String> obterListaBarrels() throws RemoteException;
    HashMap<String, ArrayList<String>> pesquisar(String s) throws RemoteException;
    HashMap<Integer, Double> obterTempos() throws RemoteException;
}