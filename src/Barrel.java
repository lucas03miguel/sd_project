package src;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
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
    private ArrayList<String> webInfo; //chave: url, valor: snippet de texto
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
                this.webInfo = new ArrayList<>();
                
                byte[] buffer = new byte[32 * 1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Barrel " + id + " esperando mensagem...");
                
                this.socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                //System.out.println("Barrel " + id + " recebeu mensagem: " + message);
                
                String[] list = message.split("; ");
                String type = list[0].split(" \\| ")[1];
                String url = list[1].split(" \\| ")[1];
                
                
                System.out.println("Guardei o url " + url);
                //System.out.println("type: " + type + " count: " + count);
                //System.out.println(Arrays.toString(list) + "\n");
                if (type.equals("links")) {
                    int count = Integer.parseInt(list[2].split(" \\| ")[1]);
                    
                    for (int i = 3; i < count; i++) {
                        String chave = list[i].split(" \\| ")[1];
                        if (!this.links.containsKey(chave))
                            this.links.put(chave, new HashSet<>());
                        this.links.get(chave).add(url);
                    }
                    
                } else if (type.equals("words")) {
                    int count = Integer.parseInt(list[2].split(" \\| ")[1]);
    
                    for (int i = 3; i < count; i++) {
                        String chave = list[i].split(" \\| ")[1];
                        if (!this.index.containsKey(chave))
                            this.index.put(chave, new HashSet<>());
                        this.index.get(chave).add(url);
                    }
                    
                    
                    
                } else if (type.equals("textSnippet")) {
                    String titulo = list[2].split(" \\| ")[1];
    
                    String chave = list[3].split(" \\| ")[1];
                    webInfo.add(url);
                    webInfo.add(titulo);
                    System.out.println("titulo: " + titulo);
                    webInfo.add(chave);
                    System.out.println("chave: " + chave);
                    
                }
                
                updateFiles(url, this.index, this.links, this.webInfo);
            } catch (Exception e) {
                System.out.println("[Erro no Barrel " + id + "] " + e);
            }
        }
    }
    
    private void updateFiles(String url_original, HashMap<String, HashSet<String>> index, HashMap<String, HashSet<String>> links, ArrayList<String> webInfo) {
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
            for (int i = 0; i < webInfo.size(); i += 3) {
                fw.write(webInfo.get(i) + " | " + webInfo.get(i + 1) + " | " + webInfo.get(i + 2) + "\n");
            }
            fw.close();
            
            this.sem.release();
        } catch (Exception e) {
            System.out.println("[EXCEPTION] While updating links: " + e);
        }
    }
    
    
    public void guardarURLs(String[] list) {
    
    }
    
    public HashMap<String, HashSet<String>> obterLinks(String palavra) {
        System.out.println("Pesquisando links com a palavra: " + palavra);
        ArrayList<String> urls = new ArrayList<>();
        HashMap<String, HashSet<String>> resp = new HashMap<>();
        
        try {
            this.sem.acquire();
            BufferedReader fr = new BufferedReader(new FileReader(wordsFile));
    
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (palavra.contains(parts[0]))
                    urls.add(parts[1]);
            }
            fr.close();
            
            fr = new BufferedReader(new FileReader(textSnippetFile));
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (urls.contains(parts[0])) {
                    resp.put(parts[0], new HashSet<>());
                    resp.get(parts[0]).add(parts[1]);
                    resp.get(parts[0]).add(parts[2]);
                }
            }
            
            this.sem.release();
        } catch (Exception e) {
            System.out.println("[EXCEPTION] " + e);
            HashMap<String, HashSet<String>> error = new HashMap<>();
            error.put("Erro ao pesquisar links", new HashSet<>());
            return error;
        }
        if (urls.isEmpty()) {
            HashMap<String, HashSet<String>> error = new HashMap<>();
            error.put("Nenhum link encontrado", new HashSet<>());
            return error;
        }
        return resp;
    }
}
