package src_teste.nuno;

import java.io.Serializable;

public class User implements Serializable{
    
    private String username;
    private String password;
    //private boolean is_logged_in;
    
    public User(String u , String p){
        this.username = u;
        this.password = p;
        //this.is_logged_in = true;
    }
    
    public void setUsername(String u){
        this.username = u;
    }
    
    public String getUsername(){
        return this.username;
    }
    
    public void setPassword(String p){
        this.password = p;
    }
    
    public String getPassword(){
        return this.password;
    }
    /*
    public void setLogStatus(boolean b){
        this.is_logged_in = b;
    }

    public boolean getLogStatus(){
        return this.is_logged_in;
    }*/
    
    public boolean checkCredentials(String u, String p){
        if(u.equals(this.username) && p.equals(this.password)) return true;
        return false;
    }
}

