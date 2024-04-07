package src;

import interfaces.URLQueueInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * A classe Downloader representa uma thread responsável por baixar e processar páginas web.
 * Esta conecta-se a uma fila de URLs por RMI e envia mensagens para os barrels usando multicast.
 */
public class Downloader extends Thread implements Remote {
    /**
     * Porta multicast.
     */
    private final int multicastPort;
    /**
     * Grupo multicast.
     */
    private final InetAddress group;
    /**
     * Socket multicast.
     */
    private final MulticastSocket socket;
    /**
     * ID do downloader.
     */
    private final int idDownloader;
    /**
     * Nome da fila de URLs no registo RMI.
     */
    private final String urlQueueName;
    /**
     * Interface da fila de URLs.
     */
    private URLQueueInterface urlQueue;
    /**
     * Semáforo para sincronização das mensagens a enviar por multicast.
     */
    private final Semaphore sem;

    /**
     * Construtor da classe Downloader.
     *
     * @param id ID do downloader
     * @param multPort porta multicast
     * @param multAddress endereço multicast
     * @param URLQueueName nome da fila de URLs no registo RMI
     * @param sem semáforo para sincronização das mensagens a enviar por multicast
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
        this.urlQueueName = URLQueueName;
    
        System.out.println("Downloader " + id + " criado com sucesso");
    }

     /**
     * Método para iniciar e correr o downloader.
     * Processa os URLs da fila de forma contínua.
     */
    public void run() {
        while (true) {
            try {
                String url = null;
                if (!urlQueue.isEmpty()) {
                    List<String> urls = urlQueue.getUrlQueue();
                    int index;
                    if (urls.isEmpty()) index = 0;
                    else index = idDownloader % urls.size();
                    url = urls.get(index);
                    urlQueue.removerLink(url);
                }
                
                if (url != null) {
                    System.out.println("Processando URL: " + url);
                    processarURL(url);
                    
                } else {
                    sleep(1000);
                }
            } catch (Exception e) {
                System.out.println("[DOWNLOADER] Erro: " + e);
                try {
                    this.urlQueue = (URLQueueInterface) Naming.lookup(urlQueueName);
                } catch (NotBoundException | MalformedURLException | RemoteException ex) {
                    System.out.println("[DOWNLOADER] Erro ao procurar URLQueue: " + ex);
                }
            }
        }
    }
    
    /**
     * Método principal para iniciar os downloaders.
     *
     * @param args argumentos de linha de comando (não utilizados)
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
            
        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Erro: " + e);
        }
        assert d != null;
        d.cleanup();
    }

    /**
     * Extrai o título da página web.
     *
     * @param doc documento Jsoup da página web
     * @return título da página web
     */
    private String extrairTitulo(Document doc) {
        String title = doc.title();
        return title != null ? title : "Sem titulo";
    }
    
    /**
     * Extrai uma citação da página web.
     *
     * @param doc documento Jsoup da página web
     * @return citação da página web
     */
    private String extrairCitacao(Document doc) {
        Elements paragraphs = doc.select("p");
        return !paragraphs.isEmpty() ? paragraphs.first().text() : "";
    }
    
    /**
     * Extrai as palavras da página web.
     *
     * @param doc documento Jsoup da página web
     * @return conjunto de palavras da página web
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
     
     /**
     * Processa um URL extraindo informações da página web e enviando-as para os barris.
     *
     * @param url URL a ser processada
     */
    private void processarURL(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://".concat(url);
        
        ArrayList<String> Links = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();
    
            String title = extrairTitulo(document);
            String textSnippet = extrairCitacao(document);
            Set<String> words = extrairPalavras(document);
    
            WebPage webPage = new WebPage(url, title, textSnippet, words);
            
            Elements links = document.select("a[href]");
            for (Element link : links) {
                String linkUrl = link.attr("abs:href");
                if (!Links.contains(linkUrl)) {
                    linkUrl = linkUrl.replaceAll("[\n;|]+", "");
                    Links.add(linkUrl);
                    
                    boolean linkInserido = false;
                    while (!linkInserido) {
                        try {
                            urlQueue.inserirLink(linkUrl);
                            linkInserido = true;
                        } catch (RemoteException e) {
                            System.out.println("[DOWNLOADER] Erro ao inserir link na fila: " + e);
                            System.out.println("[DOWNLOADER] Tentando novamente...");
                            this.urlQueue = (URLQueueInterface) Naming.lookup(urlQueueName);
                        }
                    }
    
                }
            }
            enviarParaBarrels(webPage, Links);
            
        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Erro: " + e);
        }
    }
    
    /**
     * Envia as informações da página web para os barrels.
     *
     * @param webPage página web processada
     * @param links links encontrados na página web
     */
    private void enviarParaBarrels(WebPage webPage, ArrayList<String> links) {
        Set<String> listaPalavras = webPage.getWords();
        String url = webPage.getUrl();
        String title = webPage.getTitle();
        System.out.println("[DOWNLOADER] Enviando mensagem");
        
        String message = "type | links; url | " + url + "; links_count | " + links.size();
        for (String link : links)
            message = message.concat("; link | " + link);
        sendMessage(message);
        
        message = "type | words; url | " + url + "; words_count | " + listaPalavras.size();
        for (String word : listaPalavras)
            message = message.concat("; word | " + word);
        sendMessage(message);
        
        if (webPage.getTextSnippet().equals("")) webPage.setTextSnippet("Sem citacao");
        if (webPage.getTitle().equals("")) webPage.setTitle("Sem titulo");
        message = "type | textSnippet; url | " + url + "; title | " + title + "; textSnippet | " + webPage.getTextSnippet();
        sendMessage(message);
    }
    
    /**
     * Envia uma mensagem para os barrels usando multicast.
     *
     * @param message mensagem a ser enviada
     */
    private void sendMessage(String message) {
        try {
            this.sem.acquire();
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);
            
            this.socket.send(packet);
            this.sem.release();
        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Erro ao enviar mensagem: " + e);
            this.sem.release();
        }
    }
    
    /**
    * Fecha o socket multicast caso esteja aberto.
    */
    public void cleanup() {
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
        }
    }
}

/**
 * A classe WebPage guarda as informações de uma página web tal como URL, título, palavras e citação.
 */
class WebPage {
    /**
     * URL da página web.
     */
    private final String url;
    /**
     * Título da página web.
     */
    private String title;
    /**
     * Citação da página web.
     */
    private String textSnippet;
    /**
     * Palavras da página web.
     */
    private final Set<String> words;
    
    /**
     * Construtor da classe WebPage.
     *
     * @param url URL da página web
     * @param title título da página web
     * @param textSnippet citação da página web
     * @param words palavras da página web
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
     * Obtém o URL da página web.
     *
     * @return URL da página web
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Obtém o título da página web.
     *
     * @return título da página web
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Define o título da página web.
     *
     * @param title título da página web
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Obtém a citação da página web.
     *
     * @return citação da página web
     */
    public String getTextSnippet() {
        return textSnippet;
    }
    
    /**
     * Define a citação da página web.
     *
     * @param textSnippet citação da página web
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
}