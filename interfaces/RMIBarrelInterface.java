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
 * Interface RMIBarrelInterface que extende a interface Remote.
 */
public interface RMIBarrelInterface extends Remote{
    int selecionarBarrel() throws RemoteException;
    
    HashMap<String, Integer> obterTopSearches(int id) throws RemoteException;
    
    HashMap<Integer, Integer> getNPesquisas() throws RemoteException;
    boolean alive() throws RemoteException;
    List<String> obterListaBarrels() throws RemoteException;
    HashMap<String, ArrayList<String>> pesquisarLinks(String s, int id) throws RemoteException;
    int getId() throws RemoteException;
    ArrayList<String> obterLigacoes(int id, String link) throws RemoteException;
}
