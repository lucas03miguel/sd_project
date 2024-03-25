package src;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface URLQueueInterface extends Remote {
    String takeLink() throws RemoteException;
    void offerLink(String link) throws RemoteException;
    boolean isempty() throws RemoteException;
}