/**
 * @author Lucas e Simão
 */
package src;

import interfaces.RMIBarrelInterface;

import java.io.FileInputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * A classe IndexStorageBarrels representa um indexador de barrels implementado como um serviço RMI.
 * Permite pesquisar links, obter a lista de barrels, obter as pesquisas mais populares e o tempo medio de pesquisa.
 */
public class IndexStorageBarrels extends UnicastRemoteObject implements RMIBarrelInterface {
    /**
     * id do index
     */
    private final int id;
    /**
     * lista de barrels
     */
    private final ArrayList<Barrel> barrelsThreads;
    /**
     * interface do barrel
     */
    private RMIBarrelInterface barrel;
    /**
     * numero de pesquisas
     */
    private HashMap<Integer, Integer> nPesquisas;
    
    /**
     * Construtor da classe IndexStorageBarrels.
     *
     * @param id id do index
     * @param host host do registo RMI
     * @param port porta do registo RMI
     * @param rmiRegister nome do registo RMI
     * @throws Exception se ocorrer um erro durante a criação do objeto remoto
     */
    public IndexStorageBarrels(int id, String host, int port, String rmiRegister) throws Exception {
        super();
        this.id = id;
        this.barrelsThreads = new ArrayList<>();
        this.nPesquisas = new HashMap<>();
    
        try {
            Registry r = LocateRegistry.createRegistry(port);
            System.setProperty("java.rmi.server.hostname", host);
            r.rebind(rmiRegister, this);
        
            System.out.println("[BARREL-INTERFACE] BARREL RMI criado em: " + host + ":" + port + "->" + rmiRegister);
        
        } catch (RemoteException e) {
            System.out.println("[BARREL-INTERFACE] RemoteException, não foi possível criar o registry. A tentar novamente em 1 segundo...");
        
            try {
                Thread.sleep(1000);
                this.barrel = (RMIBarrelInterface) LocateRegistry.getRegistry(host, port).lookup(rmiRegister);
                this.tentarNovamente(host, port, rmiRegister);
            } catch (InterruptedException | NotBoundException | RemoteException ei) {
                System.out.println("[INDEX-STORAGE-BARRELS]" + ei);
            }
        }
    }
    
    /**
     * Método principal para inicializar o indexador de barrels.
     *
     * @param args argumentos de linha de comando (não utilizado)
     */
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        Properties prop = new Properties();
        String SETTINGS_PATH = "properties/configuration.properties";
        
        try {
            prop.load(new FileInputStream(SETTINGS_PATH));
    
            int multPort = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
            String multAddress = prop.getProperty("MULTICAST_ADDRESS");
            
            int rmiPort = Integer.parseInt(prop.getProperty("PORT_BARRELS"));
            String rmiHost = prop.getProperty("HOST_BARRELS");
            String rmiRegister = prop.getProperty("RMI_REGISTRY_NAME_BARRELS");
    
            IndexStorageBarrels mainBarrel = new IndexStorageBarrels(0, rmiHost, rmiPort, rmiRegister);
            
            int nBarrels = Integer.parseInt(prop.getProperty("N_BARRELS"));
            for (int i = 1; i < nBarrels + 1; i++) {
        
                if (multAddress == null || multPort == 0) {
                    System.out.println("[BARREL " + i + "] Erro ao ler as propriedades do ficheiro de configuração.");
                    System.exit(-1);
                }
                
                Barrel barrel_t = new Barrel(i, multPort, multAddress);
                mainBarrel.barrelsThreads.add(barrel_t);
                barrel_t.start();
            }
            
        } catch (Exception e) {
            System.out.println("[INDEX-STORAGE-BARRELS] Erro: " + e);
            System.out.println("oi");
        }
    }
    
    /**
     * Método para pesquisar links.
     *
     * @param s string a pesquisar
     * @param id id do barrel selecionado
     * @return lista de links
     * @throws RemoteException se ocorrer um erro durante a execução de um método remoto
     */
    @Override
    public HashMap<String, ArrayList<String>> pesquisarLinks(String s, int id) throws RemoteException {
        Barrel b = this.getBarrel(id);
        if (b == null) {
            HashMap<String, ArrayList<String>> result = new HashMap<>();
            result.put("Erro", null);
            return result;
        }
        
        HashMap<String, ArrayList<String>> result = b.obterLinks(s);
        this.nPesquisas = b.getNPesquisas();
        return result;
    }
    
    /**
     * Método para obter a lista de barrels.
     *
     * @return lista de barrels
     * @throws RemoteException se ocorrer um erro durante a execução de um método remoto
     */
    @Override
    public List<String> obterListaBarrels() throws RemoteException {
        List<String> barrelNames = new ArrayList<>();
        for (Barrel barrel : barrelsThreads) {
            barrelNames.add("Barrel " + barrel.getId());
        }
        return barrelNames;
    }
    
    /**
     * Método para obter o top 10 de pesquisas.
     *
     * @return hasmap com o top 10 de pesquisas
     * @throws RemoteException se ocorrer um erro durante a execução de um método remoto
     */
    @Override
    public HashMap<String, Integer> obterTopSearches(int id) throws RemoteException {
        Barrel barrel = this.getBarrel(id);
        if (barrel == null) {
            HashMap<String, Integer> result = new HashMap<>();
            result.put("Erro", null);
            return result;
        }
        return barrel.obterTopSearches();
    }
    
    /**
     * Método para obter o numero de pesquisas por barrel
     *
     * @return hasmap com o numero de pesquisas por barrel
     */
    @Override
    public HashMap<Integer, Integer> getNPesquisas() throws RemoteException {
        return this.nPesquisas;
    }
    
    /**
     * Método para retornar o id do index.
     * @return id do index
     * @throws RemoteException se ocorrer um erro durante a execução de um método remoto
     */
    @Override
    public int getId() throws RemoteException {
        return id;
    }
    
    /**
     * Método para obter as ligações de um link.
     *
     * @param id id do barrel
     * @param link link a pesquisar
     * @return lista de ligações
     * @throws RemoteException se ocorrer um erro durante a execução de um método remoto
     */
    @Override
    public ArrayList<String> obterLigacoes(int id, String link) throws RemoteException {
        Barrel barrel = this.getBarrel(id);
        if (barrel == null) {
            ArrayList<String> result = new ArrayList<>();
            result.add("Erro");
            return result;
        }
        return barrel.obterLigacoes(link);
    }
    
    /**
     * Método para retornar o barrel com o id especificado.
     *
     * @param id id do barrel
     * @return barrel com o id especificado
     */
    public Barrel getBarrel(int id) {
        for (Barrel barrel : barrelsThreads) {
            if (barrel.getIdBarrel() == id) {
                return barrel;
            }
        }
        return null;
    }
    
    /**
     * Método para verificar se um barrel está vivo.
     *
     * @return true se o barrel estiver vivo, false caso contrário
     * @throws RemoteException se ocorrer um erro durante a execução de um método remoto
     */
    @Override
    public boolean alive() throws RemoteException {
        int id = this.selecionarBarrel();
        Barrel barrel = this.getBarrel(id);
        return barrel != null;
    }
    
    /**
     * Método para selecionar um barrel.
     *
     * @return id do barrel selecionado
     */
    @Override
    public int selecionarBarrel() {
        if (this.barrelsThreads.size() == 0) {
            System.out.println("[BARREL-INTERFACE] Nenhum barrel disponivel. À espera que um barrel fique disponivel...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("[BARREL-INTERFACE] Erro: " + e);
            }
            return selecionarBarrel();
        }
        
        int random = (int) (Math.random() * this.barrelsThreads.size());
        if (!this.barrelsThreads.get(random).isAlive()) {
            System.out.println("[BARREL-INTERFACE] Barrel " + random + " não está vivo. A remover da lista...");
            this.barrelsThreads.remove(random);
            return this.selecionarBarrel();
        }
        return this.barrelsThreads.get(random).getIdBarrel();
    }
    
    /**
     * Método para tentar novamente a ligação aos barrels.
     *
     * @param rmiHost host do registo RMI
     * @param rmiPort porta do registo RMI
     * @param rmiRegister nome do registo RMI
     * @throws RemoteException se ocorrer um erro durante a execução de um método remoto
     */
    private void tentarNovamente(String rmiHost, int rmiPort, String rmiRegister) throws RemoteException {
        while (true) {
            try {
                if (this.barrel != null && this.barrel.alive()) {
                    System.out.println("[BARREL] Connection to RMI server reestablished");
                    break;
                }
            } catch (RemoteException e) {
                System.out.println("[Erro] " + e + ". A tentar novamente em 1 segundo...");
                for (int i = 0; i < 15; i++) {
                    try {
                        Thread.sleep(1000);
                        this.barrel = (RMIBarrelInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegister);
                    } catch (RemoteException er) {
                        System.out.println("[EXCEPTION] Erro" + er);
                        this.barrel = null;
                    } catch (Exception ei) {
                        System.out.println("[EXCEPTION] Erro: " + ei);
                        return;
                    }
                }
            }
        }
    }
}
