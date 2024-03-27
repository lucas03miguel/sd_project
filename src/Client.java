package src;

import java.io.Serializable;

public class Client implements Serializable {
    private final String username;
    private final boolean isAdmin;
    
    public Client(String username, boolean admin) {
        this.username = username;
        this.isAdmin = admin;
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
    
}
