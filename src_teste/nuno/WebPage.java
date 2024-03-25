package src_teste.nuno;

import java.io.Serializable;
//import java.util.ArrayList;

public class WebPage implements Serializable {
    
    private String url;
    private String title = "";
    private String text = "";
    
    public WebPage(String u){
        this.url = u;
    }
    
    public WebPage(String u, String t){
        super();
        this.url = u;
        this.title = t;
    }
    
    public WebPage(String u, String t, String txt){
        super();
        this.url = u;
        this.title = t;
        this.text = txt;
    }
    
    public void setUrl(String u){
        this.url = u;
    }
    
    public String getUrl(){
        return this.url;
    }
    
    public void setTitle(String t){
        this.title = t;
    }
    
    public String getTitle(){
        return this.title;
    }
    
    public void setText(String t){
        this.text = t;
    }
    
    public String getText(){
        return this.text;
    }
    
    public String toHTML() {
        return "<html><table>" +
                "<tr><td style='font-size: 11px;'><span style='color: blue;'>" + this.title + "</span> | " + this.url + "</td></tr>" +
                "<tr><td style='padding-left: 20px;'>" + this.text + "</td></tr>" +
                "</table></html>";
    }
    
    
    @Override
    public String toString() {
        return this.title +" | " + this.url + " | " + this.text;
    }
    
}
