package org.jetbrains.teamcity.miniagentstats;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.teamcity.miniagentstats.model.Agent;
import org.jetbrains.teamcity.miniagentstats.model.Agents;

import javax.xml.bind.JAXBException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * TeamCity Agent Statistics Monitor
 * 
 * This class handles the retrieval and display of statistics from a TeamCity server:
 * - Number of builds in the build queue
 * - Total number of live agents
 * - Number of agents currently running builds
 */
public class AgentStats {
    // TeamCity client instance
    private final TeamCityClient teamCity;
    private ScheduledExecutorService scheduler;
    protected final Consumer<String> print;
    
    /**
     * Constructor initializes the TeamCity client and print consumer
     * 
     * @param print Consumer for string output
     */
    public AgentStats(Consumer<String> print) throws JAXBException {
        this.teamCity = createTeamCityClient();
        this.print = print;
    }
    
    /**
     * Constructor that accepts a TeamCityClient for testing
     * 
     * @param print Consumer for string output
     * @param teamCity TeamCityClient instance
     */
    public AgentStats(Consumer<String> print, TeamCityClient teamCity) {
        this.teamCity = teamCity;
        this.print = print;
    }
    
    /**
     * Creates a TeamCityClient instance
     * This method can be overridden in tests to provide a mock client
     */
    protected TeamCityClient createTeamCityClient() throws JAXBException {
        return new TeamCityClientImpl();
    }
    
    /**
     * Starts monitoring TeamCity statistics
     * 
     * @param intervalSeconds the interval between statistics updates in seconds
     * @param teamCityUrl the URL of the TeamCity server
     * @param teamCityToken the authentication token for the TeamCity server
     * @param outputFilePath optional path to a file where statistics will be written
     */
    public void startMonitoring(int intervalSeconds, String teamCityUrl, String teamCityToken, String outputFilePath) {
        // Remove trailing slash from URL if present
        if (teamCityUrl.endsWith("/")) {
            teamCityUrl = teamCityUrl.substring(0, teamCityUrl.length() - 1);
        }
        
        // Create authorization header value
        String authHeader = "Bearer " + teamCityToken;
        
        print.accept("TeamCity Agent Statistics Monitor");
        print.accept("Monitoring server: " + teamCityUrl);
        print.accept("Update interval: " + intervalSeconds + " seconds");
        if (outputFilePath != null) {
            print.accept("Writing statistics to: " + outputFilePath);
            // Clean the output file if it exists
            try {
                initializeOutputFile(outputFilePath);
            } catch (IOException e) {
                System.err.println("Error initializing output file: " + e.getMessage());
                System.exit(1);
            }
        }
        print.accept("Press any key to exit");
        print.accept("----------------------------------------");
        
        // Schedule periodic execution
        scheduler = Executors.newScheduledThreadPool(1);
        final String finalTeamCityUrl = teamCityUrl;
        final String finalOutputFilePath = outputFilePath;
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                fetchAndDisplayStats(finalTeamCityUrl, authHeader, finalOutputFilePath);
            } catch (Exception e) {
                System.err.println("Error fetching statistics: " + e.getMessage());
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
        print.accept("Started monitoring");
        
        // Start a thread to listen for keyboard input
        Thread keyboardListener = new Thread(() -> {
            try {
                // Wait for any key press
                new InputStreamReader(System.in).read();
                // Stop monitoring when any key is pressed
                stopMonitoring();
            } catch (IOException e) {
                System.err.println("Error reading keyboard input: " + e.getMessage());
            }
        });
        keyboardListener.setDaemon(true);
        keyboardListener.start();
        
        // Keep the application running
        try {
            keyboardListener.join();
        } catch (InterruptedException e) {
            print.accept("Application terminated");
        }
        print.accept("Finished monitoring");

    }
    
    /**
     * Fetches statistics from TeamCity server and displays them
     * If outputFilePath is provided, also writes statistics to that file
     */
    protected void fetchAndDisplayStats(String teamCityUrl, String authHeader, String outputFilePath) throws IOException, JAXBException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Get build queue information
            int queuedBuildsCount = teamCity.getQueuedBuildsCount(httpClient, teamCityUrl, authHeader);
            
            // Get agent information
            Agents agentStats = teamCity.getAgentStats(httpClient, teamCityUrl, authHeader);
            int totalAgents = agentStats.getCount();
            int busyAgents = countBusyAgents(agentStats);
            
            // Format statistics
            String stats = String.format("%s,%d,%d,%d",
                    java.time.LocalDateTime.now().toString(),
                    queuedBuildsCount, 
                    totalAgents, 
                    busyAgents);
            
            // Display statistics to stdout
            print.accept(stats);
            
            // Write statistics to file if path is provided
            if (outputFilePath != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true))) {
                    writer.write(stats);
                    writer.newLine();
                }
            }
        }
    }
    
    /**
     * Counts the number of busy agents (agents that are enabled, connected, and running a build)
     */
    private int countBusyAgents(Agents agentStats) {
        int busyCount = 0;
        List<Agent> agents = agentStats.getAgents();
        
        if (agents != null) {
            for (Agent agent : agents) {
                if (agent.isEnabled() && agent.isConnected() && agent.hasBuild()) {
                    busyCount++;
                }
            }
        }
        
        return busyCount;
    }
    
    /**
     * Initializes the output file by cleaning it if it exists
     */
    private void initializeOutputFile(String filePath) throws IOException {
        File file = new File(filePath);
        
        // Create parent directories if they don't exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // Clean the file by truncating it to zero length or creating a new empty file
        new FileWriter(file).close();
    }
    
    /**
     * Stops the monitoring scheduler
     */
    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}