package src;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Barrel extends Thread implements Serializable {
    private final int id;
    private final int multicastPort;
    private final String multicastAddress;
    private MulticastSocket socket;
    private InetAddress group;
    private final HashMap<String, HashSet<String>> index;
    private final HashMap<String, HashSet<String>> links;
    private final HashMap<String, String> webInfo;
    private final String linksFilename;
    private final String wordsFilename;
    private final String textSnippetFilename;
    private final File linksFile;
    private final File wordsFile;
    private final File textSnippetFile;
    
    public Barrel(int id, int multicastPort, String multicastAddress) throws IOException {
        super();
        this.id = id;
        
        this.multicastPort = multicastPort;
        this.multicastAddress = multicastAddress;
        this.socket = new MulticastSocket(multicastPort);
        this.group = InetAddress.getByName(multicastAddress);
        this.socket.joinGroup(group);
        this.links = new HashMap<>();
        this.index = new HashMap<>();
        this.webInfo = new HashMap<>();
        
        this.linksFilename = "../files/links-" + id + ".txt";
        this.wordsFilename = "../files/words-" + id + ".txt";
        this.textSnippetFilename = "../files/text-" + id + ".txt";
        
        this.linksFile = new File(linksFilename);
        this.wordsFile = new File(wordsFilename);
        this.textSnippetFile = new File(textSnippetFilename);
        
        System.out.println("BARREL " + id + " INICIALIZADO COM SUCESSO");
    }
    
    public void run() {
        try {
            while (true) {
                byte[] buffer = new byte[32 * 1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Barrel " + id + " esperando mensagem...");
                
                this.socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Barrel " + id + " recebeu mensagem: " + message);
                
                String[] list = message.split("; ");
                String type = list[0].split(" \\| ")[1];
                int count = Integer.parseInt(list[1].split(" \\| ")[1]);
                String url = list[2].split(" \\| ")[1];
                
                System.out.println("Guardei o url " + url);
                System.out.println("type: " + type + " count: " + count);
                System.out.println(Arrays.toString(list) + "\n");
                if (type.equals("links")) {
                    
                    // TODO: implementar lógica para lidar com URLs
                    System.out.println(count);
                    for (int i = 3; i < count; i++) {
                        String chave = list[i].split(" \\| ")[1];
                        if (!this.links.containsKey(chave))
                            this.links.put(chave, new HashSet<>());
                        this.links.get(chave).add(url);
                    }
                    
                } else if (type.equals("words")) {
                    System.out.println("entrei no words");
                    // TODO: implementar lógica para lidar com palavras
    
                    for (int i = 3; i < count; i++) {
                        String chave = list[i].split(" \\| ")[1];
                        if (!this.index.containsKey(chave))
                            this.index.put(chave, new HashSet<>());
                        this.index.get(chave).add(url);
                    }
                    
                    
                    
                } else if (type.equals("textSnippet")) {
                    System.out.println("entrei no snippet");
                    // TODO: implementar lógica para lidar com snippets de texto
    
                    String chave = list[3].split(" \\| ")[1];
                    if (!this.webInfo.containsKey(url))
                        this.webInfo.put(url, chave);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro no Barrel " + id + ": " + e);
        }
    }

    
    
    
    public void guardarURLs(String[] list) {
    
    }
}
