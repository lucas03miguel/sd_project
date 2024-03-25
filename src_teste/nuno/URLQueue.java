package src;

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
 * URLQueue Main Class
 */
public class URLQueue extends UnicastRemoteObject implements URLQueueInterface {
    
    private Queue<String> queue;
    private HashSet<String> urls;
    private ArrayList<DownloaderWrapper> downloaderList;
    private int downloaderCount;
    
    /**
     * URLQueue constructor
     * @throws RemoteException
     */
    public URLQueue() throws RemoteException {
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
     * Add downloader to URLQueue.downloaderList.
     * @param downloader downloader to add
     * @return downloader number
     */
    public int subscribeDownloader(DownloaderInterface downloader) throws RemoteException {
        this.downloaderList.add(new DownloaderWrapper(downloader));
        System.out.println("[URLQueue] Downloader " +  downloaderCount + " subscribed !");
        this.downloaderCount++;
        return this.downloaderCount-1;
    }
    
    /**
     * Checks the Downloader.Heartbeat() on URLQueue.downloaderList index i.
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
     * @return URLQueue.downloaderList size.
     */
    public int getDownloaderListSize() throws RemoteException {
        return this.downloaderList.size();
    }
    
    /**
     * @return Number of ACTIVE downloaders on URLQueue.downloaderList
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
            System.out.println("[URLQueue] Turning on... ");
            URLQueueInterface qi = new URLQueue();
            Registry r = LocateRegistry.createRegistry(9871);
            r.rebind("URLQueue", qi);
            System.out.println("[URLQueue] RMI Server ready !");
            
            while(true){
                for(int i = 0; i < qi.getDownloaderListSize(); i++){
                    qi.checkDownloader(i);
                }
                
                try { Thread.sleep((long) (Math.random() * 3000)); } catch (InterruptedException e) { }
            }
            
        } catch(RemoteException re) {
            System.out.println("[URLQueue] Remote Exception in main: " + re);
        } catch (Exception e) {
            System.out.println("[URLQueue] Exception in main: " + e);
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
