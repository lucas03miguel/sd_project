package src;

import interfaces.RMIBarrelInterface;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class IndexStorageBarrels {
    private final int id;
    private final String barrelsHostName;
    private final int barrelsPort;
    private final String barrelsRMIRegister;
    private final int multPort;
    private final String multAddress;
    private final MulticastSocket socket;
    private final ArrayList<Object> barrelsThreads;
    private final InetAddress group;
    
    public IndexStorageBarrels(int id, String host, int port, String rmiRegister, int multPort, String multAddress) throws Exception {
        super();
        this.id = id;
        this.barrelsThreads = new ArrayList<>();
        this.barrelsHostName = host;
        this.barrelsPort = port;
        this.barrelsRMIRegister = rmiRegister;
        
        LocateRegistry.createRegistry(port);
        try {
            this.multPort = multPort;
            this.multAddress = multAddress;
            this.socket = new MulticastSocket(multPort);
            this.group = InetAddress.getByName(multAddress);
            this.socket.joinGroup(new InetSocketAddress(group, multPort), NetworkInterface.getByIndex(0));
            
            
            //Naming.rebind(rmiRegister, (RMIBarrelInterface)this);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        //run();
    }
    
    public void run() {
        try {
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                //System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
    
                String[] list = message.split(";");
                //String id = list[0].split(":")[1];
                String type = list[0].split("\\|")[1];
                
                if (type.equals("url")) {
                    //TODO: implementem esta shit de merda
                    
                    
                } else if (type.equals("words")) {
                    //TODO: implementem esta shit
                    
                    
                } else if (type.equals("textSnippet")) {
                    //TODO: implementem esta shit
                    
                    
                }
            }
        } catch (Exception re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
        
    }
    
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        Properties prop = new Properties();
        String SETTINGS_PATH = "properties/configuration.properties";
        
        try {
            prop.load(new FileInputStream(SETTINGS_PATH));
    
            int multPort = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
            String multAddress = prop.getProperty("MULTICAST_ADDRESS");
            int port = Integer.parseInt(prop.getProperty("PORT_BARRELS"));
            String host = prop.getProperty("HOST_BARRELS");
            String rmiRegister = prop.getProperty("RMI_REGISTRY_NAME_BARRELS");
            
            new IndexStorageBarrels(1, host, port, rmiRegister, multPort, multAddress);
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
