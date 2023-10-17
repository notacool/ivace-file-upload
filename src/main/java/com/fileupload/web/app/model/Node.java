package com.fileupload.web.app.model;

public class Node {
    
    public String name;

    public PropertiesObject properties;
    
    public Node() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PropertiesObject getProperties() {
        return properties;
    }

    public void setProperties(PropertiesObject properties) {
        this.properties = properties;
    }
    
}
