package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface RMIServerInterface extends Remote {
    int checkLogin(String username, String password) throws RemoteException;
    String checkRegisto(String username, String password) throws RemoteException;
    String indexar(String url) throws RemoteException;
    List<String> obterListaBarrels() throws RemoteException;
    HashMap<String, Integer> getTopSearches() throws RemoteException;
    List<String> getDownloadersList() throws RemoteException;
    
    HashMap<String, ArrayList<String>> pesquisar(String s) throws RemoteException;
    boolean alive() throws RemoteException;
    
    /*
    boolean alive() throws RemoteException;
    
    boolean indexNewUrl(String url) throws RemoteException;
    
    boolean isLoggedIn(String username) throws RemoteException;
    boolean logout(String username) throws RemoteException;
    boolean isAdmin(String username) throws RemoteException;
    
    ArrayList<String> getTop10Searches() throws RemoteException;
    
    HashMap<String, ArrayList<String>> searchLinks(String phrase, int page) throws RemoteException;
    ArrayList<String> linkPointers(String link) throws RemoteException;
    
    ArrayList<String> getLinksByRelevance(String link) throws RemoteException;
    
    ArrayList<String> getAliveBarrels() throws RemoteException;
    ArrayList<String> getAliveCrawlers() throws RemoteException;
    
    void subscribe(RMIClientInterface rmiClientInterface) throws RemoteException;
    //boolean ping() throws RemoteException;
    void pesquisa(String a) throws RemoteException;
    */
    
}