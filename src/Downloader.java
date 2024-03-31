package src;

import interfaces.URLQueueInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;
import java.net.*;
import java.rmi.Remote;


public class Downloader extends Thread implements Remote {
    
    private final String MULTICAST_ADDRESS;
    private final int PORT;
    private final InetAddress group;
    private final MulticastSocket socket;
    private final int idDownloader;
    private final interfaces.URLQueueInterface server;
    private final HashMap<String, HashSet<String>> index = new HashMap<>();
    
    
    public Downloader(int id, int MULTICAST_PORT, String MULTICAST_ADDRESS, URLQueueInterface url) {
        this.socket = null;
        this.group = null;
        this.PORT = MULTICAST_PORT;
        this.MULTICAST_ADDRESS = MULTICAST_ADDRESS;
        
        this.idDownloader = id;
        this.server = url;
    }
    
    public void run() {
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
            
            new Downloader(1, port, address, null);
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
            
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert socket != null;
            socket.close();
        }
    }
}

