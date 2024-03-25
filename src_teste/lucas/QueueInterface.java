package src_teste.lucas;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QueueInterface extends Remote{
    public String getLink() throws RemoteException;
    public void putLink(String s) throws RemoteException;
    
}
