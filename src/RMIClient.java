package src;

import interfaces.RMIServerInterface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.spi.AbstractResourceBundleProvider;
import java.util.List;
import static java.lang.Thread.sleep;

public class RMIClient extends UnicastRemoteObject {
    private final int keepAliveTime = 5000;
    private final String rmiHost;
    private final int rmiPort;
    private final String rmiRegistryName;
    private Client client;
    private RMIServerInterface serverInterface;
    
    public RMIClient(String rmiHost, int rmiPort, String rmiRegistryName) throws RemoteException, InterruptedException {
        super();
        this.rmiHost = rmiHost;
        this.rmiPort = rmiPort;
        this.rmiRegistryName = rmiRegistryName;
        
        //this.client = client;
        while (true) {
            try {
                System.out.println("[CLIENT] Configuração: " + rmiHost + ":" + rmiPort + "->" + rmiRegistryName);
                this.serverInterface = (RMIServerInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegistryName);
                System.out.println("[CLIENT] Conectado!!!");
                break;
            } catch (Exception e) {
                System.out.println("[CLIENT] Erro ao conectar ao servidor: " + e);
                sleep(1000);
            }
        }
        run();
        //run();
        
    }
    
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        Properties prop = new Properties();
        String SETTINGS_PATH = "properties/configuration.properties";
        
        try {
            prop.load(new FileInputStream(SETTINGS_PATH));
            
            String rmiHost = prop.getProperty("HOST_CLIENT");
            int rmiPort = Integer.parseInt(prop.getProperty("PORT_SERVER"));
            String rmiRegistryName = prop.getProperty("REGISTRY_NAME_SERVER");
            
            //Client client = new Client("Antonio", false);
            //RMIServerInterface svInterface = (RMIServerInterface) Naming.lookup(rmiRegistryName);
            new RMIClient(rmiHost, rmiPort, rmiRegistryName);
        } catch (Exception e) {
            System.out.println("[CLIENT] Erro ao conectar ao servidor: " + e);
        }
    }
    
    public void run(){
        try {
            this.menu();
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Exceção na main: " + e);
        }
    }
    
    private void printMenu(int userType) {
        System.out.println("\n----Menu----");
        
        // login or register
        if (userType == 0) {
            System.out.println("1. Login");
            System.out.println("2. Registar");
        // user logged - main menu
        } else if (userType == 1) {
            System.out.println("1. Pesquisar");
            System.out.println("2. Indexar novo URL");
            System.out.println("3. Lista dos barrels");
            System.out.println("4. Lista dos downloaders");
            System.out.println("5. Top 10 pesquisas");
            System.out.println("6. Logout");
        }
        
        System.out.println("s. Sair");
        System.out.println("------------");
        System.out.print("Opção: ");
    }
    
    private void menu() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            int userType = 0;
            while (true) {
                if (userType == 0) {
                    printMenu(userType);
                    String choice = br.readLine();
                    
                    if (choice.equals("1")) {
                        int resp = login(br);
                        if (resp == -1) {
                            System.out.println("A sair...");
                            System.exit(0);
                            break;
                        } else if (resp == 1) userType = 1;
                        
                    } else if (choice.equals("2")) {
                        int resp = registar(br);
                        if (resp == -1) {
                            System.out.println("A sair...");
                            System.exit(0);
                            break;
                        } else if (resp == 1) userType = 1;
                        
                    } else if (choice.equalsIgnoreCase("s")) {
                        System.out.println("A sair...");
                        System.exit(0);
                        break;
                    } else {
                        System.out.println("Escolha inválida!");
                        continue;
                    }
                }
                
                if (userType == 1) {
                    printMenu(userType);
                    String choice = br.readLine();
                    
                    switch (choice) {
                        case "1" -> {
                            System.out.println("<----Pesquisar---->");
                            System.out.print("Insira pesquisa: ");
                            String searchQuery = br.readLine();
                            while (searchQuery.length() < 3) {
                                System.out.print("Pesquisa inválida (3+ carateres): ");
                                searchQuery = br.readLine();
                            }
                            HashMap<String, HashSet<String>> resp = serverInterface.pesquisar(searchQuery);
                            System.out.println();
                            int i = 0;
                            int tamanho = resp.size();
                            for (String link : resp.keySet()) {
                                System.out.println("Link: " + link);
                                System.out.println("Title: " + resp.get(link).toArray()[1]);
                                System.out.println("Text: " + resp.get(link).toArray()[0]);
                                System.out.println();
                                i++;
                                if (i == 10 && tamanho > 10) {
                                    System.out.print("Deseja ver mais URLs? (s/n): ");
                                    String escolha = br.readLine();
                                    while (!escolha.equalsIgnoreCase("s") && !escolha.equalsIgnoreCase("n")) {
                                        System.out.print("Escolha inválida. Deseja ver mais URLs? (s/n): ");
                                        escolha = br.readLine();
                                    }
                                    if (escolha.equalsIgnoreCase("n")) {
                                       break;
                                    }
                                }
                            }

                        }
                        case "2" -> {
                            System.out.println("<----Indexar novo URL---->");
                            String res;
                            do {
                                System.out.print("Introduza URL: ");
                                String url = br.readLine();
    
                                res = serverInterface.indexar(url);
                                if (res.equals("URL valido")) {
                                    System.out.println("URL adicionado com sucesso!");
                                    break;
                                } else {
                                    System.out.println(res);
                                }
                            } while (true);
                        }
                        case "3" -> {
                            // TODO: sao os fucking barris de vinho
                            System.out.println("<----Lista dos barrels---->");
                            try {
                                List<String> barrelsList = serverInterface.getBarrelsList();
                                for (String barrelName : barrelsList) {
                                    System.out.println(barrelName);
                                }
                            } catch (RemoteException e) {
                                System.out.println("[EXCEPTION] Erro ao obter a lista de barrels: " + e);
                            }
                            System.out.println("-------------------------");
                        }
                        case "4" -> {
                            // TODO: fucking lista dos downloaders
                            System.out.println("<----Lista dos downloaders---->");
                            //serverInterface.downloadersList();
                            System.out.println("-----------------------------");
                        }
                        case "5" -> {
                            System.out.println("<----Top 10 pesquisas---->");
                            //serverInterface.top10();
                            System.out.println("------------------------");
                        }
                        case "6" -> {
                            System.out.println("<----Logout---->");
                            userType = 0;
                        }
                        case "s" -> {
                            System.out.println("A sair...");
                            System.exit(0);
                        }
                        default -> System.out.println("Escolha errada");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Exceção na main: " + e);
            System.out.println("adeus");
            e.printStackTrace();
        }
    }
    
    public String lerInputs(BufferedReader br) throws RemoteException {
        String username;
        try {
            System.out.print("Username: ");
            username = br.readLine();
        
            while (username.length() < 3 || username.length() > 20) {
                System.out.print("Username errado (3-20 carateres): ");
                username = br.readLine();
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Erro ao ler o username: " + e);
            return "-1 -1";
        }
    
        String password;
        try {
            System.out.print("Password: ");
            password = br.readLine();
        
            while (password.length() < 1 || password.length() > 20) {
                System.out.print("Password errada (1-20 carateres): ");
                password = br.readLine();
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Erro ao ler a password: " + e);
            return "-1 -1";
        }
        
        int res = this.serverInterface.checkLogin(username, password);
        if (res == 1) return "1 " + username;
        else if (res == 2) return "2 " + username;
        else return "0 " + username + " " + password;
    }
    
    
    private int login(BufferedReader br) throws IOException {
        System.out.println("\n----Login----");
        while (true) {
            String resp = lerInputs(br);
            String[] parts = resp.split(" ");
            
            if (parts[0].equals("-1")) {
                System.out.println("[CLIENT] Login falhou: erro no servidor");
                return -1;
            } else if (parts[0].equals("2")) {
                System.out.println("[CLIENT] Login bem sucedido!");
                return 1;
            } else {
                System.out.println("[CLIENT] Login falhou: usuario ou senha errados");
                
                String choice = "";
                do {
                    System.out.print("[CLIENT] Tentar novamente (s/n)? ");
                    try {
                        choice = br.readLine();
                    } catch (Exception e) {
                        System.out.println("[EXCEPTION] Erro: " + e);
                    }
                } while (!choice.equalsIgnoreCase("s") && !choice.equalsIgnoreCase("n"));
                if (choice.equalsIgnoreCase("n")) {
                    return 0;
                }
            }
        }
    }
    
    private int registar(BufferedReader br) throws RemoteException {
        System.out.println("\n----Registar----");
        
        while (true) {
            String resp = lerInputs(br);
            String[] parts = resp.split(" ");
            
            if (parts[0].equals("-1")) {
                System.out.println("[CLIENT] Registo falhou: erro no servidor");
                return -1;
            } else if (parts[0].equals("1") || parts[0].equals("2")) {
                String username = parts[1];
                
                System.out.println("[CLIENT] Registo falhou: user " + username + " ja existe");
                String choice = "";
                do {
                    System.out.print("[CLIENT] Tentar novamente (s/n)? ");
                    try {
                        choice = br.readLine();
                    } catch (Exception e) {
                        System.out.println("[EXCEPTION] Erro: " + e);
                    }
                } while (!choice.equalsIgnoreCase("s") && !choice.equalsIgnoreCase("n"));
                if (choice.equalsIgnoreCase("n")) {
                    return 0; // Exit the registration process
                }
                // If the user chooses to try again, the loop will continue, prompting for new input.
            } else if (parts[0].equals("0")) {
                // This condition is for when the user does not exist and we need to register them.
                // Ensure that parts[2] exists before trying to access it.
                if (parts.length > 2) {
                    String username = parts[1];
                    String password = parts[2];
                    String res = this.serverInterface.checkRegisto(username, password);
                    
                    if (res.equals("Erro no lado do servidor")) {
                        System.out.println("[CLIENT] Registo falhou: erro no servidor");
                        return -1;
                    } else {
                        System.out.println("[CLIENT] " + res);
                        return 1; // Successful registration
                    }
                } else {
                    System.out.println("[CLIENT] Registo falhou: dados insuficientes fornecidos.");
                    return -1;
                }
            }
        }
    }
    
    
    /*
    
    public void printar(String s) throws RemoteException {
        System.out.println("> " + s);
    }
    */
    
    private void serverErrorHandling() {
        System.out.println("[EXCEPTION] Não conseguiu conectar ao server.");
        while (true) {
            try {
                System.out.println("[CLIENT] Tentando reconectar...");
                //Thread.sleep(keepAliveTime);
                serverInterface = (RMIServerInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegistryName);
                
                System.out.println("[CLIENT] Reconectado!");
                this.menu();
                break;
            } catch (Exception e) {
                System.out.println("[EXCEPTION] Nao conseguiu conectar ao server: " + e);
            }
        }
    }
    
}
