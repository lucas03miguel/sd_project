package src_teste.nuno;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Search Module Main Class
 */
public class SearchModule extends UnicastRemoteObject implements SearchModuleInterface {
    
    private URLQueueInterface_nuno queue;
    private ArrayList<ISBWrapper> isbList;
    private int isbCount;
    private HashMap<String , Integer> topSearchs;
    private int activeISBs;
    private int activeDownloaders;
    
    /**
     * Search Module constructor.
     * Connects to the URLQueueNuno.
     * @throws RemoteException
     */
    public SearchModule() throws RemoteException{
        super();
        this.topSearchs = new HashMap<>();
        this.isbList = new ArrayList<ISBWrapper>();
        this.isbCount = 0;
        this.activeISBs = 0;
        this.activeDownloaders = 0;
        try{
            this.queue = (URLQueueInterface_nuno) LocateRegistry.getRegistry(9871).lookup("URLQueueNuno");
            System.out.println("[SearchModule] Connected to URLQueueNuno");
        } catch(RemoteException re) {
            System.out.println("[SearchModule] Remote Exception: Cannot connect to URLQueueNuno");
        } catch(Exception e) {
            System.out.println("[SearchModule] Exception in constructor " + e);
        }
    }
    
    /**
     * Does a search to one random active ISB.
     * If selected ISB is off, catches RemoteExpection and retries up to 10 times to stablish the connection again.
     * @param key key to search
     * @param retry number of retries already done (recursive call)
     */
    public ArrayList<WebPage> search(String key, int retry) throws RemoteException{
        if(retry >= 10)
            return new ArrayList<>();
        
        else if(retry == 0){
            if(this.topSearchs.containsKey(key))
                this.topSearchs.put(key, this.topSearchs.get(key)+1);
            else{
                this.topSearchs.put(key,1);
            }
        }
        try{
            if(isbList.size() > 0){
                Random rn = new Random();
                for(int i = 0; i < 10; i++){
                    int n = rn.nextInt(0,this.isbCount);
                    if(isbList.get(n).getState() == true){
                        return isbList.get(n).getIsb().search(key);
                    }
                }
                throw new RemoteException();
            }
        } catch (RemoteException re) {
            System.out.println("[SearchModule] Search: No response... Retrying " + (retry+1) + " / 10");
            return this.search(key, retry+1);
        }
        return new ArrayList<>();
    }
    
    /**
     * Does a searchLinks to one random active ISB.
     * If selected ISB is off, catches RemoteExpection and retries up to 10 times to stablish the connection again.
     * @param key key to search
     * @param retry number of retries already done (recursive call)
     */
    public HashSet<String> searchUrl(String key, int retry) throws RemoteException{
        if(retry >= 10)
            return new HashSet<>();
        try{
            if(isbList.size() > 0){
                Random rn = new Random();
                for(int i = 0; i < 10; i++){
                    int n = rn.nextInt(0,this.isbCount);
                    if(isbList.get(n).getState() == true){
                        return isbList.get(n).getIsb().searchLinks(key);
                    }
                }
                throw new RemoteException();
            }
        } catch (RemoteException re) {
            System.out.println("[SearchModule] Search Urls: No response... Retrying " + (retry+1) + " / 10");
            return this.searchUrl(key, retry+1);
        }
        return new HashSet<>();
    }
    
    /**
     * Index a new url on URLQueueNuno.
     * @param url url to index
     */
    public void index_new_url(String url) throws RemoteException{
        System.out.println("[SearchModule] Indexing new url: " + url);
        queue.addUrl(url);
        return;
    }
    
    /**
     * Add given ISB to SearchModule.isbList (RMI callback).
     * If this ISB is already connected (isbname already on list) returns -1.
     * If the ISB was connected it reconnects it (keeps the same number).
     * @param isb ISB to add
     * @param isbname ISB's name
     * @return ISB's number on sucess. Error: return -1.
     */
    public int subscribeIsb(IndexStorageBarrelInterface isb, String isbname){
        try{
            this.getISBs();
        } catch (RemoteException re) {
            re.printStackTrace();
        }
        
        for(int i = 0; i < this.isbList.size(); i++){
            if(isbList.get(i).getName().equals(isbname)){
                if(isbList.get(i).getState()){
                    System.out.println("[SearchModule] Error subscribing ISB: Already running !");
                    return -1;
                }
                else{
                    isbList.get(i).setIsb(isb);
                    System.out.println("[SearchModule] Subscribe ISB Sucess: ISB " + isbname + " reconnected !");
                    return i;
                }
            }
        }
        
        this.isbList.add(new ISBWrapper(isb, isbname));
        this.isbCount++;
        
        System.out.println("[SearchModule] Subscribe ISB Sucess: ISB " + isbname + " subscribed and connected !");
        return this.isbCount;
    }
    
    /**
     * Checks the ISB.Heartbeat() on SearchModule.isbList index i.
     * @param i index
     */
    public void checkISB(int i) {
        //System.out.println("checking ISBs ...");
        try {
            this.isbList.get(i).setState(this.isbList.get(i).getIsb().Heartbeat());
            //System.out.println("ISB " + i + " is ok");
        }
        catch (RemoteException e) {
            this.isbList.get(i).setState(false);
        }
    }
    
    /**
     *
     * @return SearchModule.isbList size
     */
    public int getIsbListSize(){
        return this.isbList.size();
    }
    
    /**
     *
     * @return Connected ISBs of SearchModule
     */
    public int getNumberActiveIsbs(){
        int sum = 0;
        for(ISBWrapper isb: this.isbList){
            if(isb.getState() == true)
                sum++;
        }
        return sum;
    }
    
    /**
     * @return ArrayList containing top 10 searchs made.
     */
    public ArrayList<String> getTopSearchs() throws RemoteException{
        List<String> list = new ArrayList<>(this.topSearchs.keySet());
        Collections.sort(list, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return topSearchs.get(s2).compareTo(topSearchs.get(s1));
            }
        });
        
        return new ArrayList<>(list.subList(0, Math.min(list.size(), 10)));
    }
    
    /**
     * @return ArrayList containing the name of ACTIVE ISBs.
     */
    public ArrayList<String> getISBs() throws RemoteException {
        for(int i = 0; i < this.getIsbListSize(); i++){
            this.checkISB(i);
            if(this.getNumberActiveIsbs() != this.activeISBs){
                this.activeISBs = getNumberActiveIsbs();
            }
        }
        
        ArrayList<String> activeISBs = new ArrayList<>();
        for(ISBWrapper isb: this.isbList){
            if(isb.getState()){
                activeISBs.add(isb.getName());
            }
        }
        
        return activeISBs;
    }
    
    /**
     * @return ArrayList containing the name of ACTIVE Downloaders. (Name = "Downloader <Number>")
     */
    public ArrayList<String> getDownloaders() throws RemoteException {
        for(int i = 0; i < this.queue.getDownloaderListSize(); i++){
            this.queue.checkDownloader(i);
            if(this.queue.getNumberActiveDownloaders() != this.activeDownloaders){;
                this.activeDownloaders = this.queue.getNumberActiveDownloaders();
            }
        }
        
        ArrayList<String> activeDownloaders = new ArrayList<>();
        HashSet<Integer> dHashSet = this.queue.updateDownloaders();
        for(int downloader: dHashSet){
            activeDownloaders.add("Downloader " + downloader);
        }
        
        return activeDownloaders;
    }
    
    /**
     * Register User on All active ISBs.
     * If selected ISB is off, catches RemoteExpection and retries up to 10 times to stablish the connection again.
     * @param username
     * @param password
     * @param retry number of retries already done (recursive call)
     */
    public boolean register(String username , String password, int retry) throws RemoteException {
        int i = 0;
        boolean flag = false;
        if(retry >= 10)
            return false;
        try{
            if(isbList.size() > 0){
                for(i = 0; i < isbList.size(); i++){
                    if(isbList.get(i).getState() == true){
                        flag = isbList.get(i).getIsb().register(username, password);
                    }
                }
            }
        } catch (RemoteException re) {
            System.out.println("[SearchModule] Register: No response... Retrying " + (retry+1) + " / 10");
            this.checkISB(i);
            return this.register(username, password,retry+1);
        }
        return flag;
    }
    
    /**
     * Login: one random active ISB.
     * If selected ISB is off, catches RemoteExpection and retries up to 10 times to stablish the connection again.
     * @param username
     * @param password
     * @param retry number of retries already done (recursive call)
     */
    public boolean login(String username , String password, int retry) throws RemoteException {
        if(retry >= 10)
            return false;
        try{
            if(isbList.size() > 0){
                Random rn = new Random();
                for(int i = 0; i < 10; i++){
                    int n = rn.nextInt(0,this.isbCount);
                    if(isbList.get(n).getState() == true){
                        return isbList.get(n).getIsb().login(username, password);
                    }
                }
                throw new RemoteException();
            }
        } catch (RemoteException re) {
            System.out.println("[SearchModule] Login: no response... Retrying " + (retry+1) + " / 10");
            return this.login(username, password,retry+1);
        }
        return false;
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args){
        try{
            System.out.println("[SearchModule] Turning on...");
            SearchModuleInterface sm = new SearchModule();
            Registry r = LocateRegistry.createRegistry(9872);
            r.rebind("SearchModule", sm);
            System.out.println("[SearchModule] RMI Server ready !");
            
        } catch(RemoteException re) {
            System.out.println("[SearchModule] Remote Exception in main: " + re);
        } catch (Exception e) {
            System.out.println("[SearchModule] Exception in main: " + e);
        }
    }
    
}

/**
 * Secondary Class containing: isb, isb.name, isb.is_active.
 * Used to track the availability of the ISB.
 */
class ISBWrapper {
    
    private IndexStorageBarrelInterface isb;
    private String name;
    private boolean is_active;
    
    public ISBWrapper(IndexStorageBarrelInterface isb, String name){
        this.isb = isb;
        this.name = name;
        this.is_active = true;
    }
    
    public void setIsb(IndexStorageBarrelInterface isb){
        this.isb = isb;
    }
    
    public IndexStorageBarrelInterface getIsb(){return this.isb;}
    public String getName(){return this.name;}
    public boolean getState(){return this.is_active;}
    
    public void setState(boolean s){
        this.is_active = s;
    }
}

