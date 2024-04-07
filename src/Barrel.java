/**
 * @author Lucas e Simão
 */
package src;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * A classe Barrel é responsável por processar e guardar as informações dos sites.
 */
public class Barrel extends Thread implements Serializable {
    /**
     * id do Barrel
     */
    private final int id;
    /**
     * número de pesquisas feitas
     */
    private int nPesquisas;
    /**
     * socket para comunicação
     */
    private final MulticastSocket socket;
    /**
     * HashMap que guarda o número de pesquisas feitas por cada Barrel
     */
    private final HashMap<Integer, Integer> pesquisas;
    /**
     * HashMap que guarda as palavras e os urls onde elas aparecem
     */
    private final HashMap<String, HashSet<String>> index; //chave: palavra, valor: conjunto de urls
    /**
     * HashMap que guarda os links e os urls que apontam para eles
     */
    private final HashMap<String, HashSet<String>> links; //chave: url, valor: conjunto de urls
    /**
     * HashMap que guarda as citacoes de texto dos sites
     */
    private final HashMap<String, ArrayList<String>> webInfo; //chave: url, valor: snippet de texto
    /**
     * HashMap que guarda as pesquisas feitas
     */
    private final HashMap<String, Integer> searchs; //chave: pesquisa, valor: número de vezes que foi pesquisada
    /**
     * Ficheiro que guarda os links
     */
    private final File linksFile; //arquivo que guarda os links
    /**
     * Ficheiro que guarda as palavras
     */
    private final File wordsFile; //arquivo que guarda as palavras
    /**
     * Ficheiro que guarda as citacoes
     */
    private final File textSnippetFile; //arquivo que guarda os snippets de texto
    /**
     * Ficheiro que guarda as pesquisas
     */
    private final File searchsFile; //arquivo que guarda as pesquisas
    /**
     * Semáforo para controlar o acesso ao ficheiro das palavras
     */
    private final Semaphore semUpdateWords;
    /**
     * Semáforo para controlar o acesso ao ficheiro dos links
     */
    private final Semaphore semUpdateLinks;
    /**
     * Semáforo para controlar o acesso ao ficheiro das informações
     */
    private final Semaphore semUpdateInfo;
    /**
     * Semáforo para controlar o acesso ao ficheiro das pesquisas
     */
    private final Semaphore semSearch;
    
    /**
     * Construtor da classe Barrel
     * @param id id do Barrel
     * @param multicastPort porta para comunicação
     * @param multicastAddress endereço para comunicação
     * @throws IOException exceção de IO
     */
    public Barrel(int id, int multicastPort, String multicastAddress) throws IOException {
        super();
        this.id = id;
        this.nPesquisas = 0;
    
        this.socket = new MulticastSocket(multicastPort);
        InetAddress group = InetAddress.getByName(multicastAddress);
        this.socket.joinGroup(new InetSocketAddress(group, multicastPort), NetworkInterface.getByIndex(0));
        this.links = new HashMap<>();
        this.index = new HashMap<>();
        this.webInfo = new HashMap<>();
        this.searchs = new HashMap<>();
        this.pesquisas = new HashMap<>();
        
        String linksFilename = "./database/links.txt"; //nome do arquivo que guarda os links
        String wordsFilename = "./database/words.txt"; //nome do arquivo que guarda as palavras
        String textSnippetFilename = "./database/info.txt"; //nome do arquivo que guarda os snippets de texto
        String searchsFilename = "./database/searchs.txt"; //nome do arquivo que guarda as pesquisas
        
        this.linksFile = new File(linksFilename);
        this.wordsFile = new File(wordsFilename);
        this.textSnippetFile = new File(textSnippetFilename);
        this.searchsFile = new File(searchsFilename);
    
        this.semUpdateWords = new Semaphore(1);
        this.semUpdateLinks = new Semaphore(1);
        this.semUpdateInfo = new Semaphore(1);
        this.semSearch = new Semaphore(1);
        System.out.println("BARREL " + id + " INICIALIZADO COM SUCESSO");
    }
    
    /**
     * Método run da thread dos Barrel
     */
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[32 * 1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Barrel " + id + " esperando mensagem...");
                
                this.socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                
                String[] list = message.split("; ");
                String type = list[0].split(" \\| ")[1];
                String url = list[1].split(" \\| ")[1];
                
                
                System.out.println("Guardei o url " + url);
                switch (type) {
                    case "links" -> {
                        int count = Integer.parseInt(list[2].split(" \\| ")[1]);
            
                        for (int i = 3; i < count; i++) {
                            String chave = list[i].split(" \\| ")[1];
                            if (!this.links.containsKey(chave))
                                this.links.put(chave, new HashSet<>());
                            this.links.get(chave).add(url);
                        }
                        updateLinks();
                    }
                    case "words" -> {
                        int count = Integer.parseInt(list[2].split(" \\| ")[1]);
            
                        for (int i = 3; i < count; i++) {
                            String chave = list[i].split(" \\| ")[1];
                            if (!this.index.containsKey(chave))
                                this.index.put(chave, new HashSet<>());
                            this.index.get(chave).add(url);
                        }
                        updateWords();
                    }
                    case "textSnippet" -> {
                        String titulo = list[2].split(" \\| ")[1];
                        String texto = list[3].split(" \\| ")[1];
                        if (!this.webInfo.containsKey(url)) {
                            ArrayList<String> aux = new ArrayList<>();
                            aux.add(titulo);
                            aux.add(texto);
                            this.webInfo.put(url, aux);
                        }
                        updateInfo();
                    }
                }
                
            } catch (Exception e) {
                System.out.println("[Erro no Barrel " + id + "] " + e);
            }
        }
    }
    
    /**
     * Método que atualiza as palavras
     */
    private void updateWords() {
        try {
            criarFicheiros();
    
            this.semUpdateWords.acquire();
            FileWriter writer = new FileWriter(wordsFile);
            for (String word : this.index.keySet()) {
                writer.write(word);
                for (String url : this.index.get(word))
                    writer.write(" | " + url);
                writer.write("\n");
            }
            writer.close();
            this.semUpdateWords.release();
        } catch (Exception e) {
            this.semUpdateWords.release();
            System.out.println("[EXCEPTION] Erro a atualizar o ficheiro das palavras: " + e);
        }
    }
    
    /**
     * Método que atualiza os links
     */
    private void updateLinks() {
        try {
            criarFicheiros();
            
            this.semUpdateLinks.acquire();
            FileWriter linksWriter = new FileWriter(linksFile);
            for (String link : this.links.keySet()) {
                linksWriter.write(link);
                for (String url : this.links.get(link))
                    linksWriter.write(" | " + url);
                linksWriter.write("\n");
            }
            linksWriter.close();
            this.semUpdateLinks.release();
        } catch (Exception e) {
            this.semUpdateLinks.release();
            System.out.println("[EXCEPTION] Erro a atualizar o ficheiro dos links: " + e);
        }
    }
    
    /**
     * Método que atualiza as informações dos sites
     */
    private void updateInfo() {
        try {
            criarFicheiros();
            
            this.semUpdateInfo.acquire();
            FileWriter snippetWriter = new FileWriter(textSnippetFile);
            for (String url : this.webInfo.keySet()) {
                snippetWriter.write(url + " | " + this.webInfo.get(url).toArray()[0] + " | " + this.webInfo.get(url).toArray()[1] + "\n");
            }
            snippetWriter.close();
            
            this.semUpdateInfo.release();
        } catch (Exception e) {
            this.semUpdateInfo.release();
            System.out.println("[EXCEPTION] Erro a atualizar o ficheiro das informacoes: " + e);
        }
    }
    
    /**
     * Método que obtem os links da base de dados
     * @param palavra palavra a pesquisar
     * @return HashMap com os links
     */
    public HashMap<String, ArrayList<String>> obterLinks(String palavra) {
        System.out.println("Pesquisando links com a palavra: " + palavra);
        ArrayList<String> urls = new ArrayList<>();
        HashMap<String, ArrayList<String>> resp = new HashMap<>();
        
        try {
            criarFicheiros();
            obterPesquisas();
            
            this.semSearch.acquire();
            FileWriter fw = new FileWriter(searchsFile);
            if (!this.searchs.containsKey(palavra)) this.searchs.put(palavra, 1);
            else this.searchs.put(palavra, this.searchs.get(palavra) + 1);
    
            for (String search : this.searchs.keySet())
                fw.write(search + " " + this.searchs.get(search) + "\n");
            this.semSearch.release();
            fw.close();
            
            this.semUpdateWords.acquire();
            BufferedReader fr = new BufferedReader(new FileReader(wordsFile));
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (palavra.contains(parts[0])) {
                    urls.addAll(Arrays.asList(parts).subList(1, parts.length));
                }
            }
            this.semUpdateWords.release();
            fr.close();
            
            this.semUpdateInfo.acquire();
            fr = new BufferedReader(new FileReader(textSnippetFile));
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (urls.contains(parts[0])) {
                    resp.put(parts[0], new ArrayList<>());
                    resp.get(parts[0]).add(parts[1]);
                    resp.get(parts[0]).add(parts[2]);
                }
            }
            this.semUpdateInfo.release();
            fr.close();
            
            this.semUpdateLinks.acquire();
            fr = new BufferedReader(new FileReader(linksFile));
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (resp.containsKey(parts[0])) {
                    resp.get(parts[0]).add(String.valueOf(parts.length - 1));
                }
            }
            this.semUpdateLinks.release();
            
            for (String link : resp.keySet()) {
                if (resp.get(link).size() == 2)
                    resp.get(link).add("0");
            }
            fr.close();
            
            this.nPesquisas++;
            pesquisas.put(id, nPesquisas);
    
        } catch (Exception e) {
            System.out.println("[EXCEPTION] " + e);
            HashMap<String, ArrayList<String>> error = new HashMap<>();
            error.put("Erro", new ArrayList<>());
            
            return error;
        }
        if (urls.isEmpty()) {
            System.out.println("[EXCEPTION] Nenhum link encontrado");
            HashMap<String, ArrayList<String>> error = new HashMap<>();
            error.put("Nenhum", new ArrayList<>());
            return error;
        }
    
        HashMap<String, Integer> linksRelevance = new HashMap<>();
        for(String link: resp.keySet()){
            int relevancia = Integer.parseInt(resp.get(link).get(2));
            linksRelevance.put(link, relevancia);
        }
        
        List<String> sortedKeys = new ArrayList<>(resp.keySet());
        sortedKeys.sort((a, b) -> linksRelevance.get(b) - linksRelevance.get(a));
        
        HashMap<String, ArrayList<String>> sortedLinks = new LinkedHashMap<>();
        for (String key : sortedKeys) {
            sortedLinks.put(key, resp.get(key));
        }
        
        return sortedLinks;
    }
    
    /**
     * Método que obtem da base de dados as pesquisas feitas
     */
    private void obterPesquisas() {
        try {
            this.semSearch.acquire();
            BufferedReader fr = new BufferedReader(new FileReader(searchsFile));
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" ");
                this.searchs.put(parts[0], Integer.parseInt(parts[1]));
            }
            fr.close();
            this.semSearch.release();
        } catch (Exception e) {
            this.semSearch.release();
            System.out.println("[EXCEPTION] Erro ao obter as pesquisas: " + e);
        }
    }
    
    /**
     * Método que retorna as top pesquisas feitas
     * @return top 10 pesquisas
     */
    public HashMap<String, Integer> obterTopSearches() {
        try {
            obterPesquisas();
            
            List<Map.Entry<String, Integer>> list = new ArrayList<>(searchs.entrySet());
            list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
    
            HashMap<String, Integer> sortedMap = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : list)
                sortedMap.put(entry.getKey(), entry.getValue());
            
            return sortedMap;
            
        } catch (Exception e) {
            this.semSearch.release();
            System.out.println("[EXCEPTION] Erro ao obter as pesquisas: " + e);
            HashMap<String, Integer> error = new HashMap<>();
            error.put("Erro", null);
            return error;
        }
    }
    
    /**
     * Método que cria os ficheiros necessários
     */
    private void criarFicheiros() {
        boolean __ = false;
        try {
            if (!linksFile.exists()) {
                __ = linksFile.createNewFile();
            }
            if (!wordsFile.exists()) {
                __ = wordsFile.createNewFile();
            }
            if (!textSnippetFile.exists()) {
                __ = textSnippetFile.createNewFile();
            }
            if (!searchsFile.exists()) {
                __ = searchsFile.createNewFile();
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Erro ao criar os ficheiros: " + e);
        }
    }
    
    /**
     * Método que retorna o id do Barrel
     * @return id do Barrel
     */
    public int getIdBarrel() {
        return this.id;
    }
    
    /**
     * Método que retorna o número de pesquisas feitas
     * @return número de pesquisas
     */
    public HashMap<Integer, Integer> getNPesquisas() {
        return this.pesquisas;
    }
    
    /**
     * Método que retorna as ligacoes de um link
     * @return ArrayList com as ligações
     */
    public ArrayList<String> obterLigacoes(String link) {
        ArrayList<String> resp = new ArrayList<>();
        try {
            this.semUpdateLinks.acquire();
            BufferedReader fr = new BufferedReader(new FileReader(linksFile));
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (link.equals(parts[0])) {
                    resp.addAll(Arrays.asList(parts).subList(1, parts.length));
                }
            }
            this.semUpdateLinks.release();
            fr.close();
        } catch (Exception e) {
            this.semUpdateLinks.release();
            System.out.println("[EXCEPTION] Erro ao obter as ligações: " + e);
        }
        return resp;
    }
}
