package src;

import interfaces.URLQueueInterface;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.text.Normalizer;
import java.util.*;


public class Downloader extends Thread implements Remote {
    //private final String multicastAddress;
    private int multicastPort;
    private final InetAddress group;
    private MulticastSocket socket;
    private final int idDownloader;
    private final URLQueueInterface urlQueue;
    private final HashMap<String, HashSet<WebPage>> index = new HashMap<>();
    
    
    public Downloader(int id, int multPort, String multAddress, String URLQueueName) throws Exception {
        this.socket = new MulticastSocket();
        this.multicastPort = multPort;
        this.group = InetAddress.getByName(multAddress);
        this.multicastPort = multPort;
        
        
        //this.multicastAddress = MULTICAST_ADDRESS;
        
        this.idDownloader = id;
        
        this.urlQueue = (URLQueueInterface) Naming.lookup(URLQueueName);
        //this.index = new HashMap<>();
        System.out.println("Download criado com sucesso");
    
        /*
        while (true) {
            String message = "testeee";
            byte[] buffer = message.getBytes();
        
            InetAddress group = InetAddress.getByName(multAddress);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multPort);
            System.out.println("Enviando mensagem");
            socket.send(packet);
        
            try { sleep((long) (Math.random() * 2500)); } catch (InterruptedException ignored) { }
        }
        
         */
        
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
                    //printIndex();
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
            
            int port = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
            String address = prop.getProperty("MULTICAST_ADDRESS");
            String urlQueueName = prop.getProperty("URL_QUEUE_REGISTRY_NAME");
            
            
            int totalDownloaders = 50; // Número total de downloaders
            for (int i = 1; i <= totalDownloaders; i++) {
                new Downloader(i, port, address, urlQueueName);
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
    
    private String extrairTitulo(Document doc) {
        String title = doc.title();
        return title != null ? title : "";
    }
    
    private String extrairCitacao(Document doc) {
        Elements paragraphs = doc.select("p");
        return !paragraphs.isEmpty() ? paragraphs.first().text() : "";
    }
    
    private Set<String> extrairPalavras(Document doc) {
        HashSet<String> words = new HashSet<>();
        String text = doc.text();
        String[] wordArray = text.split("\\s+");
        for (String word : wordArray) {
            String normalizedText = Normalizer.normalize(word, Normalizer.Form.NFD);
            normalizedText = normalizedText.replaceAll("\\p{M}", "");
            word = normalizedText.replaceAll("[^a-zA-Z0-9 ]", "");
            if (word.length() > 2) {
                words.add(word);
            }
        }
        return words;
    }
    
    /*
    public void printIndex() {
        
        for (Map.Entry<String, HashSet<WebPage>> entry : index.entrySet()) {
            String word = entry.getKey();
            HashSet<WebPage> pages = entry.getValue();
            System.out.println("Palavra: " + word);
            
            System.out.println(" Páginas:");
            for (WebPage page : pages) {
                System.out.println("  URL: " + page.getUrl());
                System.out.println("  Título: " + page.getTitle());
                System.out.println();
            }
            
            
            System.out.println("-----------------------------");
        }
    }
    
     */
    
    
    private void processarURL(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://".concat(url);
        }
        
        try {
            Document document = Jsoup.connect(url).get();
            
            String title = extrairTitulo(document);
            String textSnippet = extrairCitacao(document);
            Set<String> words = extrairPalavras(document);
            
            WebPage webPage = new WebPage(url, title, textSnippet, words);
            
            /*
            for (String palavra : words) {
                palavra = palavra.toLowerCase();
                if (!index.containsKey(palavra)) {
                    index.put(palavra, new HashSet<>());
                }
                index.get(palavra).add(webPage);
            }
            
            printIndex();
            */
            
            
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



