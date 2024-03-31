package src;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
    
    public static void main(String[] args) {
    
    }
    
    public Downloader(int id, int MULTICAST_SEND_PORT, String MULTICAST_ADDRESS, interfaces.URLQueueInterface server) {
        this.socket = null;
        this.group = null;
        this.PORT = MULTICAST_SEND_PORT;
        this.MULTICAST_ADDRESS = MULTICAST_ADDRESS;
        
        this.idDownloader = id;
        this.server = server;
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

