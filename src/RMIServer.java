/**
 * @author Lucas e Simão
 */
package src;

import interfaces.RMIBarrelInterface;
import interfaces.RMIServerInterface;
import interfaces.URLQueueInterface;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import static java.lang.Thread.sleep;

/**
 * A classe RMIServer representa um servidor RMI que permite indexar URLs e pesquisar links.
 * Implementa a interface RMIServerInterface.
 */
public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {
    /**
     * Nome do registo da queue de url
     */
    private final String urlName;
    /**
     * Interface da queue de url
     */
    private URLQueueInterface urlQueue;
    /**
     * Interface do barrel
     */
    private RMIBarrelInterface barrel;
    /**
     * Porta do registo RMI do barrel
     */
    private final int barrelRMIPort;
    /**
     * Host do registo RMI do barrel
     */
    private final String barrelRMIHost;
    /**
     * Nome do registo RMI do barrel
     */
    private final String barrelRMIRegistryName;
    /**
     * HasMap com os tempos médios de pesquisa
     */
    HashMap<Integer, Double> temposMedios;
    
    /**
     * Construtor da classe RMIServer.
     *
     * @param rmiPort porta do registo RMI
     * @param rmiHost host do registo RMI
     * @param rmiRegistryName nome do registo RMI
     * @param urlQueueRegistryName nome do registo RMI da queue de url
     * @param barrelRMIPort porta do registo RMI do barrel
     * @param barrelRMIHost host do registo RMI do barrel
     * @param barrelRMIRegistryName nome do registo RMI do barrel
     * @throws RemoteException se ocorrer um erro durante a criação do objeto remoto
     */
    public RMIServer(int rmiPort, String rmiHost, String rmiRegistryName, String urlQueueRegistryName, int barrelRMIPort, String barrelRMIHost, String barrelRMIRegistryName) throws RemoteException {
        super();
        this.barrelRMIPort = barrelRMIPort;
        this.barrelRMIHost = barrelRMIHost;
        this.barrelRMIRegistryName = barrelRMIRegistryName;
        this.temposMedios = new HashMap<>();
        this.urlName = urlQueueRegistryName;
        
        while (true) {
            try {
                Registry r = LocateRegistry.createRegistry(rmiPort);
                System.setProperty("java.rmi.server.hostname", rmiHost);
                r.rebind(rmiRegistryName, this);
                System.out.println("[SERVER] A correr em " + rmiHost + ":" + rmiPort + "->" + rmiRegistryName);
                
                while (true) {
                    try {
                        this.barrel = (RMIBarrelInterface) LocateRegistry.getRegistry(barrelRMIHost, barrelRMIPort).lookup(barrelRMIRegistryName);
                        System.out.println("[SERVER] Got barrel registry on " + barrelRMIHost + ":" + barrelRMIPort + "->" + barrelRMIRegistryName);
                        
                        this.urlQueue = (URLQueueInterface) Naming.lookup(urlQueueRegistryName);
                        System.out.println("[SERVER] Got urlqueue registry on " + urlQueueRegistryName) ;
                        break;
                    } catch (NotBoundException | RemoteException e1) {
                        System.out.println("[EXCEPTION] NotBoundException | RemoteException, could not get barrel Registry: " + e1.getMessage());
                        System.out.println("Current barrel config: " + barrelRMIHost + ":" + barrelRMIPort + " " + barrelRMIRegistryName);
                        
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e2) {
                            System.out.println("[EXCEPTION] InterruptedException: " + e2);
                        }
                    }
                }
                run();
            } catch (Exception e) {
                System.out.println("[EXCEPTION] Nao conseguiu criar registry. A tentar novamente num segundo...");
                try {
                    sleep(1000);
                    LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegistryName);
                    this.tentarNovamente(barrelRMIHost, barrelRMIPort, barrelRMIRegistryName);
                } catch (InterruptedException | NotBoundException e2) {
                    System.out.println("[EXCEPTION] " + e2);
                    return;
                } catch (RemoteException ex) {
                    System.out.println("[EXCEPTION] Nao conseguiu criar registry. A tentar novamente num segundo...");
                }
            }
        }
        
    }
    
    /**
     * Método para correr o servidor.
     */
    public void run() {
        try {
            System.out.println("[SERVER] Server preparado.");
            while (true) ;
        } catch (Exception re) {
            System.out.println("[SERVER] Erro: " + re);
        }
    }
    
    /**
     * Método principal para inicializar o servidor RMI.
     *
     * @param args argumentos de linha de comando (não utilizado)
     */
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        Properties prop = new Properties();
        String SETTINGS_PATH = "properties/configuration.properties";
        try {
            prop.load(new FileInputStream(SETTINGS_PATH));
            int rmiPort = Integer.parseInt(prop.getProperty("PORT_SERVER"));
            String rmiHost = prop.getProperty("HOST_SERVER");
            String rmiRegistryName = prop.getProperty("REGISTRY_NAME_SERVER");
            String urlQueueRegistryName = prop.getProperty("URL_QUEUE_REGISTRY_NAME");
            int barrelRMIPort = Integer.parseInt(prop.getProperty("PORT_BARRELS"));
            String barrelRMIHost = prop.getProperty("HOST_BARRELS");
            String barrelRMIRegistryName = prop.getProperty("RMI_REGISTRY_NAME_BARRELS");
            
            new RMIServer(rmiPort, rmiHost, rmiRegistryName, urlQueueRegistryName, barrelRMIPort, barrelRMIHost, barrelRMIRegistryName);
        } catch (Exception e) {
            System.out.println("[SERVER] Erro: " + e);
        }
    }
    
    /**
     * Método para indexar um URL.
     * @param url URL a indexar
     * @return mensagem de sucesso ou erro
     * @throws RemoteException se ocorrer um erro durante a conexão remota
     */
    @Override
    public String indexar(String url) throws RemoteException {
        System.out.println("[SERVER] Adicionando url à queue: " + url);
        
        boolean urlInserido = false;
        String res = "";
        
        while (!urlInserido) {
            try {
                res = this.urlQueue.inserirLink(url);
                urlInserido = true;
            } catch (RemoteException e) {
                System.out.println("[SERVER] Erro ao inserir URL na fila: " + e);
                System.out.println("[SERVER] Tentando reconectar à fila de URLs...");
                
                // Tenta reconectar à fila de URLs
                boolean reconectado = false;
                while (!reconectado) {
                    try {
                        this.urlQueue = (URLQueueInterface) Naming.lookup(urlName);
                        System.out.println("[SERVER] Reconectado à fila de URLs!");
                        reconectado = true;
                    } catch (RemoteException | NotBoundException | MalformedURLException ex) {
                        System.out.println("[SERVER] Erro ao reconectar à fila de URLs: " + ex);
                        System.out.println("[SERVER] Tentando novamente em 1 segundo...");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            System.out.println("[SERVER] Erro: " + ie);
                        }
                    }
                }
            }
        }
        System.out.println(res);
        
        return res;
    }
    
    /**
     * Método para pesquisar links.
     * @param s string de pesquisa
     * @return HashMap com os links pesquisados
     * @throws RemoteException se ocorrer um erro durante a conexão remota
     */
    @Override
    public HashMap<String, ArrayList<String>> pesquisar(String s) throws RemoteException {
        String[] palavras = s.split(" ");
        long startTime, endTime;
        HashMap<String, ArrayList<String>> resp = new HashMap<>();
        HashMap<String, ArrayList<String>> aux = new HashMap<>();
        HashMap<Integer, Double> tempos = new HashMap<>();
        
        for (String palavra : palavras) {
            boolean pesquisaRealizada = false;
            while (!pesquisaRealizada) {
                try {
                    int idBarrelEscolhido = this.barrel.selecionarBarrel();
                    startTime = System.currentTimeMillis();
                    aux = barrel.pesquisarLinks(palavra, idBarrelEscolhido);
                    endTime = System.currentTimeMillis();
                    pesquisaRealizada = true;
                
                    double tempo = (double) (endTime - startTime);
                    if (!tempos.containsKey(idBarrelEscolhido)) tempos.put(idBarrelEscolhido, tempo);
                    else tempos.put(idBarrelEscolhido, tempos.get(idBarrelEscolhido) + tempo);
                } catch (RemoteException e) {
                    System.out.println("[EXCEPTION] Erro: " + e);
                    try {
                        tentarNovamente(barrelRMIHost, barrelRMIPort, barrelRMIRegistryName);
                    } catch (Exception ex) {
                        System.out.println("[EXCEPTION] Erro: " + ex);
                    }
                }
            }
        }
        HashMap<Integer, Integer> nPesquisas = barrel.getNPesquisas();
        for (Integer id: tempos.keySet()) {
            temposMedios.put(id, tempos.get(id) / nPesquisas.get(id));
        }
        
        for (String url : aux.keySet()) {
            if (resp.containsKey(url)) resp.get(url).addAll(aux.get(url));
            else resp.put(url, aux.get(url));
        }
        return resp;
    }
    
    /**
     * Método para verificar o login de um utilizador.
     * @param username nome de utilizador
     * @param password palavra-passe
     * @return 0 se o login for inválido, 1 se o username for inválido, 2 se o login for válido, -1 se for erro
     * @throws RemoteException se ocorrer um erro durante a conexão remota
     */
    public int checkLogin(String username, String password) throws RemoteException {
        //TODO: Modificar esta shit porque nao esta bem. temos que usar o fucking barril
        int validLogins = 0;
        String filePath = "./database/users.txt";
        
        Path dirPath = Path.of("./database");
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
                System.out.println("Base de dados criada: " + dirPath);
            } catch (Exception e) {
                System.err.println("Erro ao criar a base de dados: " + e);
                return -1;
            }
        }
        
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                System.out.println("Ficheiro de users criado: " + filePath);
            } catch (Exception e) {
                System.err.println("Erro ao criar o ficheiro de users: " + e);
                return -1;
            }
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2 && parts[0].equals(username)) {
                    validLogins += 1;
                    if (parts[1].equals(password))
                        validLogins += 1;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler do ficheiro de users: " + e);
            return -1;
        }
        
        System.out.println(validLogins);
        return validLogins;
    }
    
    /**
     * Método para verificar o registo de um utilizador.
     * @param username nome de utilizador
     * @param password palavra-passe
     * @return mensagem de sucesso ou erro
     * @throws RemoteException se ocorrer um erro durante a conexão remota
     */
    public String checkRegisto(String username, String password) throws RemoteException {
        String filePath = "./database/users.txt";
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(username + " " + password + "\n");
            System.out.println("O user adicionado foi: " + username + " " + password);
            bw.flush();
            System.out.println("User adicionado com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao escrever no ficheiro de users: " + e);
            return "Erro no lado do servidor";
        }
        
        return "User adicionado com sucesso";
    }
    
    /**
     * Método para obter a lista de barrels.
     * @return lista de barrels
     * @throws RemoteException se ocorrer um erro durante a conexão remota
     */
    @Override
    public List<String> obterListaBarrels() throws RemoteException {
        return barrel.obterListaBarrels();
    }
    
    /**
     * Método para obter as top searches.
     * @return HashMap com as top searches
     * @throws RemoteException se ocorrer um erro durante a conexão remota
     */
    @Override
    public HashMap<String, Integer> obterTopSearches() throws RemoteException {
        int id = barrel.selecionarBarrel();
        return this.barrel.obterTopSearches(id);
    }
    
    /**
     * Método para obter os tempos médios de pesquisa.
     * @return HashMap com os tempos médios de pesquisa
     * @throws RemoteException se ocorrer um erro durante a conexão remota
     */
    @Override
    public HashMap<Integer, Double> obterTempos() throws RemoteException {
        return this.temposMedios;
    }
    
    /**
     * Método para tentar reconectar ao barrel.
     *
     * @param rmiHost host do registo RMI do barrel
     * @param rmiPort porta do registo RMI do barrel
     * @param rmiRegistryName nome do registo RMI do barrel
     * @throws NotBoundException se o nome especificado não estiver associado a um objeto remoto
     * @throws RemoteException se ocorrer um erro durante a conexão remota
     * @throws InterruptedException se a thread for interrompida enquanto estiver dormindo
     */
    public void tentarNovamente(String rmiHost, int rmiPort, String rmiRegistryName) throws NotBoundException, RemoteException, InterruptedException {
        while (true) {
            try {
                if (this.barrel.alive()) {
                    System.out.println("[BARREL] Barrel is alive.");
                }
            } catch (Exception e) {
                System.out.println("[SERVER] Getting connection...");
                
                try {
                    Thread.sleep(1000);
                    this.barrel = (RMIBarrelInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegistryName);
                    break;
                } catch (InterruptedException | NotBoundException ei) {
                    System.out.println("[EXCEPTION] InterruptedException: " + ei);
                    return;
                } catch (Exception er) {
                    System.out.println("[EXCEPTION] RemoteException, could not create registry. Retrying in 1 second...");
                }
            }
        }
    }
}
