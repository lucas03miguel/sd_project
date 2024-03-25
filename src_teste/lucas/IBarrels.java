package src_teste.lucas;

import java.rmi.*;
import java.util.*;


public interface IBarrels extends Remote {
    
    public HashSet<String> searchWord(String word) throws RemoteException;
    public HashSet<String> searchLink(String link) throws RemoteException;
    public HashSet<String> searchInfo(String info) throws RemoteException;

    public String writeUserInfo(String username, String password) throws RemoteException;
    public String readUserInfo(String username, String password) throws RemoteException;



}
