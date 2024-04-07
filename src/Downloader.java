/**
 * @author Lucas e Simão
 */
package src;

import interfaces.URLQueueInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.Serializable;
import java.net.*;
import java.rmi.Naming;
import java.rmi.Remote;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * A classe Downloader representa um thread responsável por baixar e processar páginas web.
 * Esta conecta-se a uma fila de URLs através de RMI e envia mensagens para os barris usando multicast.
 */
public class Downloader extends Thread implements Remote {
    //private final String multicastAddress;
    private final int multicastPort;
    private final InetAddress group;
    private final MulticastSocket socket;
    private final int idDownloader;
    private final URLQueueInterface urlQueue;
    
    private final Semaphore sem;

    /**
     * Construtor da classe Downloader.
     *
     * @param id o ID do downloader
     * @param multPort a porta multicast
     * @param multAddress o endereço multicast
     * @param URLQueueName o nome da fila de URLs no registro RMI
     * @param sem o semáforo para sincronização
     * @throws Exception se ocorrer um erro durante a inicialização
     */
    
    public Downloader(int id, int multPort, String multAddress, String URLQueueName, Semaphore sem) throws Exception {
        this.sem = sem;
        this.idDownloader = id;
        this.multicastPort = multPort;
        this.socket = new MulticastSocket(multPort);
        this.group = InetAddress.getByName(multAddress);
        this.socket.joinGroup(new InetSocketAddress(group, multicastPort), NetworkInterface.getByIndex(0));
        this.urlQueue = (URLQueueInterface) Naming.lookup(URLQueueName);
    
        System.out.println("Downloader " + id + " criado com sucesso");
        
        /*
        //this.multicastPort = multPort;
        //this.multicastAddress = MULTICAST_ADDRESS;
        //this.index = new HashMap<>();
        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        this.socket.receive(packet);
        
        
        String message = new String(packet.getData(), 0, packet.getLength());
        System.out.println(message);
        
        
        System.out.println("Download " + id + " criado com sucesso");
        
        while (true) {
            String message = "testeee";
            byte[] buffer = message.getBytes();
        
            InetAddress group = InetAddress.getByName(multAddress);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multPort);
            System.out.println("Enviando mensagem");
            socket.send(packet);
        
            try { sleep((long) (Math.random() * 2500)); } catch (InterruptedException ignored) { }
        }
        //start();
        */
    }

     /**
     * Método executado pelo thread do downloader.
     * Processa as URLs da fila de forma contínua.
     */
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
                System.out.println("[DOWNLOADER] Erro 1: " + e);
            }
        }
    }
    /**
     * Método principal para iniciar os downloaders.
     *
     * @param args os argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        Properties prop = new Properties();
        String SETTINGS_PATH = "properties/configuration.properties";
        Downloader d = null;
        try {
            prop.load(new FileInputStream(SETTINGS_PATH));
            
            int port = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
            String address = prop.getProperty("MULTICAST_ADDRESS");
            String urlQueueName = prop.getProperty("URL_QUEUE_REGISTRY_NAME");
    
            Semaphore sem = new Semaphore(1);
            int totalDownloaders = 5; // Número total de downloaders
            for (int i = 1; i <= totalDownloaders; i++) {
                d = new Downloader(i, port, address, urlQueueName, sem);
                d.start();
            }
            //new Downloader(1, port, address, 5);
            
        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Erro 3: " + e);
        }
        assert d != null;
        d.cleanup();
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

    /**
     * Extrai o título da página web.
     *
     * @param doc o documento Jsoup da página web
     * @return o título da página web
     */
    private String extrairTitulo(Document doc) {
        String title = doc.title();
        return title != null ? title : "Sem titulo";
    }
    
    /**
     * Extrai uma citação da página web.
     *
     * @param doc o documento Jsoup da página web
     * @return a citação da página web
     */
    private String extrairCitacao(Document doc) {
        Elements paragraphs = doc.select("p");
        return !paragraphs.isEmpty() ? paragraphs.first().text() : "";
    }
    
    /**
     * Extrai as palavras da página web.
     *
     * @param doc o documento Jsoup da página web
     * @return um conjunto de palavras da página web
     */
    private Set<String> extrairPalavras(Document doc) {
        HashSet<String> words = new HashSet<>();
        String text = doc.text();
        String[] wordArray = text.split("\\s+");
        for (String word : wordArray) {
            String normalizedText = Normalizer.normalize(word, Normalizer.Form.NFD);
            normalizedText = normalizedText.replaceAll("-\\p{M}", "");
            word = normalizedText.replaceAll("[^a-zA-Z0-9 -]", "");
            if (word.length() > 2) {
                words.add(word.toLowerCase());
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
    
     /**
     * Processa uma URL, extraindo informações da página web e enviando para os barris.
     *
     * @param url a URL a ser processada
     */
    private void processarURL(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://".concat(url);
        }
        
        ArrayList<String> Links = new ArrayList<>();
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
            */
            
            //printIndex();
            
            Elements links = document.select("a[href]");
            for (Element link : links) {
                String linkUrl = link.attr("abs:href");
                if (!Links.contains(linkUrl)) {
                    linkUrl = linkUrl.replaceAll("[\n;|]+", "");
                    Links.add(linkUrl);
                    urlQueue.inserirLink(linkUrl);
                }
            }
            
            enviarParaBarrels(webPage, Links);
            
        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Erro 2: " + e);
        }
    }
    
    /**
     * Envia as informações da página web para os barris.
     *
     * @param webPage a página web processada
     * @param links os links encontrados na página web
     */
    private void enviarParaBarrels(WebPage webPage, ArrayList<String> links) {
        Set<String> listaPalavras = webPage.getWords();
        String url = webPage.getUrl();
        String title = webPage.getTitle();
        System.out.println("[DOWNLOADER] Enviando mensagem");
        
        String message = "type | links; url | " + url + "; links_count | " + links.size();
        for (String link : links)
            message = message.concat("; link | " + link);
        //System.out.println("[DOWNLOADER] Enviando links: " + message);
        sendMessage(message);
        
        message = "type | words; url | " + url + "; words_count | " + listaPalavras.size();
        for (String word : listaPalavras)
            message = message.concat("; word | " + word);
        //System.out.println("[DOWNLOADER] Enviando palavras: " + message);
        sendMessage(message);
        
        if (webPage.getTextSnippet().equals("")) webPage.setTextSnippet("Sem citacao");
        if (webPage.getTitle().equals("")) webPage.setTitle("Sem titulo");
        message = "type | textSnippet; url | " + url + "; title | " + title + "; textSnippet | " + webPage.getTextSnippet();
        sendMessage(message);
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
    
    /**
     * Envia uma mensagem para os barris usando multicast.
     *
     * @param message a mensagem a ser enviada
     */
    private void sendMessage(String message) {
        try {
            this.sem.acquire();
            try {
                byte[] buffer = message.getBytes();
                
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);
                
                
                this.socket.send(packet);
            } finally {
                this.sem.release();
            }
        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Erro ao enviar mensagem: " + e);
        }
    }
    
    /**
    * Limpa os recursos utilizados pelo downloader.
    */
    public void cleanup() {
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
        }
    }
    
    /**
     * Obtém a fila de URLs.
     *
     * @return a fila de URLs como uma string
     */
    public String getUrlQueue() {
        return urlQueue.toString();
    }
}

/**
 * A classe WebPage representa uma página web com informações como URL, título, citação e palavras.
 */
class WebPage {
    private String url;
    private String title;
    private String textSnippet;
    private Set<String> words;
    
    /**
     * Construtor da classe WebPage.
     *
     * @param url a URL da página web
     * @param title o título da página web
     * @param textSnippet a citação da página web
     * @param words as palavras da página web
     */
    public WebPage(String url, String title, String textSnippet, Set<String> words) {
        this.url = url.replaceAll("[\n;|]+", "");
        this.title = title.replaceAll("[\n;|]+", "");
        this.textSnippet = textSnippet.replaceAll("[\n;|]+", "");
        Set<String> wordsCopy = new HashSet<>();
        for (String word : words) {
            word = word.replaceAll("[\n;|]+", "");
            wordsCopy.add(word);
        }
        this.words = wordsCopy;
    }
    
    /**
     * Construtor padrão da classe WebPage.
     */
    public WebPage() {
        this.url = "";
        this.title = "";
        this.textSnippet = "";
        this.words = new HashSet<>();
    }
    
    /**
     * Obtém a URL da página web.
     *
     * @return a URL da página web
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Define a URL da página web.
     *
     * @param url a URL da página web
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Obtém o título da página web.
     *
     * @return o título da página web
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Define o título da página web.
     *
     * @param title o título da página web
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Obtém a citação da página web.
     *
     * @return a citação da página web
     */
    public String getTextSnippet() {
        return textSnippet;
    }
    
    /**
     * Define a citação da página web.
     *
     * @param textSnippet a citação da página web
     */
    public void setTextSnippet(String textSnippet) {
        this.textSnippet = textSnippet;
    }
    
    /**
     * Obtém as palavras da página web.
     *
     * @return as palavras da página web
     */
    public Set<String> getWords() {
        return words;
    }
    
    /**
     * Define as palavras da página web.
     *
     * @param words as palavras da página web
     */
    public void setWords(Set<String> words) {
        this.words = words;
    }
}