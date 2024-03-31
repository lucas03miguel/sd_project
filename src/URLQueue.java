package src;

import src.interfaces.URLQueueInterface;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class URLQueue extends UnicastRemoteObject implements URLQueueInterface, Serializable {
    private final Queue<String> urlQueue;
    
    public URLQueue() throws RemoteException {
        super();
        this.urlQueue = new LinkedList<>();
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
            if (!isValidURL(link)) {
                return "URL invalido";
            }
            
            if (this.urlQueue.contains(link)) {
                return "URL já existe na fila";
            }
            
            // Adiciona o URL à fila
            this.urlQueue.add(link);
            return "URL valido";
        } catch (Exception e) {
            return "Erro ao indexar URL";
        }
    }
    
    
    public boolean isEmpty() throws RemoteException {
        return this.urlQueue.isEmpty();
    }
    
    public List<String> getUrlQueue() throws RemoteException {
        return new ArrayList<>(urlQueue);
    }
}
