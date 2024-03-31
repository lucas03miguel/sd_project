package src;

import interfaces.RMIBarrelInterface;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class IndexStorageBarrels {
    private final int id;
    private final String hostName;
    private final int port;
    private final String rmiRegister;
    private final ArrayList<Object> barrelsThreads;
    
    public IndexStorageBarrels(int id, String host, int port, String rmiRegister) throws RemoteException {
        super();
        this.id = id;
        this.barrelsThreads = new ArrayList<>();
        this.hostName = host;
        this.port = port;
        this.rmiRegister = rmiRegister;
    
        LocateRegistry.createRegistry(1099);
    
        try {
            Naming.rebind("XPTO", (RMIBarrelInterface)this);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        run();
    }
    
    public void run() {
        try {
        
        } catch (Exception re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }
    
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
    
        try {
            new IndexStorageBarrels(1, "10.1.1.1", 5000, "barrelsDeVinho");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /*
    private Barrel selectBarrelToExcute() {
        // select a random barrel to fulfill the task
        if (this.barrels_threads.size() == 0) {
            System.out.println("[BARREL-INTERFACE] No barrels to fulfill the task");
            // no barrels to fulfill the task
            return null;
        }
        
        int random = (int) (Math.random() * this.barrels_threads.size());
        
        // check if barrel is alive if not remove from barrels_threads and select another barrel
        if (!this.barrels_threads.get(random).isAlive()) {
            System.out.println("[BARREL-INTERFACE] Barrel " + random + " is not alive");
            this.barrels_threads.remove(random);
            return this.selectBarrelToExcute();
        }
        
        return this.barrels_threads.get(random);
    }
    
     */
}
