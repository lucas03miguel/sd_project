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
    private HashMap<String, ArrayList<String>> webInfo; //chave: url, valor: snippet de texto
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
        this.links = new HashMap<>();
        this.index = new HashMap<>();
        this.webInfo = new HashMap<>();
        
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
                //HashMap<String, HashSet<String>> auxLinks = new HashMap<>();
                //HashMap<String, HashSet<String>> auxWords = new HashMap<>();
                //ArrayList<String> auxInfo = new ArrayList<>();
                
                
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
                    String texto = list[3].split(" \\| ")[1];
                    
                    if (!this.webInfo.containsKey(url)) {
                        ArrayList<String> aux = new ArrayList<>();
                        aux.add(titulo);
                        aux.add(texto);
                        this.webInfo.put(url, aux);
                    }
                    
                    System.out.println("titulo: " + titulo);
                    System.out.println("chave: " + texto);
                    
                }
                
                updateFiles();
            } catch (Exception e) {
                System.out.println("[Erro no Barrel " + id + "] " + e);
            }
        }
    }
    
    private void updateFiles() {
        try {
            this.sem.acquire();
            /*
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
                if (!this.index.containsKey(word)) {
                    this..put(word, new HashSet<>());
                }
                currentIndex.get(word).addAll(index.get(word));
            }
            */
            
            FileWriter writer = new FileWriter(wordsFile);
            for (String word : this.index.keySet()) {
                writer.write(word);
                for (String url : this.index.get(word))
                    writer.write(" | " + url);
                writer.write("\n");
            }
            writer.close();
            
            
            FileWriter linksWriter = new FileWriter(linksFile);
            for (String link : this.links.keySet()) {
                linksWriter.write(link);
                for (String url : this.links.get(link))
                    linksWriter.write(" | " + url);
                linksWriter.write("\n");
            }
            linksWriter.close();
            
            
            FileWriter snippetWriter = new FileWriter(textSnippetFile);
            for (String url : this.webInfo.keySet()) {
                snippetWriter.write(url + " | " + this.webInfo.get(url).toArray()[0] + " | " + this.webInfo.get(url).toArray()[1] + "\n");
            }
            snippetWriter.close();
            
            this.sem.release();
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Erro a atualizar os ficheiros: " + e);
        }
    }
    /*
    private void guardarLinks() {
        try (BufferedReader fr = new BufferedReader(new FileReader(linksFile))){
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                
                String url = parts[0];
                if (!this.links.containsKey(url))
                    this.links.put(url, new HashSet<>());
                this.links.get(url).add(url);
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Erro ao ler do ficheiro dos links: " + e);
        }
    }
    
    private void guardarPalavras() {
        try (BufferedReader fr = new BufferedReader(new FileReader(wordsFile))) {
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                String word = parts[0];
                if (!this.index.containsKey(word))
                    this.index.put(word, new HashSet<>());
                this.index.get(word).add(url);
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Erro ao ler do ficheiro das palavras: " + e);
        }
    }
    
    private void guardarInfo() {
    
    }
    */
    
    
    public HashMap<String, ArrayList<String>> obterLinks(String palavra) {
        System.out.println("Pesquisando links com a palavra: " + palavra);
        ArrayList<String> urls = new ArrayList<>();
        HashMap<String, ArrayList<String>> resp = new HashMap<>();
        
        try {
            this.sem.acquire();
            BufferedReader fr = new BufferedReader(new FileReader(wordsFile));
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (palavra.contains(parts[0])) {
                    urls.addAll(Arrays.asList(parts).subList(1, parts.length));
                }
            }
            fr.close();
            
            fr = new BufferedReader(new FileReader(textSnippetFile));
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (urls.contains(parts[0])) {
                    resp.put(parts[0], new ArrayList<>());
                    resp.get(parts[0]).add(parts[1]);
                    resp.get(parts[0]).add(parts[2]);
                }
            }
            
            fr = new BufferedReader(new FileReader(linksFile));
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (resp.containsKey(parts[0])) {
                    if (resp.get(parts[0]).size() < 3) {
                        resp.get(parts[0]).add(String.valueOf(0));
                    } else {
                        int atual = Integer.parseInt(resp.get(parts[0]).get(2));
                        resp.get(parts[0]).set(2, String.valueOf(atual + 1));
                    }
                }
            }
            
            this.sem.release();
        } catch (Exception e) {
            System.out.println("[EXCEPTION] " + e);
            HashMap<String, ArrayList<String>> error = new HashMap<>();
            error.put("Erro ao pesquisar links", new ArrayList<>());
            return error;
        }
        if (urls.isEmpty()) {
            HashMap<String, ArrayList<String>> error = new HashMap<>();
            error.put("Nenhum link encontrado", new ArrayList<>());
            return error;
        }
        return resp;
    }
}
