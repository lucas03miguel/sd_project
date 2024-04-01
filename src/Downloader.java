package src;

import interfaces.URLQueueInterface;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;


public class Downloader extends Thread implements Remote {
    private final String multicastAddress;
    private final int multicastPort;
    private final InetAddress group;
    private final MulticastSocket socket;
    private final int idDownloader;
    private final URLQueueInterface urlQueue;
    private HashMap<String, HashSet<WebPage>> index;
    
    
    public Downloader(int id, int MULTICAST_PORT, String MULTICAST_ADDRESS) throws Exception {
        this.socket = null;
        this.group = null;
        this.multicastPort = MULTICAST_PORT;
        this.multicastAddress = MULTICAST_ADDRESS;
        
        this.idDownloader = id;
        
        this.urlQueue = (URLQueueInterface) Naming.lookup("URLQUEUE");
        this.index = new HashMap<>();
        System.out.println("Download criado com sucesso");
        start();
    }
    
    public void run() {
        while (true) {
            try {
                String url = this.urlQueue.removerLink();
                if (url == null) continue;
                
                System.out.println("Processando URL: " + url);
                // Processa o URL aqui
                processarURL(url);
                System.out.println(index.toString());
                
            } catch (RemoteException e) {
                System.out.println("[DOWNLOADER] Erro:" + e);
            }
        }
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
    
    /*
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
    */
    
    private String obterConteudoDaPagina(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        return document.html();
    }
    
    private String extrairTitulo(String conteudo) {
        Document document = Jsoup.parse(conteudo);
        Element titleElement = document.selectFirst("title");
        return titleElement != null ? titleElement.text() : "";
    }
    
    private String extrairTextSnippet(String conteudo) {
        Document document = Jsoup.parse(conteudo);
        Elements paragraphs = document.select("p");
        if (!paragraphs.isEmpty()) {
            return paragraphs.first().text();
        }
        return "";
    }
    
    private Set<String> extrairPalavras(String conteudo) {
        Set<String> words = new HashSet<>();
        Document document = Jsoup.parse(conteudo);
        String text = document.text();
        String[] wordArray = text.split("\\s+");
        for (String word : wordArray) {
            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        return words;
    }
    
    private void processarURL(String url) {
        try {
            // Obtenha o conteúdo da página aqui (usando JSoup, por exemplo)
            String conteudo = obterConteudoDaPagina(url);
            
            // Extraia as informações relevantes da página
            String title = extrairTitulo(conteudo);
            String textSnippet = extrairTextSnippet(conteudo);
            Set<String> words = extrairPalavras(conteudo);
            
            // Crie um objeto WebPage
            WebPage webPage = new WebPage(url, title, textSnippet, words);
            
            // Adicione a página ao índice
            for (String palavra : words) {
                palavra = palavra.toLowerCase();
                if (!index.containsKey(palavra)) {
                    index.put(palavra, new HashSet<>());
                }
                index.get(palavra).add(webPage);
            }
            
        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Erro: + e");
        }
    }
    
    public HashSet<WebPage> getWebPages(String palavra) {
        palavra = palavra.toLowerCase();
        return index.getOrDefault(palavra, new HashSet<>());
    }
    
    public String getTitle(String url) {
        for (HashSet<WebPage> pages : index.values()) {
            for (WebPage page : pages) {
                if (page.getUrl().equals(url)) {
                    return page.getTitle();
                }
            }
        }
        return null;
    }
    
    public String getTextSnippet(String url) {
        for (HashSet<WebPage> pages : index.values()) {
            for (WebPage page : pages) {
                if (page.getUrl().equals(url)) {
                    return page.getTextSnippet();
                }
                
            }
        }
        return null;
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

class WebPage {
    private String url;
    private String title;
    private String textSnippet;
    private Set<String> words;
    
    public WebPage(String url, String title, String textSnippet, Set<String> words) {
        this.url = url;
        this.title = title;
        this.textSnippet = textSnippet;
        this.words = words;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTextSnippet() {
        return textSnippet;
    }
    
    public void setTextSnippet(String textSnippet) {
        this.textSnippet = textSnippet;
    }
    
    public Set<String> getWords() {
        return words;
    }
    
    public void setWords(Set<String> words) {
        this.words = words;
    }
}



