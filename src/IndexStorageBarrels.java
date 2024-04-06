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
import java.util.HashSet;
import java.util.Properties;
import java.util.List;

public class IndexStorageBarrels extends UnicastRemoteObject implements RMIBarrelInterface{
    private final int id;
    private final String barrelsHostName;
    private final int barrelsPort;
    private final String barrelsRMIRegister;
    private final int multPort;
    private final String multAddress;
    private final ArrayList<Barrel> barrelsThreads;
    //private final MulticastSocket socket;
    //private final InetAddress group;
    private RMIBarrelInterface barrel;
    
    public IndexStorageBarrels(int id, String host, int port, String rmiRegister, int multPort, String multAddress) throws Exception {
        super();
        this.id = id;
        this.barrelsThreads = new ArrayList<>();
        this.barrelsHostName = host;
        this.barrelsPort = port;
        this.barrelsRMIRegister = rmiRegister;
        
    
        //LocateRegistry.createRegistry(port);
        this.multPort = multPort;
        this.multAddress = multAddress;
        //this.socket = new MulticastSocket(multPort);
        //this.group = InetAddress.getByName(multAddress);
        //this.socket.joinGroup(new InetSocketAddress(group, multPort), NetworkInterface.getByIndex(0));
    
        try {
            Registry r = LocateRegistry.createRegistry(port);
            System.setProperty("java.rmi.server.hostname", host);
            r.rebind(rmiRegister, this);
        
            //r.rebind(rmiRegister, mainBarrel);
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
        
        /*
        String message = "OIIIIIIIIIIIIIII";
        //while (true) {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multPort);
        //System.out.println("Enviando pacote");
        this.socket.send(packet);
        System.out.println("Pacote enviado");
        
        Thread.sleep(1000); // Aguarda 1 segundo antes de enviar a próxima mensagem
        //}
        //Naming.rebind(rmiRegister, (RMIBarrelInterface)this);
        //byte[] buffer = new byte[256];
        //DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        //socket.receive(packet);
    
        //System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
        //String message = new String(packet.getData(), 0, packet.getLength());
        //System.out.println("ududhdhdhd");
        //System.out.println(message);
        */
        //start();
    }
    
    
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
    
            IndexStorageBarrels mainBarrel = new IndexStorageBarrels(0, rmiHost, rmiPort, rmiRegister, multPort, multAddress);
    
            for (int i = 1; i < 5; i++) {
        
                if (multAddress == null || multPort == 0) {
                    System.out.println("[BARREL " + i + "] Erro ao ler as propriedades do ficheiro de configuração.");
                    System.exit(1);
                }
        
                //File linkfile = new File("src/main/java/com/ProjetoSD/links-" + i);
                //File wordfile = new File("src/main/java/com/ProjetoSD/words-" + i);
                //File infofile = new File("src/main/java/com/ProjetoSD/info-" + i);
        
                //Database files = new Database();
                Barrel barrel_t = new Barrel(i, multPort, multAddress);
                mainBarrel.barrelsThreads.add(barrel_t);
                barrel_t.start();
            }
            
        } catch (Exception e) {
            System.out.println("[INDEX-STORAGE-BARRELS] Erro: " + e);
        }
    }
    
    public HashMap<String, ArrayList<String>> pesquisarLinks(String s) throws RemoteException {
        Barrel barrel = this.selecionarBarrel();
        if (barrel == null) {
            HashMap<String, ArrayList<String>> result = new HashMap<>();
            result.put("Erro", new ArrayList<>());
            return result;
        }
    
        return barrel.obterLinks(s);
    }
    
    public List<String> getBarrelsList() {
        List<String> barrelNames = new ArrayList<>();
        for (Barrel barrel : barrelsThreads) {
            barrelNames.add("Barrel " + barrel.getId());
        }
        return barrelNames;
    }
    
    public boolean alive() throws RemoteException {
        Barrel barrel = this.selecionarBarrel();
        return barrel != null;
    }
    
    public Barrel selecionarBarrel() {
        // escolhe um barril aleatório para executar a tarefa
        if (this.barrelsThreads.size() == 0) {
            System.out.println("[BARREL-INTERFACE] Nenhum barrel disponivel. À espera que um barrel fique disponivel...");
            // wait for a short period and try again
            try {
                Thread.sleep(1000); // wait for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selecionarBarrel(); // recursive call to try again
        }
        
        int random = (int) (Math.random() * this.barrelsThreads.size());
        
        // verificar se o barril está vivo, se não estiver remover da lista e selecionar outro barril
        if (!this.barrelsThreads.get(random).isAlive()) {
            System.out.println("[BARREL-INTERFACE] Barrel " + random + " não está vivo. A remover da lista...");
            this.barrelsThreads.remove(random);
            return this.selecionarBarrel();
        }
        return this.barrelsThreads.get(random);
    }
    
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
                        ei.printStackTrace();
                        return;
                    }
                }
            }
        }
    }
}
