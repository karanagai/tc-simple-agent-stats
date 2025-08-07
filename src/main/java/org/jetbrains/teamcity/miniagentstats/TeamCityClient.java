package org.jetbrains.teamcity.miniagentstats;

import org.apache.http.impl.client.CloseableHttpClient;
import org.jetbrains.teamcity.miniagentstats.model.Agents;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public interface TeamCityClient {
    /**
     * Gets the number of builds in the queue
     */
    int getQueuedBuildsCount(CloseableHttpClient httpClient, String teamCityUrl, String authHeader) throws IOException, JAXBException;
    
    /**
     * Gets agent statistics
     */
    Agents getAgentStats(CloseableHttpClient httpClient, String teamCityUrl, String authHeader) throws IOException, JAXBException;
}