package org.jetbrains.teamcity.miniagentstats.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "agents")
@XmlAccessorType(XmlAccessType.FIELD)
public class Agents {
    
    @XmlAttribute
    private int count;
    
    @XmlElement(name = "agent")
    private List<Agent> agent;
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public List<Agent> getAgents() {
        return agent;
    }
    
    public void setAgents(List<Agent> agent) {
        this.agent = agent;
    }
}