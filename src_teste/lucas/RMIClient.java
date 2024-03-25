package src_teste.lucas;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.rmi.Remote;
import java.rmi.RemoteException;



public class RMIClient extends UnicastRemoteObject {

    /**
     * Construtor da classe RMIClient
     * 
     * @throws RemoteException
     */
    protected RMIClient() throws RemoteException {
        super();
    }

    /**
     * Método main que inicia o cliente RMI do sistema
     * 
     * @param args
     */
    public static void main(String[] args) {
        
        try {
            // Localiza o registro RMI no servidor de nomes do RMI Search Module
            SearchModule searchModule = (SearchModule) LocateRegistry.getRegistry(7001).lookup("rmi_first");
    
            Scanner input = new Scanner(System.in);
            int option;
            boolean flag = false;
    
            while (!flag) {
                try {
                    System.out.println("-------------------Googol-------------------\n");
                    System.out.println("1 - Search Link");
                    System.out.println("2 - Serch Word");
                    System.out.println("3 - Get info from link");
                    System.out.println("4 - Index a new link");
                    System.out.println("5 - Exit");
                    System.out.println("Opção:");
    
                    option = input.nextInt();


                    if(option==1){
                        // pesquisa link e retorna o links associados
                        pesquisaLink(searchModule);
                        continue;
                    }
                    else if(option==2){
                        // funcao para pesquisar palavra e retornar links associados
                        pesquisaWord(searchModule);
                        continue;
                    }
                    else if(option==3){
                        // consulta - retorna a informação do link
                        consulta(searchModule);
                        continue;
                    }
                    else if(option==4){
                        IndexLink(searchModule);
                        continue;
                    }
                    else if(option==5){
                        // sair do sistema
                        flag = true;
                        System.exit(0);
                    }
                    else{
                        System.out.println("Opção inválida");
                    }
                    
    

                } catch (InputMismatchException e) {
                    System.out.println("Opção inválida");
                    input.nextLine();
                } catch (Exception e) {
                    System.out.println("Erro no menu: " + e.getMessage());
                }
            }
            
            input.close();
    
        } catch (Exception e) {
            System.err.println("Erro no cliente RMI: " + e.toString());
            e.printStackTrace();
        }
    }



    /**
     * Método que permite fazer login no sistema e verificar se o utilizador existe,
     * caso exista, o utilizador é autenticado e pode aceder ao sistema
     * 
     * @param searchModule
     */
    private static void login(SearchModule searchModule) {

        try (Scanner input = new Scanner(System.in)) {
            String username, password;
            boolean flag = true;
            while (flag) {
                try {
                    System.out.println("Login\n");

                    System.out.println("Username:\n");
                    username = input.nextLine();
                    System.out.println("Password:\n");
                    password = input.nextLine();

                    String result = searchModule.readUserInfo(username, password);


                } catch (Exception e) {
                    System.out.println("Erro no login");
                }
            }
        }

    }

    /**
     * Método que permite registar um novo utilizador no sistema e verificar se o
     * utilizador já existe,
     * caso não exista, o utilizador é registado e pode aceder ao sistema
     * 
     * @param searchModule
     */
    private static void registo(SearchModule searchModule) {

        try (Scanner input = new Scanner(System.in)) {
            String username, password;

            while (true) {
                try {
                    System.out.println("Register\n");

                    System.out.println("Username:\n");
                    username = input.nextLine();
                    System.out.println("Password:\n");
                    password = input.nextLine();

                    searchModule.writeUserInfo(username, password);

                } catch (Exception e) {
                    System.out.println("Erro no registo");
                }

            }
        }

    }

    /**
     * Método que permite consultar as informações de um link e verificar se o link
     * existe,
     * caso exista, o link é consultado e retorna as informações do link
     * 
     * @param searchModule
     *
     */
    private static void pesquisaLink(SearchModule searchModule) {

        try (Scanner input = new Scanner(System.in)) {
            String inputlink;

            while (true) {
                try {
                    System.out.println("Search Link\n");

                    //System.out.println("Link:\n");
                    inputlink = input.nextLine();
                    System.out.print(inputlink);
                    //searchModule.searchLink(inputlink);
                    HashSet<String> links = searchModule.searchLink(inputlink);
                    
                    if (links.isEmpty()||links==null) {
                        System.out.println("Link não encontrado");
                    }

                    for (String link : links) {
                        System.out.println(link);
                    }
                    

                } catch (Exception e) {
                    System.out.println("Erro na pesquisa do link");
                }
            }
        }

    }

    private static void IndexLink(SearchModule searchModule){
        try (Scanner input = new Scanner(System.in)) {
            String inputlink;

            while (true) {
                try {
                    System.out.println("Search Link\n");

                    //System.out.println("Link:\n");
                    inputlink = input.nextLine();
                    //searchModule.searchLink(inputlink);
                    searchModule.indexLink(inputlink);
                    

                } catch (Exception e) {
                    System.out.println("Erro na pesquisa do link");
                }
            }
        }

    }

    private static void pesquisaWord(SearchModule searchModule) {

        try (Scanner input = new Scanner(System.in)) {
            String inputword;

            while (true) {
                try {
                    System.out.println("Search Word\n");

                    System.out.println("Word:\n");
                    inputword = input.nextLine();

                    //searchModule.searchWord(inputword);

                    HashSet<String> links = searchModule.searchWord(inputword.toLowerCase());
                     for (String link : links) {
                        System.out.println(link);
                     }

                } catch (Exception e) {
                    System.out.println("Erro na pesquisa da palavra");
                }
            }
        }

    }

    private static void consulta(SearchModule searchModule) {

        try (Scanner input = new Scanner(System.in)) {
            String inputlink;

            while (true) {
                try {
                    System.out.println("Search Link\n");

                    System.out.println("Link:\n");
                    inputlink = input.nextLine();

                    //searchModule.searchInfo(inputlink);

                    HashSet<String> infos = searchModule.searchInfo(inputlink);
                    for (String info : infos) {
                        System.out.println(info);
                    }
                     

                } catch (Exception e) {
                    System.out.println("Erro na pesquisa do link");
                }
            }
        }

    }

}