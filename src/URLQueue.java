package src;

import interfaces.URLQueueInterface;

import java.io.FileInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class URLQueue extends UnicastRemoteObject implements URLQueueInterface, Serializable {
    private final BlockingQueue<String> urlQueue;
    private URLQueueInterface queue;
    
    
    public URLQueue(String urlQueueName, int urlQueuePort) throws RemoteException {
        super();
        this.urlQueue = new LinkedBlockingQueue<>();
        
        try {
            LocateRegistry.createRegistry(urlQueuePort);
            Naming.rebind("URLQUEUE", this);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        Properties prop = new Properties();
        String SETTINGS_PATH = "properties/configuration.properties";
    
        try {
            prop.load(new FileInputStream(SETTINGS_PATH));
            
            int urlQueuePort = Integer.parseInt(prop.getProperty("URL_QUEUE_RMI_PORT"));
            String urlQueueName = prop.getProperty("URL_QUEUE_REGISTRY_NAME");
            
            new URLQueue(urlQueueName, urlQueuePort);
            System.out.println("URL inicializado com sucesso");
        } catch (Exception e) {
            System.out.println("[URLQUEUE] Erro: " + e);
        }
    }
    
    /*
    @Override
    public String takeLink() throws RemoteException {
        return null;
    }
    
     */
    public static boolean isValidURL(String urlString) {
        try {
            URI uri = new URI(urlString);
            if (uri.getScheme() == null || uri.getHost() == null)
                return false;
        
            URL __ = uri.toURL();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }
    
    public String inserirLink(String link) throws RemoteException {
        try {
            if (!isValidURL(link)) return "URL invalido";
            else if (this.urlQueue.contains(link)) return "URL já existe na fila";
            
            this.urlQueue.put(link);
            System.out.println("URL adicionado: " + link);
            return "URL valido";
        } catch (Exception e) {
            return "Erro ao indexar URL";
        }
    }
    
    
    
    public void removerLink(String url) throws RemoteException {
        try {
            boolean res = this.urlQueue.remove(url);
        } catch (Exception e) {
            System.out.println("Erro: " + e);
        }
    }
    
    @Override
    public int size() throws RemoteException{
        return this.urlQueue.size();
    }
    
    @Override
    public boolean isEmpty() throws RemoteException {
        return this.urlQueue.isEmpty();
    }
    
    public List<String> getUrlQueue() throws RemoteException {
        return new ArrayList<>(urlQueue);
    }
}
