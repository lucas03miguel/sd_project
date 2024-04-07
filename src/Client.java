/**
 * @author Lucas e Simão
 */
package src;

import java.io.Serializable;

public class Client implements Serializable {
    private final String username;
    private final String password;
    
    public Client(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
}
