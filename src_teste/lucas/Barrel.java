package src_teste.lucas;

import java.net.MulticastSocket;
import java.net.InetAddress;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.net.DatagramPacket;
import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;



public class Barrel extends UnicastRemoteObject implements Runnable, IBarrels {
    private int PORT;
    private MulticastSocket socket;
    private InetAddress group;
    private String WordLinkTxt;
    private String WordLinkTxtBackup;
    private String LinkLinkTxt;
    private String LinkLinkTxtBackup;
    private String LinkInfoTxt;
    private String LinkInfoTxtBackup;
    private String UsersTxt;
    private HashMap<String, HashSet<String>> wordLink;
    private HashMap<String, HashSet<String>> linkLink;
    private HashMap<String, HashSet<String>> linkInfo;
    private HashMap<String, String> users;

    /**
     * Construtor da classe Barrel
     * 
     * @param MULTICAST_ADDRESS
     * @param PORT
     * @throws RemoteException
     */
    public Barrel(String MULTICAST_ADDRESS, int PORT) throws RemoteException {
        super();
        this.PORT = PORT;
        try {
            this.socket = new MulticastSocket(this.PORT); // create socket without binding it (only for sending)
            this.group = InetAddress.getByName(MULTICAST_ADDRESS);
            this.socket.joinGroup(group);

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Barrel created");

        getBarrelsFiles("WordLink");
        getHasMaps("WordLink");

        getBarrelsFiles("LinkLink");
        getHasMaps("LinkLink");

        getBarrelsFiles("LinkInfo");
        getHasMaps("LinkInfo");

        getBarrelsFiles("Users");
        getHasMaps("Users");

        // dar inicio à thread
        // escrever nada no ficheiro barrels.txt
        // ErraseContentFile();

    }

    /**
     * Método que lê o ficheiro Users.txt e guarda os dados num HashMap
     * funcao que vai correr em paralelo com o main e vai estar sempre a receber
     * mensagens do multicast
     *
     */
    public void run() {
        while (true) {

            try {
                int bufferSize = 1024 * 8;
                byte[] buffer = new byte[bufferSize];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(packet);

                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":"
                        + packet.getPort() + " with message:");
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);

                processMessage(message);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Método que cria os files de cada um dos HashMaps e guarda os dados
     * 
     * @param directory_and_filename - nome da pasta e do ficheiro
     * @param number                 - número do ficheiro
     */
    private void createFile(String directory_and_filename, int number) { // directory é o nome da pasta ou seja se é um
                                                                         // wordLink ou linkLink ou linkInfo
        String file_path = "./src/main/java/com/example/googol/src/barrels/" + directory_and_filename + "/" + directory_and_filename + number
                + ".txt";

        try {
            File file = new File(file_path);
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
                if (directory_and_filename.equals("WordLink")) {
                    this.WordLinkTxt = directory_and_filename + number + ".txt";
                } else if (directory_and_filename.equals("WordLinkBackup")) {
                    this.WordLinkTxtBackup = directory_and_filename + number + ".txt";
                } else if (directory_and_filename.equals("LinkLink")) {
                    this.LinkLinkTxt = directory_and_filename + number + ".txt";
                } else if (directory_and_filename.equals("LinkLinkBackup")) {
                    this.LinkLinkTxtBackup = directory_and_filename + number + ".txt";
                } else if (directory_and_filename.equals("LinkInfo")) {
                    this.LinkInfoTxt = directory_and_filename + number + ".txt";
                } else if (directory_and_filename.equals("LinkInfoBackup")) {
                    this.LinkInfoTxtBackup = directory_and_filename + number + ".txt";
                }

            } else {
                System.out.println("File already exists.");
                number++;
                createFile(directory_and_filename, number);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Método que apaga o conteúdo de um ficheiro
     * 
     * @param path - caminho do ficheiro
     */
    private void ErraseContentFile(String path) {
        try {
            FileWriter myWriter = new FileWriter(path);
            myWriter.write("");
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // para isto funcionar é necessário que o ficheiro barrels.txt esteja vazio e
    // protanto tenho que limpar no fim do main

    // para saber que fiheiro usar para cada barrel, tenho que ver no ficheiro de
    // ficheiros disponiveis

    /**
     * Método que lê o ficheiro barrels.txt e guarda os nomes dos ficheiros
     * disponiveis num HashSet
     * 
     * @param fileManagerName - nome do ficheiro
     */
    private void getBarrelsFiles(String fileManagerName) { // file manager name tem que ser WordLink, LinkLink ou
                                                           // LinkInfo

        String fileManager_path = "./src_teste/lucas/barrels/Information/" + fileManagerName + ".txt";
        String TxtFolder_path = "./src_teste/lucas//barrels/" + fileManagerName;

        HashSet<String> filesBarrels = new HashSet<String>(); // lista de ficheiros que estão disponiveis
        String final_txt_file = "";
        // verificar o valor no ficheiro barrels.txt
        String content = "";
        try {

            BufferedReader buffRead = new BufferedReader(new FileReader(fileManager_path));
            String linha = buffRead.readLine();
            while (linha != null) {
                content += linha;
                linha = buffRead.readLine();
            }

            // System.out.println(content);
            buffRead.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content.equals("first")) { // se for a primeira vez, então só vou criar ficheiros
            //System.out.println("There are no files in the barrels directory");
            createFile(fileManagerName, 0);
            if (!fileManagerName.equals("Users")) {
                createFile(fileManagerName + "Backup", 0);
            }
        }

        // eu apenas vou buscar os ficheiros que estão no diretorio barrels se o
        // ficheiro barrels.txt estiver vazio
        else if (content.equals("") && !content.equals("first")) {
            String path = TxtFolder_path;
            // para ir buscar todos os ficheiros que estão no diretorio barrels
            File fObj = new File(path);
            File a[] = fObj.listFiles();
            for (int i = 0; i < a.length; i++) {
                if (a[i].isFile()) {
                    System.out.println(a[i].getName());
                    filesBarrels.add(a[i].getName());
                }
            }

            if (filesBarrels.isEmpty()) { // se não houver mais ficheiros no diretorio barrels disponiveis então tenho
                                          // que criar um novo
                try {
                    File file = new File(fileManager_path);
                    FileWriter fw = new FileWriter(file, true);
                    fw.write("first");
                    fw.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //System.out.println("There are no files in the barrels directory");
                createFile(fileManagerName, 0);
                if (!fileManagerName.equals("Users")) {
                    createFile(fileManagerName + "Backup", 0);
                }

            } else {
                final_txt_file = filesBarrels.stream().findFirst().get();
                filesBarrels.remove(final_txt_file);

                //System.out.print("I am connected to the barrel:" + final_txt_file);

                if (fileManagerName.equals("WordLink")) {
                    this.WordLinkTxt = final_txt_file;
                    this.WordLinkTxtBackup = "WordLinkBackup" + final_txt_file.split("WordLink")[1].split(".txt")[0]
                            + ".txt";
                } else if (fileManagerName.equals("LinkLink")) {
                    this.LinkLinkTxt = final_txt_file;
                    this.LinkLinkTxtBackup = "LinkLinkBackup" + final_txt_file.split("LinkLink")[1].split(".txt")[0]
                            + ".txt";
                } else if (fileManagerName.equals("LinkInfo")) {
                    this.LinkInfoTxt = final_txt_file;
                    this.LinkInfoTxtBackup = "LinkInfoBackup" + final_txt_file.split("LinkInfo")[1].split(".txt")[0]
                            + ".txt";
                } else if (fileManagerName.equals("Users")) {
                    this.UsersTxt = final_txt_file;
                }

                // escrever no ficheiro barrels.txt os ficheiros que estão disponiveis

                try {
                    FileOutputStream fos = new FileOutputStream(fileManager_path);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(filesBarrels);
                    oos.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        } else {
            try {

                FileInputStream fis = new FileInputStream(fileManager_path);
                ObjectInputStream ois = new ObjectInputStream(fis);
                filesBarrels = (HashSet<String>) ois.readObject();
                ois.close();
                fis.close();
                if (filesBarrels.isEmpty()) { // se não houver mais ficheiros no diretorio barrels disponiveis então
                                              // tenho que criar um novo

                    //System.out.println("There are no more files in the barrels directory");
                    createFile(fileManagerName, 0);
                    if (!fileManagerName.equals("Users")) {
                        createFile(fileManagerName + "Backup", 0);
                    }

                } else {
                    final_txt_file = filesBarrels.stream().findFirst().get();
                    filesBarrels.remove(final_txt_file);

                    //System.out.print("I am connected to the barrel:" + final_txt_file);

                    // escrever no ficheiro barrels.txt os ficheiros que estão disponiveis
                    FileOutputStream fos = new FileOutputStream(fileManager_path);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(filesBarrels);
                    oos.close();

                    if (fileManagerName.equals("WordLink")) {
                        this.WordLinkTxt = final_txt_file;
                        this.WordLinkTxtBackup = "WordLinkBackup" + final_txt_file.split("WordLink")[1].split(".txt")[0]
                                + ".txt";
                    } else if (fileManagerName.equals("LinkLink")) {
                        this.LinkLinkTxt = final_txt_file;
                        this.LinkLinkTxtBackup = "LinkLinkBackup" + final_txt_file.split("LinkLink")[1].split(".txt")[0]
                                + ".txt";
                    } else if (fileManagerName.equals("LinkInfo")) {
                        this.LinkInfoTxt = final_txt_file;
                        this.LinkInfoTxtBackup = "LinkInfoBackup" + final_txt_file.split("LinkInfo")[1].split(".txt")[0]
                                + ".txt";
                    } else if (fileManagerName.equals("Users")) {
                        this.UsersTxt = final_txt_file;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Método que vai buscar os ficheiros que estão no diretorio barrels e que estão
     * disponiveis para serem usados
     * 
     * @param type - tipo de HashMap que queremos obter
     */
    private void getHasMaps(String type) {

        String file = "";
        String backup_file = "";

        if (type.equals("WordLink")) {
            System.out.println("I am going to get the WordLink HashMap");
            // System.out.println("I am going to get the HashMap from the barrel:
            // "+this.WordLinkTxt);
            file = this.WordLinkTxt;
            backup_file = this.WordLinkTxtBackup;
        }

        else if (type.equals("LinkLink")) {
            file = this.LinkLinkTxt;
            backup_file = this.LinkLinkTxtBackup;
        }

        else if (type.equals("LinkInfo")) {
            file = this.LinkInfoTxt;
            backup_file = this.LinkInfoTxtBackup;
        } else if (type.equals("Users")) {
            file = this.UsersTxt;
        }

        System.out.println("I am going to get the HashMap from the barrel: " + file);

        File _file = new File("./src/main/java/com/example/googol/src/barrels/" + type + "/" + file);

        if (_file.length() == 0) { // se for a primeira vez, então só vou criar o hashMap
            if (type.equals("WordLink")) {
                System.out.println("I just created a new WordLink HashMap");
                this.wordLink = new HashMap<String, HashSet<String>>();
            } else if (type.equals("LinkLink")) {
                System.out.println("I just created a new LinkLink HashMap");

                this.linkLink = new HashMap<String, HashSet<String>>();
            } else if (type.equals("LinkInfo")) {
                System.out.println("I just created a new LinkInfo HashMap");
                this.linkInfo = new HashMap<String, HashSet<String>>();
            } else if (type.equals("Users")) {
                System.out.println("I just created a new Users HashMap");
                this.users = new HashMap<String, String>();
            }

        } else { // se já o ficheiro não estiver vazio, então tenho que ir buscar os hashmaps que
                 // estão no ficheiro
            System.out.println("I am going to get the HashMap from the barrel:pilal " + file);
            try {
                FileInputStream fis = new FileInputStream("./src/main/java/com/example/googol/src/barrels/" + type + "/" + file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                if (type.equals("WordLink")) {
                    System.out.println("I just got the WordLink HashMap");
                    this.wordLink = (HashMap<String, HashSet<String>>) ois.readObject();
                } else if (type.equals("LinkLink")) {
                    this.linkLink = (HashMap<String, HashSet<String>>) ois.readObject();
                } else if (type.equals("LinkInfo")) {
                    this.linkInfo = (HashMap<String, HashSet<String>>) ois.readObject();
                } else if (type.equals("Users")) {
                    this.users = (HashMap<String, String>) ois.readObject();
                }

                ois.close();
                fis.close();
            } catch (IOException e) {
                try {
                    FileInputStream fis = new FileInputStream(
                            "./src/main/java/com/example/googol/src/barrels/" + type + "Backup" + "/" + backup_file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    if (type.equals("WordLink")) {
                        System.out.println("I just got the WordLink HashMap");
                        this.wordLink = (HashMap<String, HashSet<String>>) ois.readObject();
                        writeInFile("WordLink");
                    } else if (type.equals("LinkLink")) {
                        this.linkLink = (HashMap<String, HashSet<String>>) ois.readObject();
                        writeInFile("WordLink");
                    } else if (type.equals("LinkInfo")) {
                        this.linkInfo = (HashMap<String, HashSet<String>>) ois.readObject();
                        writeInFile("WordLink");
                    }

                    ois.close();
                    fis.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Método que vai ser chamado sempre que receber uma mensagem e vai escrever no
     * ficheiro correspondente
     * 
     * @param message - mensagem que vai ser escrita no ficheiro
     */
    private void processMessage(String message) {
        String[] words = message.split("\\|");
        if (words[0].split(" ")[1].equals("WordLink")) {
            // System.out.println("WordLink");
            // I have to link a word to a link
            String word = words[1].split(" ")[2];
            String link = words[2].split(" ")[2];
            addTowordLink(word, link);
            writeHashMaps("WordLink");

        } else if (words[0].split(" ")[1].equals("LinkLink")) {
            // I have to link a link to a link
            String link = words[1].split(" ")[2];
            String link2 = words[2].split(" ")[2];
            addTolinkLink(link, link2);
            writeHashMaps("LinkLink");
        } else if (words[0].split(" ")[1].equals("LinkInfo")) {
            // I have to add the info of a link
            for (String s : words) {
                System.out.println(s);
            }
            String link = words[1].split(" ")[2];
            String descricao = words[2].split("descricao ")[1];
            String titulo = words[3].split("titulo ")[1];

            addTolinkInfo(link, titulo, descricao); // tenho que mudar aqui pq o titulo e a descrição podem ter espaços
            writeHashMaps("LinkInfo");
        }
    }

    /**
     * Método que se a palavra já estiver no hashmap, então vou adicionar o link,
     * se a palavra não estiver no hashmap, então cria um novo
     * 
     * @param word - palavra que vai ser adicionada
     * @param link - link que vai ser adicionado
     */
    private void addTowordLink(String word, String link) {
        if (this.wordLink.containsKey(word)) {
            this.wordLink.get(word).add(link);

        } else { // se a palavra não estiver no hashmap, então tenho que criar um novo
            HashSet<String> links = new HashSet<String>();
            links.add(link);
            this.wordLink.put(word, links);
        }
    }

    /**
     * Método que se o link já estiver no hashmap, então vou adicionar o link,
     * se o link não estiver no hashmap, então cria um novo
     * 
     * @param link1 - link que vai ser adicionado
     * @param link2 - link que vai ser adicionado
     */
    private void addTolinkLink(String link1, String link2) {
        if (this.linkLink.containsKey(link1)) {
            this.linkLink.get(link1).add(link2);

        } else {
            HashSet<String> links = new HashSet<String>();
            links.add(link2);
            this.linkLink.put(link1, links);
        }
    }

    /**
     * Método que se o link já estiver no hashmap, então vou adicionar o titulo e a
     * descrição,
     * se o link não estiver no hashmap, então cria um novo
     * 
     * @param link      - link que vai ser adicionado
     * @param titulo    - titulo que vai ser adicionado
     * @param descricao - descricao que vai ser adicionada
     */
    private void addTolinkInfo(String link, String titulo, String descricao) {
        if (this.linkInfo.containsKey(link)) {
            this.linkInfo.get(link).add(titulo);
            this.linkInfo.get(link).add(descricao);

        } else {
            HashSet<String> infos = new HashSet<String>();
            infos.add(titulo);
            infos.add(descricao);
            this.linkInfo.put(link, infos);
        }
    }

    /**
     * Método que vai escrever no ficheiro correspondente
     * 
     * @param type - tipo de ficheiro que vai ser escrito
     */
    private void writeHashMaps(String type) { // function to write the hashmaps to the txt files
        String file = "";
        String file_backup = "";
        HashMap<String, HashSet<String>> hashMap = null;

        if (type.equals("WordLink")) {
            file = this.WordLinkTxt;
            file_backup = this.WordLinkTxtBackup;
            hashMap = this.wordLink;
        } else if (type.equals("LinkLink")) {
            file = this.LinkLinkTxt;
            file_backup = this.LinkLinkTxtBackup;
            hashMap = this.linkLink;
        } else if (type.equals("LinkInfo")) {
            file = this.LinkInfoTxt;
            file_backup = this.LinkInfoTxtBackup;
            hashMap = this.linkInfo;
        }

        String path = "./src/main/java/com/example/googol/src/barrels/" + type + "/" + file;
        String path_backup = "./src/main/java/com/example/googol/src/barrels/" + type + "Backup" + "/" + file_backup;

        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(hashMap);
            oos.close();
            fos.close();

            FileOutputStream fos_backup = new FileOutputStream(path_backup);
            ObjectOutputStream oos_backup = new ObjectOutputStream(fos_backup);
            oos_backup.writeObject(hashMap);
            oos_backup.close();
            fos_backup.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que vai ler o ficheiro correspondente
     * 
     * @param type - tipo de ficheiro que vai ser lido
     */
    private void writeInFile(String type) {
        String file = "";

        HashMap<String, HashSet<String>> hashMap = null;

        if (type.equals("WordLink")) {
            file = this.WordLinkTxt;
            hashMap = this.wordLink;
        } else if (type.equals("LinkLink")) {
            file = this.LinkLinkTxt;
            hashMap = this.linkLink;
        } else if (type.equals("LinkInfo")) {
            file = this.LinkInfoTxt;
            hashMap = this.linkInfo;
        }

        String path = "./src/main/java/com/example/googol/src/barrels/" + type + "/" + file;

        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.wordLink);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que lê o ficheiro Users.txt e guarda os dados num HashMap
     * 
     * @param username - nome do utilizador
     * @param password - password do utilizador
     * @return String - mensagem de erro ou sucesso
     * @throws RemoteException
     *
     */
    public String writeUserInfo(String username, String password) throws RemoteException {
        // posso criar um novo barrel para cada user
        // todos registam o user e a password

        // em primeiro verificar se o user ja existe
        if (this.users.containsKey(username)) {
            return "User already exists";
        } else {
            if (username.contains(" ") || username.contains("\\|")) {
                return "Username cannot contain spaces or |";
            }
            // se nao existir, criar novo user
            this.users.put(username, password);
            String path = "./src/main/java/com/example/googol/src/barrels/Users/" + this.UsersTxt;

            try {
                FileOutputStream fos = new FileOutputStream(path);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(this.wordLink);
                oos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // System.out.println("Novo user");
        }

        return "";
    }

    /**
     * Método que lê o ficheiro Users.txt e guarda os dados num HashMap
     * 
     * @param username - nome do utilizador
     * @param password - password do utilizador
     * @return String - mensagem de erro ou sucesso
     * @throws RemoteException
     *
     */
    public String readUserInfo(String username, String password) throws RemoteException {
        // posso criar um novo barrel para cada user
        // todos registam o user e a password
        if (this.users.containsKey(username)) {
            if (this.users.get(username).equals(password)) {
                return "Login successful";
            } else {
                return "Wrong password";
            }
        } else {
            return "User does not exist";
        }
    }

    /**
     * Método que vai pesquisar por palavra e retornar os links associados
     * 
     * @param word - palavra que vai ser pesquisada
     * @return - retorna os links associados
     * @throws RemoteException
     */
    public HashSet<String> searchWord(String word) throws RemoteException {
        HashSet<String> links = null;
        System.out.println("Word: " + word);
        for(String s : this.wordLink.get(word)) {
            System.out.println("Content: " + s);
        }
        if (this.wordLink.containsKey(word)) {
            links = this.wordLink.get(word);
        }
        return links;
    }

    /**
     * Método que vai pesquisar por link e retornar os links associados
     * 
     * @param link - link que vai ser pesquisado
     * @return - retorna os links associados
     * @throws RemoteException
     */
    public HashSet<String> searchLink(String link) throws RemoteException {
        HashSet<String> links = null;
        System.out.println("Link: " + link);
        for(String s : this.linkLink.keySet()) {
            System.out.println("Key: " + s);
        }
        if (this.linkLink.containsKey(link)) {
            links = this.linkLink.get(link);
        }
        
        return links;
    }

    /**
     * Método que vai pesquisar por link e retornar as informações associadas
     * 
     * @param link - link que vai ser pesquisado
     * @return - retorna as informações associadas
     * @throws RemoteException
     */
    public HashSet<String> searchInfo(String link) throws RemoteException {
        HashSet<String> infos = null;
        if (this.linkInfo.containsKey(link)) {
            infos = this.linkInfo.get(link);
        }
        return infos;
    }

    /**
     * Método que vai verificar se o barrel está vivo
     * 
     * @return - retorna true se estiver vivo
     * @throws RemoteException
     */
    public boolean isAlive() throws RemoteException {
        return true;
    }

    /**
     * Método que vai criar um novo barrel, vai ligar-se ao cliente por rmi
     * e vai criar uma nova thread para o barrel
     * 
     * @throws RemoteException
     */
    public static void main(String[] args) {
        try {

            Barrel barrel = new Barrel("224.3.2.1", 4321);
            Thread thread = new Thread(barrel);
            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("rmi_barrel", barrel);
            thread.start();
        } catch (IOException e) {
            System.out.print("I was not able to create the barrel");
        }

    }

}
