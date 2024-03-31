package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface URLQueueInterface extends Remote {
    List<String> getUrlQueue() throws RemoteException;
    String inserirLink(String link) throws RemoteException;
    boolean isEmpty() throws RemoteException;
    String removerLink() throws RemoteException;
    
    int size() throws RemoteException;
}