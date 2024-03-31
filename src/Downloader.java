package src;

import interfaces.URLQueueInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;
import java.net.*;
import java.rmi.Remote;


public class Downloader extends Thread implements Remote {
    private Downloader d;
    
    private final String multicastAddress;
    private final int multicastPort;
    private final InetAddress group;
    private final MulticastSocket socket;
    private final int idDownloader;
    private URLQueueInterface urlQueue;
    private final HashMap<String, HashSet<String>> index = new HashMap<>();
    
    
    public Downloader(int id, int MULTICAST_PORT, String MULTICAST_ADDRESS) throws RemoteException {
        this.socket = null;
        this.group = null;
        this.multicastPort = MULTICAST_PORT;
        this.multicastAddress = MULTICAST_ADDRESS;
        
        this.idDownloader = id;
        
        try {
            this.urlQueue = (URLQueueInterface) Naming.lookup("URLQUEUE");
            run();
        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Erro ao ligar à URL queue");
        }
    }
    
    public void run() {
        int size = 1;
        while (true) {
            try {
                if (!this.urlQueue.isEmpty() && this.urlQueue.size() == size) {
                    System.out.println("Conteudo na queue:");
                    System.out.println(this.urlQueue.getUrlQueue() + "\n");
                    size++;
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            
        }
        /*
        
        try {
            InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert socket != null;
            socket.close();
        }
        */
    }
    
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        Properties prop = new Properties();
        String SETTINGS_PATH = "properties/configuration.properties";
        
        try {
            prop.load(new FileInputStream(SETTINGS_PATH));
            
            int port = Integer.parseInt(prop.getProperty("PORT_SERVER"));
            String address = prop.getProperty("SERVER_REGISTRY_NAME");
            
            new Downloader(1, port, address);
            
        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Erro");
        }
    }
    
    
    public void getWebsites(String url) {
        if (url.equals("")) {
            System.out.print("URL is null or empty");
            return;
        }
        
        try {
            Document doc = Jsoup.connect(url).get();
            String title = doc.title();
            
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements() && countTokens++ < 100)
                System.out.println(tokens.nextToken().toLowerCase());
            Elements links = doc.select("a[href]");
            for (Element link : links)
                System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
    }
    
    private void sendMessage(String message) {
        try {
            //String message = this.getName() + " packet " + counter++;
            byte[] buffer = message.getBytes();
            
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);
            socket.send(packet);
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert socket != null;
            socket.close();
        }
    }
    
    public String getUrlQueue() {
        return urlQueue.toString();
    }
}

