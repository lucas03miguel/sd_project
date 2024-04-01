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
    private final int totalDownloaders;
    private final HashMap<String, HashSet<WebPage>> index;
    
    
    public Downloader(int id, int MULTICAST_PORT, String MULTICAST_ADDRESS, int totalDownloaders) throws Exception {
        this.socket = null;
        this.group = null;
        this.multicastPort = MULTICAST_PORT;
        this.multicastAddress = MULTICAST_ADDRESS;
        
        this.idDownloader = id;
        this.totalDownloaders = totalDownloaders;
        
        this.urlQueue = (URLQueueInterface) Naming.lookup("URLQUEUE");
        this.index = new HashMap<>();
        System.out.println("Download criado com sucesso");
        start();
    }
    
    public void run() {
        while (true) {
            try {
                String url = null;
                synchronized (urlQueue) {
                    if (!urlQueue.isEmpty()) {
                        List<String> urls = urlQueue.getUrlQueue();
                        int index;
                        if (urls.isEmpty()) index = 0;
                        else index = idDownloader % urls.size();
                        url = urls.get(index);
                        urlQueue.removerLink(url);
                    }
                }
                
                if (url != null) {
                    System.out.println("Processando URL: " + url);
                    processarURL(url);
                    printIndex();
                } else {
                    sleep(1000);
                }
            } catch (Exception e) {
                System.out.println("[DOWNLOADER] Erro: " + e);
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
            
            int totalDownloaders = 50; // Número total de downloaders
            for (int i = 1; i <= totalDownloaders; i++) {
                new Downloader(i, port, address, totalDownloaders);
            }
            //new Downloader(1, port, address, 5);
            
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
    
    private String extrairTitulo(String conteudo) {
        Document document = Jsoup.parse(conteudo);
        Element titleElement = document.selectFirst("title");
        return titleElement != null ? titleElement.text() : "";
    }
    
    private String extrairCitacao(String conteudo) {
        Document document = Jsoup.parse(conteudo);
        Elements paragraphs = document.select("p");
        if (!paragraphs.isEmpty()) {
            return paragraphs.first().text();
        }
        return "";
    }
    
    private Set<String> extrairPalavras(String conteudo) {
        HashSet<String> words = new HashSet<>();
        Document document = Jsoup.parse(conteudo);
        String text = document.text();
        String[] wordArray = text.split("\\s+");
        for (String word : wordArray) {
            word = word.replaceAll("[^\\p{L}]", "").toLowerCase();
            if (word.length() > 2) {
                words.add(word);
            }
        }
        return words;
    }
    
    public void printIndex() {
        for (Map.Entry<String, HashSet<WebPage>> entry : index.entrySet()) {
            String word = entry.getKey();
            HashSet<WebPage> pages = entry.getValue();
            System.out.println("Palavra: " + word);
            /*
            System.out.println(" Páginas:");
            for (WebPage page : pages) {
                System.out.println("  URL: " + page.getUrl());
                System.out.println("  Título: " + page.getTitle());
                System.out.println();
            }
            
             */
            System.out.println("-----------------------------");
        }
    }
    
    private void processarURL(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://".concat(url);
        }
        
        try {
            Document document = Jsoup.connect(url).get();
            String conteudo = document.html();
            
            String title = extrairTitulo(conteudo);
            String textSnippet = extrairCitacao(conteudo);
            Set<String> words = extrairPalavras(conteudo);
            
            WebPage webPage = new WebPage(url, title, textSnippet, words);
            
            for (String palavra : words) {
                palavra = palavra.toLowerCase();
                if (!index.containsKey(palavra)) {
                    index.put(palavra, new HashSet<>());
                }
                index.get(palavra).add(webPage);
            }
            
            /*
            Elements links = document.select("a[href]");
            for (Element link : links) {
                String linkUrl = link.attr("abs:href");
                urlQueue.inserirLink(linkUrl);
            }
            
             */
            
        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Erro: " + e);
        }
    }
    
    /*
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
    
     */
    
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



