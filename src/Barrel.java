package src;

import java.io.Serializable;

public class Barrel extends Thread implements Serializable {
    private final int id;
    private final int multicastPort;
    private final String multicastAddress;
    
    public Barrel(int id, int multicastPort, String multicastAddress) {
        super();
        this.id = id;
        this.multicastPort = multicastPort;
        this.multicastAddress = multicastAddress;
        
        
        //start();
    }
    
    public void run() {
        System.out.println("Barrel " + id + " started");
    }
    
}
