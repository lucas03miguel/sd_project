package src;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.ArrayList;

public interface SearchModuleInterface extends Remote{
    public ArrayList<WebPage> search(String key, int retry) throws RemoteException;
    public HashSet<String> searchUrl(String key, int retry) throws RemoteException;
    public void index_new_url(String url) throws RemoteException;
    public int subscribeIsb(IndexStorageBarrelInterface isb, String isbname) throws RemoteException;
    public ArrayList<String> getTopSearchs() throws RemoteException;
    public ArrayList<String> getISBs() throws RemoteException;
    public ArrayList<String> getDownloaders() throws RemoteException;
    public boolean register(String username , String password, int retry) throws RemoteException;
    public boolean login(String username , String password, int retry) throws RemoteException;
}
