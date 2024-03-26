package src.interfaces;

import java.rmi.*;

public interface RMIClientInterface extends Remote{
    void atualizaAdminPage(String s) throws java.rmi.RemoteException;
}