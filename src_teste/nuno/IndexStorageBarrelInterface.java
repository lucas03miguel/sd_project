package src_teste.nuno;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.ArrayList;

public interface IndexStorageBarrelInterface extends Remote{
    public void init(String name) throws RemoteException;
    public String getName() throws RemoteException;
    public ArrayList<WebPage> search(String key) throws RemoteException;
    public HashSet<String> searchLinks(String key) throws RemoteException;
    public boolean Heartbeat() throws RemoteException;
    public boolean register(String username , String password) throws RemoteException;
    public boolean login(String username , String password) throws RemoteException;
}
