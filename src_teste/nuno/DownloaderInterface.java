package src;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DownloaderInterface extends Remote{
    public void downloadWebPage(String url) throws RemoteException;
    public boolean Heartbeat() throws RemoteException;
}
