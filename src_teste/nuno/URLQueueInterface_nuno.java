package src_teste.nuno;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

public interface URLQueueInterface_nuno extends Remote {
    public void addUrl(String url) throws RemoteException;
    public String getUrl() throws RemoteException;
    public int subscribeDownloader(DownloaderInterface downloader) throws RemoteException;
    public void checkDownloader(int i) throws RemoteException;
    public int getDownloaderListSize() throws RemoteException;
    public HashSet<Integer> updateDownloaders() throws RemoteException;
    public int getNumberActiveDownloaders() throws RemoteException;
}

