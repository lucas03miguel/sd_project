package src;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;

public class Barrel extends Thread implements Serializable {
    private final int id;
    private final int multicastPort;
    private final String multicastAddress;
    private MulticastSocket socket;
    private InetAddress group;
    
    public Barrel(int id, int multicastPort, String multicastAddress) throws IOException {
        super();
        this.id = id;
    
        this.multicastPort = multicastPort;
        this.multicastAddress = multicastAddress;
        this.socket = new MulticastSocket(multicastPort);
        this.group = InetAddress.getByName(multicastAddress);
        this.socket.joinGroup(new InetSocketAddress(group, multicastPort), NetworkInterface.getByIndex(0));
        
        System.out.println("BARREL " + id + " INICIALIZADOO COM SUCESSO");
        //start();
    }
    
    public void run() {
        try {
            
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("A espera de boi");
                this.socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Recebi boi " + message);
                
                
                String[] list = message.split(";");
                //String id = list[0].split(":")[1];
                String type = list[0].split(" \\| ")[1];
                
                System.out.println("type: " + type + "\n\n");
                if (type.equals("url")) {
                    //TODO: implementem esta shit de merda
                    String url = list[1].split(" \\| ")[1];
                    System.out.println("entrei no url " + url);
                    //guardarURLs(list);
                    
                    
                } else if (type.equals("words")) {
                    //TODO: implementem esta shit
                    System.out.println("entrei no words");
                    
                } else if (type.equals("textSnippet")) {
                    //TODO: implementem esta shit
                    System.out.println("entrei no snippet");
                    
                    
                }
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e);
        }
    }
    
    public void guardarURLs(String[] list) {
    
    }
}
