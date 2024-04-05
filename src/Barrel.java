package src;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

public class Barrel extends Thread implements Serializable {
    private final int id;
    private final int multicastPort;
    private final String multicastAddress;
    private MulticastSocket socket;
    private InetAddress group;
    private HashMap<String, HashSet<String>> index; //chave: palavra, valor: conjunto de urls
    private HashMap<String, HashSet<String>> links; //chave: url, valor: conjunto de urls
    private HashMap<String, String> webInfo; //chave: url, valor: snippet de texto
    private final String linksFilename; //nome do arquivo que guarda os links
    private final String wordsFilename; //nome do arquivo que guarda as palavras
    private final String textSnippetFilename; //nome do arquivo que guarda os snippets de texto
    private final File linksFile; //arquivo que guarda os links
    private final File wordsFile; //arquivo que guarda as palavras
    private final File textSnippetFile; //arquivo que guarda os snippets de texto
    private Semaphore sem;
    
    public Barrel(int id, int multicastPort, String multicastAddress) throws IOException {
        super();
        this.id = id;
        
        this.multicastPort = multicastPort;
        this.multicastAddress = multicastAddress;
        this.socket = new MulticastSocket(multicastPort);
        this.group = InetAddress.getByName(multicastAddress);
        this.socket.joinGroup(new InetSocketAddress(group, multicastPort), NetworkInterface.getByIndex(0));
        //this.links = new HashMap<>();
        //this.index = new HashMap<>();
        //this.webInfo = new HashMap<>();
        
        this.linksFilename = "./database/links-" + id + ".txt";
        this.wordsFilename = "./database/palavras-" + id + ".txt";
        this.textSnippetFilename = "./database/texto-" + id + ".txt";
        
        this.linksFile = new File(linksFilename);
        this.wordsFile = new File(wordsFilename);
        this.textSnippetFile = new File(textSnippetFilename);
    
        this.sem = new Semaphore(1);
        System.out.println("BARREL " + id + " INICIALIZADO COM SUCESSO");
    }
    
    public void run() {
        while (true) {
            try {
                this.links = new HashMap<>();
                this.index = new HashMap<>();
                this.webInfo = new HashMap<>();
                
                byte[] buffer = new byte[32 * 1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Barrel " + id + " esperando mensagem...");
                
                this.socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                //System.out.println("Barrel " + id + " recebeu mensagem: " + message);
                
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
                
                updateFiles(url, this.index, this.links, this.webInfo);
            } catch (Exception e) {
                System.out.println("[Erro no Barrel " + id + "] " + e);
            }
        }
    }
    
    private void updateFiles(String url_original, HashMap<String, HashSet<String>> index, HashMap<String, HashSet<String>> links, HashMap<String, String> webInfo) {
        try {
            this.sem.acquire();
            
            FileWriter fw = new FileWriter(wordsFile, true);
            for (String word : index.keySet()) {
                fw.write(word);
                //System.out.println("word: " + word);
                for (String url : index.get(word)) {
                    fw.write(" | " + url);
                }
                fw.write("\n");
            }
            fw.close();
    
            fw = new FileWriter(linksFile, true);
            for (String link : links.keySet()) {
                fw.write(link);
                for (String url : links.get(link)) {
                    fw.write(" | " + url);
                }
                fw.write("\n");
            }
            //fw.write("\n");
            fw.close();
            
            fw = new FileWriter(textSnippetFile, true);
            for (String url : webInfo.keySet()) {
                fw.write(url + " | " + webInfo.get(url) + "\n");
            }
            fw.close();
            
            this.sem.release();
        } catch (Exception e) {
            System.out.println("[EXCEPTION] While updating links: " + e);
        }
    }
    
    
    public void guardarURLs(String[] list) {
    
    }
    
    public String[] obterLinks(String palavra) {
        System.out.println("Pesquisando links com a palavra: " + palavra);
        
        HashSet<String> urls = new HashSet<>();
        /*
        if (urls == null) {
            System.out.println("Nenhum link encontrado com a palavra: " + palavra);
            return new String[]{"Nenhum link encontrado"};
        }
        
         */
        try {
            this.sem.acquire();
            BufferedReader fr = new BufferedReader(new FileReader(wordsFile));
            
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (parts[0].equals(palavra)) {
                    urls.addAll(Arrays.asList(parts).subList(1, parts.length));
                }
            }
            fr.close();
            this.sem.release();
        } catch (Exception e) {
            System.out.println("[EXCEPTION] " + e);
            return new String[]{"Erro ao pesquisar links"};
        }
        if (urls.isEmpty())
            return new String[]{"Nenhum link encontrado"};
        return urls.toArray(new String[0]);
    }
}
