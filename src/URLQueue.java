/**
 * @author Lucas e Simão
 */

package src;

import interfaces.URLQueueInterface;

import java.io.FileInputStream;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A classe URLQueue representa uma fila de URLs implementada como um serviço RMI.
 * Esta permite inserir, remover e obter informações sobre os URLs na fila.
 */
public class URLQueue extends UnicastRemoteObject implements URLQueueInterface {
    /**
     * queue que guarda os urls
     */
    private final BlockingQueue<String> urlQueue;
    
    /**
     * Construtor da classe URLQueue.
     *
     * @param urlQueueName o nome da fila de URLs no registo RMI
     * @param urlQueuePort a porta do registo RMI
     * @throws RemoteException se ocorrer um erro durante a criação do objeto remoto
     */
    public URLQueue(String urlQueueName, int urlQueuePort) throws RemoteException {
        super();
        this.urlQueue = new LinkedBlockingQueue<>();
        
        try {
            LocateRegistry.createRegistry(urlQueuePort);
            Naming.rebind(urlQueueName, this);
            
        } catch (Exception e) {
            System.out.println("[URLQUEUE] Erro: " + e);
        }
    }
    
    /**
     * Método principal para inicializar a fila de URLs.
     *
     * @param args os argumentos de linha de comando (não utilizado)
     */
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        Properties prop = new Properties();
        String SETTINGS_PATH = "./properties/configuration.properties";
    
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
    
    /**
     * Formata um URL adicionando "http://" ou "https://" ou "www." se necessário.
     *
     * @param urlString URL a ser formatado
     * @return URL formatado
     */
    public static String formatURL(String urlString) {
        if (!urlString.contains("http://") && !urlString.contains("https://")) {
            urlString = "https://" + urlString;
        }
    
        if (!urlString.contains("www.")) {
            urlString = urlString.replace("://", "://www.");
        }
    
        return urlString;
    }
    
    /**
     * Verifica se um URL é válido.
     *
     * @param url URL a ser verificado
     * @return true se o URL for válido, false caso contrário
     */
    public static boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Insere um link na fila de URLs.
     *
     * @param link link a ser inserido
     * @return mensagem indicando o resultado da operação
     * @throws RemoteException se ocorrer um erro durante a chamada remota
     */
    public String inserirLink(String link) throws RemoteException {
        String[] separacao = link.split("\\.");
        if (separacao.length < 2) return "URL invalido";
        if (separacao[0].equals("www") && separacao.length == 2) return "URL invalido";
        if (separacao[0].contains("www") && separacao.length < 3) return "URL invalido";
        if ((separacao[0].equals("https://") || separacao[0].equals("http://")) && separacao.length == 2) return "URL invalido";
        
        try {
            link = formatURL(link);
            if (!isValidURL(link)) return "URL invalido";
            else if (this.urlQueue.contains(link)) return "URL já existe na fila";
            
            this.urlQueue.put(link);
            System.out.println("URL adicionado: " + link);
            return "URL valido";
        } catch (Exception e) {
            return "Erro ao indexar URL";
        }
    }
    
    /**
     * Remove um link da fila de URLs.
     *
     * @param url link a ser removido
     * @throws RemoteException se ocorrer um erro durante a chamada remota
     */
    public void removerLink(String url) throws RemoteException {
        try {
            boolean res = this.urlQueue.remove(url);
        } catch (Exception e) {
            System.out.println("Erro: " + e);
        }
    }
    
    /**
     * Obtém o tamanho da fila de URLs.
     *
     * @return tamanho da fila
     * @throws RemoteException se ocorrer um erro durante a chamada remota
     */
    @Override
    public int size() throws RemoteException{
        return this.urlQueue.size();
    }
    
    /**
     * Verifica se a fila de URLs está vazia.
     *
     * @return true se a fila estiver vazia, false caso contrário
     * @throws RemoteException se ocorrer um erro durante a chamada remota
     */
    @Override
    public boolean isEmpty() throws RemoteException {
        return this.urlQueue.isEmpty();
    }
    
    /**
     * Obtém a lista de URLs na fila.
     *
     * @return lista de URLs
     * @throws RemoteException se ocorrer um erro durante a chamada remota
     */
    public List<String> getUrlQueue() throws RemoteException {
        return new ArrayList<>(urlQueue);
    }
}