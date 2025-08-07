package org.jetbrains.teamcity.miniagentstats.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Agent {
    
    @XmlAttribute
    private boolean enabled;
    
    @XmlAttribute
    private boolean connected;
    
    @XmlElement(name = "build")
    private Build build;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    public Build getBuild() {
        return build;
    }
    
    public void setBuild(Build build) {
        this.build = build;
    }
    
    public boolean hasBuild() {
        return build != null;
    }
    
    public static class Build {
        // This class is just a marker to check if build exists
        // We don't need any properties from it
    }
}