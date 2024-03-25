package src_teste.lucas;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

public class Queue extends UnicastRemoteObject implements QueueInterface {
    LinkedBlockingQueue<String> TheQueue = new LinkedBlockingQueue<String>();
    ArrayList<Integer> WebCrawlerPorts = new ArrayList<Integer>();

    public Queue() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        String url = "https://en.wikipedia.org/wiki/Survival_horror";

        try {
            Queue obj = new Queue();
            Registry registry = LocateRegistry.createRegistry(6969);
            registry.bind("Queue", obj);
            System.out.println("Queue bound in registry");
            clearFiles();
            obj.putLink(url);
        } catch (Exception e) {
            System.out.println("Queue err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public void putLink(String s) throws RemoteException {
        TheQueue.offer(s);
        //System.out.println("This URL was offered: " + s);

    }

    
    public String getLink() throws RemoteException{
        String url = "";
        try {
            url = TheQueue.take();
            //System.out.println("This URL was taken: " + url);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return url;
    }

    public void addWebCrawlerPort(Integer port) throws RemoteException {
        WebCrawlerPorts.add(port);
    }

    public ArrayList<Integer> getWebCrawlerPorts() throws RemoteException {
        return WebCrawlerPorts;
    }

    public static void clearFiles(){ 
        ArrayList<String> list = new ArrayList<String>();
        list.add("LinkInfo.txt");
        list.add("LinkLink.txt");
        list.add("Users.txt");
        list.add("WordLink.txt");
        for(String s : list){
            try{
            FileWriter fw = new FileWriter("./src/main/java/com/example/googol/src/barrels/Information/"+s, false);
            PrintWriter pw = new PrintWriter(fw, false);
            pw.flush();
            pw.close();
            fw.close();
        }
        catch(Exception exception){
            System.out.println("Exception have been caught");
        }
        }
        

    }
    
    



    
}
