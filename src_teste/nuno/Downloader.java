package src_teste.nuno;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.DatagramPacket;
import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.InetSocketAddress;
//import java.util.HashMap;
import java.net.InetSocketAddress;

import java.text.Normalizer;

/**
 * Downloader Main Class
 */
public class Downloader extends UnicastRemoteObject implements DownloaderInterface, Runnable{
    
    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT = 4321;
    private MulticastSocket socket;
    private InetAddress group;
    private URLQueueInterface_nuno queue;
    
    private int downloaderID;
    private int packetID;
    
    private HashMap<Integer , DatagramPacket> packetBuffer;
    
    
    /**
     * Downloader constructor
     * @param s Multicast Socket
     * @param g Inet Address
     * @param q URLQueueInterface_nuno
     * @throws RemoteException
     */
    public Downloader(MulticastSocket s, InetAddress g, URLQueueInterface_nuno q) throws RemoteException {
        super();
        this.socket = s;
        this.group = g;
        this.queue = q;
        this.packetID = 0;
        
        this.packetBuffer = new HashMap<>();
        
        new Thread(this,"Downloader").start();
    }
    
    public void setNumber(int n){
        this.downloaderID = n;
    }
    
    public int getNumber(){
        return this.downloaderID;
    }
    
    /**
     * Used as checker by URLQueueNuno.
     * @return true
     */
    public boolean Heartbeat() throws RemoteException {
        return true;
    }
    
    /**
     * Send a UDP multicast Packet with "addWebpage" operation
     * @param url Webpage url
     * @param title Webpage title
     * @param text Webpage text
     */
    public void sendAddWebPage(String url, String title, String text){
        if(url.length() < 1 || title.length() < 1 || text.length() < 1) return;
        
        try{
            String message = "alldeep|downloader|" + this.downloaderID + "|" + this.packetID + "|addWebPage|" + url + "|" + title + "|" + text + "|";
            //System.out.println(message);
            
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            
            this.packetBuffer.put(this.packetID, packet);
            socket.send(packet);
            this.packetID++;
            
        } catch (IOException e) {
            System.out.println("[Downloader] Error: sending addWebPage ");
        }
    }
    
    /**
     * Send a UDP multicast Packet with "addWord" operation
     * @param url Webpage url
     * @param word Webpage word
     */
    public void sendAddWord(String url, String word){
        if(url.length() < 1 || word.length() < 1) return;
        
        try{
            String message = "alldeep|downloader|" + this.downloaderID + "|" + this.packetID + "|addWord|" + url + "|" + word + "|";
            //System.out.println(message);
            
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            
            this.packetBuffer.put(this.packetID, packet);
            socket.send(packet);
            this.packetID++;
            
        } catch (IOException e) {
            System.out.println("[Downloader] Error: sending addWord ");
        }
    }
    
    /**
     * Send a UDP multicast Packet with "addLink" operation
     * @param url parent Webpage url
     * @param son son Webpage url
     */
    public void sendAddLink(String url, String son){
        if(url.length() < 1 || son.length() < 1) return;
        
        try{
            String message = "alldeep|downloader|" + this.downloaderID + "|" + this.packetID + "|addLink|" + url + "|" + son + "|";
            //System.out.println(message);
            
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            
            this.packetBuffer.put(this.packetID, packet);
            socket.send(packet);
            this.packetID++;
            
        } catch (IOException e) {
            System.out.println("[Downloader] Error: sending addUrl ");
        }
    }
    
    /**
     * Send a UDP multicast Packet stored on Downloader.packetBuffer with index packetID
     * @param packetID packet Identifeir
     */
    synchronized public void resendPacket(int packetID){
        //System.out.println("resending packet " + packetID);
        try{
            if(this.packetBuffer.containsKey(packetID))
                socket.send(this.packetBuffer.get(packetID));
            else{
                String message = "alldeep|downloader|" + this.downloaderID + "|" + this.packetID + "|Error|PacketNotFound|";
                
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
            }
        } catch (IOException e) {
            System.out.println("[Downloader] Error: resending packet " + packetID);
        }
    }
    
    /**
     * Download Webpage information.
     * Send UDP multicast packets with parsed information.
     * @param url Webpage url
     */
    public void downloadWebPage(String url) throws RemoteException {
        try {
            Document doc = Jsoup.connect(url).ignoreHttpErrors(true).get();
            StringTokenizer tokens = new StringTokenizer(doc.text(), " ,.:;!?()|\n\t");
            
            String url_title = removeSpecialChars(doc.title());
            if(url_title.length() < 1) url_title = "Untitled Page";
            
            String url_text = removeSpecialChars(doc.text().replaceAll("|", ""));
            if(doc.text().length() > 100){
                url_text = removeSpecialChars(doc.text().substring(0, 100).replaceAll("|", ""));
                url_text += "...";
            }
            
            sendAddWebPage(url, url_title, url_text);
            
            while (tokens.hasMoreElements()){
                String word = removeSpecialChars(tokens.nextToken().replaceAll(",.:;!?()|\n\t","").toLowerCase());
                sendAddWord(url, word);
            }
            
            Elements links = doc.select("a[href]");
            for (Element link : links){
                //System.out.println(link.attr("abs:href"));
                if(link.attr("abs:href").length() < 100){
                    String aux = link.attr("abs:href").replaceAll("/", "");
                    int slashCount = link.attr("abs:href").length() - aux.length();
                    
                    if(slashCount <= 8 && slashCount >= 1)
                        sendAddLink(url, link.attr("abs:href"));
                    queue.addUrl(link.attr("abs:href"));
                }
            }
            
        } catch (IOException e) {
            System.out.println("[Downloader] Error: downloading " + url);
        }
    }
    
    /**
     * Remove all special characters in a String text.
     * @param text source text
     * @return clean text
     */
    public static String removeSpecialChars(String text) {
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalizedText = normalizedText.replaceAll("\\p{M}", "");
        normalizedText = normalizedText.replaceAll("[^a-zA-Z0-9 ]", "");
        return normalizedText;
    }
    
    public static void main(String[] args){
        MulticastSocket socket = null;
        try{
            System.out.println("[Downloader] Turning on... ");
            socket = new MulticastSocket(PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            
            URLQueueInterface_nuno queue = (URLQueueInterface_nuno) LocateRegistry.getRegistry(9871).lookup("URLQueueNuno");
            System.out.println("[Downloader] Connected to URLQueueNuno !");
            Downloader d = new Downloader(socket,group,queue);
            d.setNumber(queue.subscribeDownloader((DownloaderInterface) d));
            System.out.println("[Downloader] I'm ready !");
            
        } catch(RemoteException re){
            System.out.println("[Downloader] Remote Exception: Cannot connect to URLQueueNuno");
        } catch(Exception e){
            System.out.println("[Downloader] Exception in main: " + e);
        } finally {
            //socket.close();
        }
    }
    
    public void run(){
        new MulticastDownloader(this);
    }
    
}

/**
 * Thread responsible to receive multicast messages and create DMsgHandler to all of them
 */
class MulticastDownloader implements Runnable {
    
    private Downloader d;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    
    MulticastDownloader(Downloader d){
        new Thread(this,"DownloaderISB").start();
        this.d = d;
    }
    
    public void run(){
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(PORT);
            InetSocketAddress group = new InetSocketAddress(MULTICAST_ADDRESS, PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");
            socket.joinGroup(group,netIf);
            
            System.out.println("[Downloader] Multicast: Listening ...");
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                //System.out.println("[Downloader] Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                String message = new String(packet.getData(), 0, packet.getLength());
                //System.out.println(message);
                new DMsgHandler(this.d,message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
            System.out.println("[Downloader] Multicast: Turning off ...");
        }
    }
}

/**
 * Thread responsible to parse information of messages received by multicast.
 */
class DMsgHandler implements Runnable {
    
    private Downloader d;
    private String message;
    
    DMsgHandler(Downloader d, String m){
        new Thread(this,"DMsgHandler").start();
        this.d = d;
        this.message = m;
    }
    
    public void run() {
        StringTokenizer st = null;
        try {
            st = new StringTokenizer(this.message, "|");
            if (st.nextToken().equals("alldeep")){
                if(st.nextToken().equals("isb")){
                    if(st.nextToken().equals("retry")){
                        int downloader_number = Integer.parseInt(st.nextToken());
                        if(downloader_number == d.getNumber()){
                            int packet_number = Integer.parseInt(st.nextToken());
                            d.resendPacket(packet_number);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Downloader] Unformated message received: " + this.message);
            e.printStackTrace();
        }
    }
}
