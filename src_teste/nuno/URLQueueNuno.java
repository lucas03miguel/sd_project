package src_teste.nuno;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * URLQueueNuno Main Class
 */
public class URLQueueNuno extends UnicastRemoteObject implements URLQueueInterface_nuno {
    
    private Queue<String> queue;
    private HashSet<String> urls;
    private ArrayList<DownloaderWrapper> downloaderList;
    private int downloaderCount;
    
    /**
     * URLQueueNuno constructor
     * @throws RemoteException
     */
    public URLQueueNuno() throws RemoteException {
        super();
        this.queue = new LinkedList<String>();
        this.urls = new HashSet<>();
        this.downloaderList = new ArrayList<DownloaderWrapper>();
        this.downloaderCount = 0;
    }
    
    /**
     * Adds a url to end of queue.
     * recursevely and randomly triggers a downloader to download the information of the url on the head of queue.
     * @param url
     *
     */
    public void addUrl(String url) throws RemoteException {
        if(!urls.contains(url)){
            this.urls.add(url);
            this.queue.add(url);
            if(downloaderList.size() > 0){
                Random rn = new Random();
                downloaderList.get(rn.nextInt(0,this.downloaderCount)).getDownloader().downloadWebPage(this.getUrl());
            }
        }
    }
    
    /**
     * @return Head of queue
     */
    public String getUrl() throws RemoteException {
        return this.queue.remove();
    }
    
    /**
     * Add downloader to URLQueueNuno.downloaderList.
     * @param downloader downloader to add
     * @return downloader number
     */
    public int subscribeDownloader(DownloaderInterface downloader) throws RemoteException {
        this.downloaderList.add(new DownloaderWrapper(downloader));
        System.out.println("[URLQueueNuno] Downloader " +  downloaderCount + " subscribed !");
        this.downloaderCount++;
        return this.downloaderCount-1;
    }
    
    /**
     * Checks the Downloader.Heartbeat() on URLQueueNuno.downloaderList index i.
     * @param i index
     */
    public void checkDownloader(int i) throws RemoteException {
        try {
            this.downloaderList.get(i).setState(this.downloaderList.get(i).getDownloader().Heartbeat());
            //System.out.println("Downloader " + i + " is ok");
        }
        catch (RemoteException e) {
            this.downloaderList.get(i).setState(false);
        }
    }
    
    /**
     * @return URLQueueNuno.downloaderList size.
     */
    public int getDownloaderListSize() throws RemoteException {
        return this.downloaderList.size();
    }
    
    /**
     * @return Number of ACTIVE downloaders on URLQueueNuno.downloaderList
     */
    public int getNumberActiveDownloaders() throws RemoteException {
        int sum = 0;
        for(DownloaderWrapper d: this.downloaderList){
            if(d.getState() == true)
                sum++;
        }
        return sum;
    }
    
    /**
     * @return HashSet of the numbers of ACTIVE downloaders.
     */
    public HashSet<Integer> updateDownloaders() throws RemoteException {
        HashSet<Integer> r = new HashSet<>();
        for(int i = 0; i < this.downloaderList.size(); i++){
            if(downloaderList.get(i).getState() == true){
                r.add(i);
            }
        }
        return r;
    }
    
    
    /**
     * @param args
     * @throws RemoteException
     */
    public static void main(String[] args) throws RemoteException {
        try{
            System.out.println("[URLQueueNuno] Turning on... ");
            URLQueueInterface_nuno qi = new URLQueueNuno();
            Registry r = LocateRegistry.createRegistry(9871);
            r.rebind("URLQueueNuno", qi);
            System.out.println("[URLQueueNuno] RMI Server ready !");
            
            while(true){
                for(int i = 0; i < qi.getDownloaderListSize(); i++){
                    qi.checkDownloader(i);
                }
                
                try { Thread.sleep((long) (Math.random() * 3000)); } catch (InterruptedException e) { }
            }
            
        } catch(RemoteException re) {
            System.out.println("[URLQueueNuno] Remote Exception in main: " + re);
        } catch (Exception e) {
            System.out.println("[URLQueueNuno] Exception in main: " + e);
        }
    }
}

/**
 * Downloader secondary class to track if the Downloader is active.
 */
class DownloaderWrapper {
    
    private DownloaderInterface downloader;
    private boolean is_active;
    
    public DownloaderWrapper(DownloaderInterface downloader){
        this.downloader = downloader;
        this.is_active = true;
    }
    
    public DownloaderInterface getDownloader(){return this.downloader;}
    public boolean getState(){return this.is_active;}
    
    public void setState(boolean s){
        this.is_active = s;
    }
}
