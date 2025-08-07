package org.jetbrains.teamcity.miniagentstats;

import org.apache.http.impl.client.CloseableHttpClient;
import org.jetbrains.teamcity.miniagentstats.model.Agent;
import org.jetbrains.teamcity.miniagentstats.model.Agents;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of TeamCityClient for testing
 */
public class MockTeamCityClient implements TeamCityClient {
    
    private int queuedBuildsCount = 0;
    private int totalAgentsCount = 0;
    private List<Agent> agents = new ArrayList<>();
    
    @Override
    public int getQueuedBuildsCount(CloseableHttpClient httpClient, String teamCityUrl, String authHeader) 
            throws IOException, JAXBException {
        return queuedBuildsCount;
    }
    
    @Override
    public Agents getAgentStats(CloseableHttpClient httpClient, String teamCityUrl, String authHeader) 
            throws IOException, JAXBException {
        Agents agentsObj = new Agents();
        agentsObj.setCount(totalAgentsCount);
        agentsObj.setAgents(agents);
        return agentsObj;
    }
    
    /**
     * Sets the number of queued builds to return
     */
    public void setQueuedBuildsCount(int count) {
        this.queuedBuildsCount = count;
    }
    
    /**
     * Sets the total number of agents to return
     */
    public void setTotalAgentsCount(int count) {
        this.totalAgentsCount = count;
    }
    
    /**
     * Sets the list of agents to return
     */
    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }
    
    /**
     * Helper method to create an agent with specified properties
     */
    public static Agent createAgent(boolean enabled, boolean connected, boolean hasBuild) {
        Agent agent = new Agent();
        agent.setEnabled(enabled);
        agent.setConnected(connected);
        
        if (hasBuild) {
            agent.setBuild(new Agent.Build());
        }
        
        return agent;
    }
}