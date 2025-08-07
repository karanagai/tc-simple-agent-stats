package org.jetbrains.teamcity.miniagentstats.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "builds")
@XmlAccessorType(XmlAccessType.FIELD)
public class BuildQueue {
    
    @XmlAttribute
    private int count;
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
}