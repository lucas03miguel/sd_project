package src_teste.lucas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class WebCrawler extends Thread{
    private static QueueInterface q;
    private int PORT;
    private MulticastSocket socket;
    private InetAddress group;

    public static void main(String args[]) {
        WebCrawler wc = new WebCrawler();
    }

    public WebCrawler() {
        try{
            this.PORT = 4321;
            this.socket = new MulticastSocket(); // create socket without binding it (only for sending)
            this.group = InetAddress.getByName("224.3.2.1");
            
            q = (QueueInterface) LocateRegistry.getRegistry(6969).lookup("Queue");
            
            String url = q.getLink();
            getInfoSites(url);
            this.start();

        }
        catch(Exception e){
            System.out.println("WebCrawler err: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void run() {

        while (true) {

            String url = "";
            try {
                url = q.getLink();
                //System.out.println("URL: " + url);
            } catch (Exception e) {
                e.printStackTrace();
            }

            getInfoSites(url);

        }

    }

    public void getInfoSites(String url){
        if ("".equals(url) || url == null) {
            System.out.print("URL is null or empty");
            return;
        }

        try {
            Document doc = Jsoup.connect(url).get();

            // Titulo da página
            String title = doc.title();

            // Descrição da página
            String description = doc.select("meta[name=description]").attr("content");
            if (description.equals("") || description == null) {
                description = "No description";
            }
            
            sendInfo("type LinkInfo | link1 " + url + " | descricao " + description + " | titulo " + title);
            // addInfoLink(url, description, title); //adicionar iformação sobre o site

            // Ter o conteúdo da página e ligar os links às palavras as palavras no HasMap
            String content = doc.text();
            getWord(content, url); // enviar aos barrels a palavra e o link

            // daqui para baixo apenas vamos adicionar os links à queue para mais tarde
            // serem processados pelo crawler
            Elements newLinks = doc.select("a[href]"); // get all links
            for (Element link : newLinks) {
                // firts is the description
                // second is the link
                sendInfo("type LinkLink | link1 " + url + " | link2 " + link.attr("abs:href"));
                // System.out.println("LINK:" + link.text() + "\n" + "LINK2:" +
                // link.attr("abs:href") + "\n");
                q.putLink(link.attr("abs:href")); // add to queue
                
                // File file = new File("urls.txt");
                // FileWriter fw = new FileWriter(file, true);
                // fw.write(link.attr("abs:href") + "\n");
                // fw.close();
            }
                                    
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getWord(String text, String link) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
        String line;
        int numberOFWords = 0;
        while (true) {
            try {
                if ((line = reader.readLine()) == null) {
                    break;
                }
                String[] words = line.split("[ ,;:.?!“”(){}\\[\\]<>']+");

                for (String word : words) {
                    numberOFWords++;
                    if (numberOFWords <= 200 && !word.equals("|")) {
                        sendInfo("type WordLink | word " + word.toLowerCase() + " | link " + link);
                    } else {
                        break;
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que envia a informação atraves de multicast 
     * 
     * @param message - mensagem
     */
    private void sendInfo(String message) {
        try {
            // System.out.println("Sending message: " + message);
            System.out.println(message);
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.group, this.PORT);
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    
}
