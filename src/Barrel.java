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
            //TODO: esta shit é uma shit e nao esta como quero fds
            this.sem.acquire();
            if (!linksFile.exists()) {
                linksFile.createNewFile();
            }
            if (!wordsFile.exists()) {
                wordsFile.createNewFile();
            }
            if (!textSnippetFile.exists()) {
                textSnippetFile.createNewFile();
            }
            
            // Ler o conteúdo atual do arquivo de palavras
            HashMap<String, HashSet<String>> currentIndex = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader(wordsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                String word = parts[0];
                HashSet<String> urls = new HashSet<>(Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length)));
                currentIndex.put(word, urls);
            }
            reader.close();
            
            // Atualizar o índice atual com as novas palavras e URLs
            for (String word : index.keySet()) {
                if (!currentIndex.containsKey(word)) {
                    currentIndex.put(word, new HashSet<>());
                }
                currentIndex.get(word).addAll(index.get(word));
            }
            
            // Escrever o índice atualizado no arquivo de palavras
            FileWriter writer = new FileWriter(wordsFile);
            for (String word : currentIndex.keySet()) {
                writer.write(word);
                for (String url : currentIndex.get(word)) {
                    writer.write(" | " + url);
                }
                writer.write("\n");
            }
            writer.close();
            
            // Escrever os links no arquivo de links
            FileWriter linksWriter = new FileWriter(linksFile, true);
            for (String link : links.keySet()) {
                linksWriter.write(link);
                for (String url : links.get(link)) {
                    linksWriter.write(" | " + url);
                }
                linksWriter.write("\n");
            }
            linksWriter.close();
            
            // Escrever os snippets de texto no arquivo de snippets
            FileWriter snippetWriter = new FileWriter(textSnippetFile, true);
            for (int i = 0; i < webInfo.size(); i += 3) {
                snippetWriter.write(webInfo.get(i) + " | " + webInfo.get(i + 1) + " | " + webInfo.get(i + 2) + "\n");
            }
            snippetWriter.close();
            
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
            
            fr = new BufferedReader(new FileReader(linksFile));
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (urls.contains(parts[0])) {
                    if (!resp.containsKey(parts[0]))
                        resp.put(parts[0], new HashSet<>());
                    for (int i = 1; i < parts.length; i++) {
                        resp.get(parts[0]).add(parts[i]);
                    }
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
