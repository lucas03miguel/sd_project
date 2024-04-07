/**
 * @author Lucas e Simão
 */
package src;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class teste {
    
    /**
     * Verifica se a string fornecida é um URL válido.
     *
     * @param urlString A string a ser verificada.
     * @return true se a string for um URL válido, false caso contrário.
     */
    public static boolean isValidURL(String urlString) {
        try {
            URI uri = new URI(urlString);
            if (uri.getScheme() == null || uri.getHost() == null)
                return false;
            
            URL __ = uri.toURL();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }
    
    public static void main(String[] args) {
        String url1 = "https://ww3.sdfsdfsds.com";
        String url2 = "htp://www.example.com";
        String url3 = "http://www.example.com/path/to/resource";
        
        System.out.println(url1 + " é válido? " + isValidURL(url1)); // Deve imprimir true
        System.out.println(url2 + " é válido? " + isValidURL(url2)); // Deve imprimir false
        System.out.println(url3 + " é válido? " + isValidURL(url3)); // Deve imprimir false
    }
}
