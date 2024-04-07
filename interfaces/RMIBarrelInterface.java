package interfaces;

import src.Barrel;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface RMIBarrelInterface extends Remote{
    //void guardarURLs(String[] list);
    int selecionarBarrel() throws RemoteException;
    
    HashMap<String, Integer> obterTopSearches(int id) throws RemoteException;
    
    boolean alive() throws RemoteException;
    List<String> obterListaBarrels() throws RemoteException;
    HashMap<String, ArrayList<String>> pesquisarLinks(String s, int id) throws RemoteException;
    HashMap<Integer, Integer> obterPesquisas() throws RemoteException;
    int getId() throws RemoteException;
    /*
    
    ArrayList<String> checkUserRegistration(String username, String password, String firstName, String lastName) throws RemoteException;
    ArrayList<String> verifyUser(String username, String password) throws RemoteException;
    
    HashSet<String> searchLinks(String word) throws RemoteException;
    ArrayList<String> searchTitle(String word) throws RemoteException;
    ArrayList<String> searchDescription(String word) throws RemoteException;
    HashSet<String> linkpointers(String link) throws RemoteException;
    
    boolean isAdmin(String username) throws RemoteException;
    
    ArrayList<ArrayList<String>> getBarrelsAlive() throws RemoteException;
    
    ArrayList<String> saveWordSearches(String phrase) throws RemoteException;
    
    HashMap<String, Integer> getTop10Searches() throws RemoteException;
    */
    //IndexStorageBarrels selectBarrelToExcute();
}
