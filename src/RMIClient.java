/**
 * @author Lucas e Simão
 */
package src;

import interfaces.RMIServerInterface;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static java.lang.Thread.sleep;

/**
 * Classe que representa um cliente RMI.
 * Estende a classe UnicastRemoteObject para permitir a comunicação remota.
 */
public class RMIClient extends UnicastRemoteObject {

    /**
     * O host do servidor RMI.
     */
    private final String rmiHost;

    /**
     * A porta do servidor RMI.
     */
    private final int rmiPort;

    /**
     * O nome do registro RMI.
     */
    private final String rmiRegistryName;

    /**
     * A interface do servidor RMI.
     */
    private RMIServerInterface serverInterface;
    
    /**
     * Construtor da classe RMIClient.
     *
     * @param rmiHost O host do servidor RMI.
     * @param rmiPort A porta do servidor RMI.
     * @param rmiRegistryName O nome do registro RMI.
     * @throws RemoteException Se ocorrer um erro durante a comunicação remota.
     * @throws InterruptedException Se a thread for interrompida.
     */
    public RMIClient(String rmiHost, int rmiPort, String rmiRegistryName) throws RemoteException, InterruptedException {
        super();
        this.rmiHost = rmiHost;
        this.rmiPort = rmiPort;
        this.rmiRegistryName = rmiRegistryName;
        
        while (true) {
            try {
                System.out.println("[CLIENT] Configuração: " + rmiHost + ":" + rmiPort + "->" + rmiRegistryName);
                this.serverInterface = (RMIServerInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegistryName);
                System.out.println("[CLIENT] Conectado!!!");
                break;
            } catch (Exception e) {
                System.out.println("[CLIENT] Erro ao conectar ao servidor: " + e);
                sleep(2000);
            }
        }
        run();
        
    }
    /**
     * Método principal que inicia o cliente RMI.
     *
     * @param args Os argumentos de linha de comando.
     */
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        Properties prop = new Properties();
        String SETTINGS_PATH = "properties/configuration.properties";
        
        try {
            prop.load(new FileInputStream(SETTINGS_PATH));
            
            String rmiHost = prop.getProperty("HOST_CLIENT");
            int rmiPort = Integer.parseInt(prop.getProperty("PORT_SERVER"));
            String rmiRegistryName = prop.getProperty("REGISTRY_NAME_SERVER");
            
            new RMIClient(rmiHost, rmiPort, rmiRegistryName);
        } catch (Exception e) {
            System.out.println("[CLIENT] Erro ao conectar ao servidor: " + e);
        }
    }
    
    /**
     * Executa o cliente RMI.
     */
    public void run(){
        try {
            this.menu();
        } catch (Exception ignored) {}
    }
    
    /**
     * Imprime o menu de opções de acordo com o tipo de usuário.
     *
     * @param userType O tipo de usuário (0 para não autenticado, 1 para autenticado).
     */
    private void printMenu(int userType) {
        System.out.println("\n----Menu----");
        
        if (userType == 0) {
            System.out.println("1. Login");
            System.out.println("2. Registar");

        } else if (userType == 1) {
            System.out.println("1. Pesquisar");
            System.out.println("2. Indexar novo URL");
            System.out.println("3. Lista dos barrels");
            System.out.println("4. Tempo médio por pesquisa");
            System.out.println("5. Top 10 pesquisas");
            System.out.println("6. Logout");
        }
        
        System.out.println("s. Sair");
        System.out.println("------------");
        System.out.print("Opção: ");
    }
    
    /**
     * Exibe o menu principal e processa as opções selecionadas pelo usuário.
     *
     * @throws InterruptedException Se a thread for interrompida.
     */
    private void menu() throws InterruptedException {
        int userType = 0;
        BufferedReader br;
        while (true) {
            try {
                br = new BufferedReader(new InputStreamReader(System.in));
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
                            while (searchQuery.length() < 1) {
                                System.out.print("Pesquisa inválida (1+ carateres): ");
                                searchQuery = br.readLine();
                            }
                            HashMap<String, ArrayList<String>> resp = new HashMap<>();
                            try {
                                resp = serverInterface.pesquisar(searchQuery);
                            } catch (RemoteException e) {
                                System.out.println("Erro ao pesquisar palavra. Tente novamente");
                                serverErrorHandling();
                                continue;
                            }
                            if (resp.containsKey("Erro")  || resp.containsKey("Nenhum")) {
                                System.out.println("Nenhum resultado encontrado.");
                                //continue;
                            } else {
                                System.out.println();
                                int i = 0;
                                int tamanho = resp.size();
                                for (String link : resp.keySet()) {
                                    System.out.println("Link: " + link);
                                    System.out.println("Title: " + resp.get(link).toArray()[0]);
                                    System.out.println("Text: " + resp.get(link).toArray()[1]);
                                    System.out.println();
                                    i++;
                                    if (i%10 == 0 && tamanho > 10) {
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

                            System.out.println("<----Lista dos barrels---->");
                            
                            List<String> barrelsList = serverInterface.obterListaBarrels();
                            for (String barrelName : barrelsList)
                                System.out.println(barrelName);
                            System.out.println("-------------------------");
                        }
                        case "4" -> {

                            System.out.println("<----Tempo médio por pesquisa---->");
                            try {
    
                                HashMap<Integer, Double> averageTime = serverInterface.obterTempos();
                                for (Integer id: averageTime.keySet()) {
                                    System.out.println("Barril " + id + "- tempo: " + averageTime.get(id));
                                }
                                
                                
                                System.out.println("Tempo médio: " + averageTime + " décimos de segundo");
                                
                            } catch (RemoteException e) {
                                System.out.println("[EXCEPTION] Erro ao obter o tempo médio por pesquisa: " + e);
                            }
                            System.out.println("------------------------");

                        }
                        case "5" -> {
                            System.out.println("<----Top 10 pesquisas---->");
                            try {
                                HashMap<String, Integer> topSearches = serverInterface.getTopSearches();
                                int i = 1;
                                for (String s: topSearches.keySet()) {
                                    System.out.println(i + "º " + s);
                                    ++i;
                                }
                            } catch (RemoteException e) {
                                System.out.println("[EXCEPTION] Erro ao obter as top 10 pesquisas: " + e);
                            }
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
                
            } catch (Exception e) {
                System.out.println("[EXCEPTION] Exceção na main: " + e);
                serverErrorHandling();
            }
        }
        try {
            br.close();
        } catch (IOException e) {
            System.out.println("[EXCEPTION] Erro ao fechar o buffer: " + e);
        }
    }
    
    /**
     * Lê os inputs do usuário (username e password) a partir do BufferedReader.
     *
     * @param br O BufferedReader para ler os inputs.
     * @return Uma string a conter o resultado da leitura dos inputs.
     * @throws RemoteException Se ocorrer um erro durante a comunicação remota.
     */
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
    
    /**
     * Realiza o processo de login do usuário.
     *
     * @param br O BufferedReader para ler os inputs.
     * @return 1 se o login for bem-sucedido, -1 se ocorrer um erro no servidor, 0 se o usuário decidir não tentar novamente.
     * @throws IOException Se ocorrer um erro de I/O.
     */
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
    
    /**
     * Realiza o processo de registo de um novo usuário.
     *
     * @param br O BufferedReader para ler os inputs.
     * @return 1 se o registo for bem-sucedido, -1 se ocorrer um erro no servidor, 0 se o usuário decidir não tentar novamente.
     * @throws RemoteException Se ocorrer um erro durante a comunicação remota.
     */
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
                    return 0; 
                }

            } else if (parts[0].equals("0")) {

                if (parts.length > 2) {
                    String username = parts[1];
                    String password = parts[2];
                    String res = this.serverInterface.checkRegisto(username, password);
                    
                    if (res.equals("Erro no lado do servidor")) {
                        System.out.println("[CLIENT] Registo falhou: erro no servidor");
                        return -1;
                    } else {
                        System.out.println("[CLIENT] " + res);
                        return 1; 
                    }
                } else {
                    System.out.println("[CLIENT] Registo falhou: dados insuficientes fornecidos.");
                    return -1;
                }
            }
        }
    }
       
    /**
     * Lida com erros de conexão com o servidor, tentando reconectar.
     *
     * @throws InterruptedException Se a thread for interrompida.
     */
    private void serverErrorHandling() throws InterruptedException {

        while (true) {
            try {
                System.out.println("[CLIENT] Tentando reconectar ao server...");

                serverInterface = (RMIServerInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup(rmiRegistryName);
                
                System.out.println("[CLIENT] Reconectado!");
                break;
            } catch (Exception e) {
                System.out.println("[EXCEPTION] Nao conseguiu conectar ao server: " + e);
                sleep(1000);
            }
        }
    }
    
}