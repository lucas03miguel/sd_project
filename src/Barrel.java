/**
 * @author Lucas e Simão
 */
package src;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Barrel extends Thread implements Serializable {
    private final int id;
    private int nPesquisas;
    private final int multicastPort;
    private final String multicastAddress;
    private MulticastSocket socket;
    private InetAddress group;
    private HashMap<Integer, Integer> pesquisas;
    private HashMap<String, HashSet<String>> index; //chave: palavra, valor: conjunto de urls
    private HashMap<String, HashSet<String>> links; //chave: url, valor: conjunto de urls
    private HashMap<String, ArrayList<String>> webInfo; //chave: url, valor: snippet de texto
    private HashMap<String, Integer> searchs; //chave: pesquisa, valor: número de vezes que foi pesquisada
    private final String linksFilename; //nome do arquivo que guarda os links
    private final String wordsFilename; //nome do arquivo que guarda as palavras
    private final String textSnippetFilename; //nome do arquivo que guarda os snippets de texto
    private final String searchsFilename; //nome do arquivo que guarda as pesquisas
    private final File linksFile; //arquivo que guarda os links
    private final File wordsFile; //arquivo que guarda as palavras
    private final File textSnippetFile; //arquivo que guarda os snippets de texto
    private final File searchsFile; //arquivo que guarda as pesquisas
    private final Semaphore semUpdateWords;
    private final Semaphore semUpdateLinks;
    private final Semaphore semUpdateInfo;
    private final Semaphore semSearch;
    private final Semaphore sem;
    
    public Barrel(int id, int multicastPort, String multicastAddress) throws IOException {
        super();
        this.id = id;
        this.nPesquisas = 0;
        
        this.multicastPort = multicastPort;
        this.multicastAddress = multicastAddress;
        this.socket = new MulticastSocket(multicastPort);
        this.group = InetAddress.getByName(multicastAddress);
        this.socket.joinGroup(new InetSocketAddress(group, multicastPort), NetworkInterface.getByIndex(0));
        this.links = new HashMap<>();
        this.index = new HashMap<>();
        this.webInfo = new HashMap<>();
        this.searchs = new HashMap<>();
        this.pesquisas = new HashMap<>();
        
        this.linksFilename = "./database/links.txt";
        this.wordsFilename = "./database/words.txt";
        this.textSnippetFilename = "./database/info.txt";
        this.searchsFilename = "./database/searchs.txt";
        
        this.linksFile = new File(linksFilename);
        this.wordsFile = new File(wordsFilename);
        this.textSnippetFile = new File(textSnippetFilename);
        this.searchsFile = new File(searchsFilename);
    
        this.semUpdateWords = new Semaphore(1);
        this.semUpdateLinks = new Semaphore(1);
        this.semUpdateInfo = new Semaphore(1);
        this.semSearch = new Semaphore(1);
        this.sem = new Semaphore(1);
        System.out.println("BARREL " + id + " INICIALIZADO COM SUCESSO");
    }
    
    public void run() {
        while (true) {
            try {
                //HashMap<String, HashSet<String>> auxLinks = new HashMap<>();
                //HashMap<String, HashSet<String>> auxWords = new HashMap<>();
                //ArrayList<String> auxInfo = new ArrayList<>();
                
                
                byte[] buffer = new byte[32 * 1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Barrel " + id + " esperando mensagem...");
                
                this.socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                //System.out.println("Barrel " + id + " recebeu mensagem: " + message);
                
                String[] list = message.split("; ");
                String type = list[0].split(" \\| ")[1];
                String url = list[1].split(" \\| ")[1];
                
                
                System.out.println("Guardei o url " + url);
                //System.out.println("type: " + type + " count: " + count);
                //System.out.println(Arrays.toString(list) + "\n");
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
                        //System.out.println("titulo: " + titulo);
                        //System.out.println("chave: " + texto);
                        
                        updateInfo();
                    }
                }
                
            } catch (Exception e) {
                System.out.println("[Erro no Barrel " + id + "] " + e);
            }
        }
    }
    
    private void updateWords() {
        try {
            criarFicheiros();
            /*
            if (!linksFile.exists()) {
                linksFile.createNewFile();
            }
            if (!wordsFile.exists()) {
                wordsFile.createNewFile();
            }
            if (!textSnippetFile.exists()) {
                textSnippetFile.createNewFile();
            }
            
            // Ler o conteúdo atual do arquivo de palavras
            HashMap<String, HashSet<String>> currentIndex = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader(wordsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                String word = parts[0];
                HashSet<String> urls = new HashSet<>(Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length)));
                currentIndex.put(word, urls);
            }
            reader.close();
            
            // Atualizar o índice atual com as novas palavras e URLs
            for (String word : index.keySet()) {
                if (!this.index.containsKey(word)) {
                    this..put(word, new HashSet<>());
                }
                currentIndex.get(word).addAll(index.get(word));
            }
            */
    
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
    /*
    private void guardarLinks() {
        try (BufferedReader fr = new BufferedReader(new FileReader(linksFile))){
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                
                String url = parts[0];
                if (!this.links.containsKey(url))
                    this.links.put(url, new HashSet<>());
                this.links.get(url).add(url);
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Erro ao ler do ficheiro dos links: " + e);
        }
    }
    
    private void guardarPalavras() {
        try (BufferedReader fr = new BufferedReader(new FileReader(wordsFile))) {
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                String word = parts[0];
                if (!this.index.containsKey(word))
                    this.index.put(word, new HashSet<>());
                this.index.get(word).add(url);
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Erro ao ler do ficheiro das palavras: " + e);
        }
    }
    
    private void guardarInfo() {
    
    }
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
            fw.close();
            this.semSearch.release();
            
            this.sem.acquire();
            BufferedReader fr = new BufferedReader(new FileReader(wordsFile));
            String line;
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (palavra.contains(parts[0])) {
                    urls.addAll(Arrays.asList(parts).subList(1, parts.length));
                }
            }
            fr.close();
            
            fr = new BufferedReader(new FileReader(textSnippetFile));
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (urls.contains(parts[0])) {
                    resp.put(parts[0], new ArrayList<>());
                    resp.get(parts[0]).add(parts[1]);
                    resp.get(parts[0]).add(parts[2]);
                }
            }
            fr.close();
            
            fr = new BufferedReader(new FileReader(linksFile));
            while ((line = fr.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (resp.containsKey(parts[0])) {
                    resp.get(parts[0]).add(String.valueOf(parts.length - 1));
                }
            }
            
            for (String link : resp.keySet()) {
                if (resp.get(link).size() == 2)
                    resp.get(link).add("0");
            }
            
            fr.close();
            //long endTime = System.currentTimeMillis();
            //long time = endTime - startTime;
            this.nPesquisas++;
    
            pesquisas.put(id, nPesquisas);
            this.sem.release();
        } catch (Exception e) {
            this.sem.release();
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
        //System.out.println("oiiii " + resp);
    
        HashMap<String, Integer> linksRelevance = new HashMap<>();
        for(String link: resp.keySet()){
            //System.out.println("aaaaaaa");
            //System.out.println(resp.get(link));
            //System.out.println();
            int relevancia = Integer.parseInt(resp.get(link).get(2));
            linksRelevance.put(link, relevancia);
        }
        //System.out.println("oooooooooooooooo" + linksRelevance);
        
        List<String> sortedKeys = new ArrayList<>(resp.keySet());
        sortedKeys.sort((a, b) -> linksRelevance.get(b) - linksRelevance.get(a));
        
        HashMap<String, ArrayList<String>> sortedLinks = new LinkedHashMap<>();
        for (String key : sortedKeys) {
            sortedLinks.put(key, resp.get(key));
        }
        /*
        System.out.println(sortedLinks);
        
        ArrayList<Map.Entry<String, ArrayList<String>>> list = new ArrayList<>(resp.entrySet());
    
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                int a = Integer.parseInt(list.get(j).getValue().get(2));
                System.out.println("aaaaaaaaaaaaaaa "+ a);
                if (a < Integer.parseInt(list.get(j + 1).getValue().get(2))) {
                    Map.Entry<String, ArrayList<String>> temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
        
        HashMap<String, ArrayList<String>> sortedMap = new HashMap<>();
        for (Map.Entry<String, ArrayList<String>> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        */
        
        return sortedLinks;
    }
    
    private boolean obterPesquisas() {
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
            return true;
        } catch (Exception e) {
            this.semSearch.release();
            System.out.println("[EXCEPTION] Erro ao obter as pesquisas: " + e);
            return false;
        }
    }
    
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
            //System.out.println(__);
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Erro ao criar os ficheiros: " + e);
        }
    }
    
    public int getIdBarrel() {
        return this.id;
    }
    public HashMap<Integer, Integer> getNPesquisas() {
        return this.pesquisas;
    }
}
