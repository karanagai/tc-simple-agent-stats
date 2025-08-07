package org.jetbrains.teamcity.miniagentstats;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jetbrains.teamcity.miniagentstats.model.Agents;
import org.jetbrains.teamcity.miniagentstats.model.BuildQueue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;

public class TeamCityClientImpl implements TeamCityClient {
    private final JAXBContext buildQueueContext;
    private final JAXBContext agentsContext;

    public TeamCityClientImpl() throws JAXBException {
        this.buildQueueContext = JAXBContext.newInstance(BuildQueue.class);
        this.agentsContext = JAXBContext.newInstance(Agents.class);
    }

    /**
     * Gets the number of builds in the queue
     */
    @Override
    public int getQueuedBuildsCount(CloseableHttpClient httpClient, String teamCityUrl, String authHeader) throws IOException, JAXBException {
        HttpGet request = new HttpGet(teamCityUrl + "/app/rest/buildQueue");
        request.setHeader(HttpHeaders.ACCEPT, "application/xml");
        request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("Failed to get build queue: HTTP " + response.getStatusLine().getStatusCode());
            }
            
            String responseBody = EntityUtils.toString(response.getEntity());
            Unmarshaller unmarshaller = buildQueueContext.createUnmarshaller();
            BuildQueue buildQueue = (BuildQueue) unmarshaller.unmarshal(new StringReader(responseBody));
            return buildQueue.getCount();
        }
    }
    
    /**
     * Gets agent statistics
     */
    @Override
    public Agents getAgentStats(CloseableHttpClient httpClient, String teamCityUrl, String authHeader) throws IOException, JAXBException {
        HttpGet request = new HttpGet(teamCityUrl + "/app/rest/agents?fields=count,agent(id,enabled,connected,build)");
        request.setHeader(HttpHeaders.ACCEPT, "application/xml");
        request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("Failed to get agents: HTTP " + response.getStatusLine().getStatusCode());
            }
            
            String responseBody = EntityUtils.toString(response.getEntity());
            Unmarshaller unmarshaller = agentsContext.createUnmarshaller();
            return (Agents) unmarshaller.unmarshal(new StringReader(responseBody));
        }
    }
}