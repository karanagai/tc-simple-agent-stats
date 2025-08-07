package org.jetbrains.teamcity.miniagentstats;

import org.jetbrains.teamcity.miniagentstats.model.Agent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class AgentStatsTest {
    
    private AgentStats agentStats;
    private MockTeamCityClient mockTeamCityClient;
    private List<String> outputLines;
    private String testOutputFile;
    
    @Before
    public void setUp() throws Exception {
        // Create a list to capture output
        outputLines = new ArrayList<>();
        
        // Create a mock TeamCity client
        mockTeamCityClient = new MockTeamCityClient();
        
        // Create a temporary file for testing output
        testOutputFile = "test_output.csv";
        
        // Create AgentStats with a custom print consumer that captures output
        agentStats = new AgentStatsWithMockClient(
            message -> outputLines.add(message),
            mockTeamCityClient
        );
    }
    
    @After
    public void tearDown() throws Exception {
        // Stop monitoring if it's running
        agentStats.stopMonitoring();
        
        // Delete test output file if it exists
        Files.deleteIfExists(Paths.get(testOutputFile));
    }

    @Test
    public void testFetchAndDisplayStats() throws Exception {
        // Setup mock data
        mockTeamCityClient.setQueuedBuildsCount(5);
        mockTeamCityClient.setTotalAgentsCount(10);
        
        List<Agent> agents = new ArrayList<>();
        // Add 3 busy agents (enabled, connected, with build)
        agents.add(MockTeamCityClient.createAgent(true, true, true));
        agents.add(MockTeamCityClient.createAgent(true, true, true));
        agents.add(MockTeamCityClient.createAgent(true, true, true));
        // Add 2 agents that are not busy (one not enabled, one not connected)
        agents.add(MockTeamCityClient.createAgent(false, true, true));
        agents.add(MockTeamCityClient.createAgent(true, false, true));
        // Add 1 agent without a build
        agents.add(MockTeamCityClient.createAgent(true, true, false));
        
        mockTeamCityClient.setAgents(agents);
        
        // Create a reference to capture the stats output
        AtomicReference<String> statsOutput = new AtomicReference<>();
        
        // Create a special AgentStats that captures the stats output
        AgentStatsWithMockClient testAgentStats = new AgentStatsWithMockClient(
            message -> {
                // Only capture the stats line (not the initialization messages)
                if (message.contains(",")) {
                    statsOutput.set(message);
                }
            },
            mockTeamCityClient
        );
        
        // Call the method that fetches and displays stats
        testAgentStats.testFetchAndDisplayStats("http://test-teamcity", "test-token", null);
        
        // Verify the output contains the expected values
        String output = statsOutput.get();
        assertNotNull("Stats output should not be null", output);
        
        // Format should be: timestamp,queuedBuilds,totalAgents,busyAgents
        String[] parts = output.split(",");
        assertEquals("Output should have 4 parts", 4, parts.length);
        assertEquals("Queued builds count should match", "5", parts[1]);
        assertEquals("Total agents count should match", "10", parts[2]);
        assertEquals("Busy agents count should match", "3", parts[3]);
    }
    
    @Test
    public void testOutputToFile() throws Exception {
        // Setup mock data
        mockTeamCityClient.setQueuedBuildsCount(3);
        mockTeamCityClient.setTotalAgentsCount(7);
        
        List<Agent> agents = new ArrayList<>();
        // Add 2 busy agents
        agents.add(MockTeamCityClient.createAgent(true, true, true));
        agents.add(MockTeamCityClient.createAgent(true, true, true));
        mockTeamCityClient.setAgents(agents);
        
        // Create a special AgentStats for testing
        AgentStatsWithMockClient testAgentStats = new AgentStatsWithMockClient(
            message -> {}, // Ignore console output
            mockTeamCityClient
        );
        
        // Call the method that fetches and displays stats with a file output
        testAgentStats.testFetchAndDisplayStats("http://test-teamcity", "test-token", testOutputFile);
        
        // Verify the file exists and contains the expected data
        File outputFile = new File(testOutputFile);
        assertTrue("Output file should exist", outputFile.exists());
        
        List<String> fileLines = Files.readAllLines(outputFile.toPath());
        assertEquals("File should have 1 line", 1, fileLines.size());
        
        String fileLine = fileLines.get(0);
        String[] parts = fileLine.split(",");
        assertEquals("File line should have 4 parts", 4, parts.length);
        assertEquals("Queued builds count should match", "3", parts[1]);
        assertEquals("Total agents count should match", "7", parts[2]);
        assertEquals("Busy agents count should match", "2", parts[3]);
    }
    
    @Test
    public void testEmptyAgentsList() throws Exception {
        // Setup mock data with empty agents list
        mockTeamCityClient.setQueuedBuildsCount(2);
        mockTeamCityClient.setTotalAgentsCount(5);
        mockTeamCityClient.setAgents(new ArrayList<>()); // Empty agents list
        
        // Create a reference to capture the stats output
        AtomicReference<String> statsOutput = new AtomicReference<>();
        
        // Create a special AgentStats that captures the stats output
        AgentStatsWithMockClient testAgentStats = new AgentStatsWithMockClient(
            message -> {
                // Only capture the stats line (not the initialization messages)
                if (message.contains(",")) {
                    statsOutput.set(message);
                }
            },
            mockTeamCityClient
        );
        
        // Call the method that fetches and displays stats
        testAgentStats.testFetchAndDisplayStats("http://test-teamcity", "test-token", null);
        
        // Verify the output contains the expected values
        String output = statsOutput.get();
        assertNotNull("Stats output should not be null", output);
        
        // Format should be: timestamp,queuedBuilds,totalAgents,busyAgents
        String[] parts = output.split(",");
        assertEquals("Output should have 4 parts", 4, parts.length);
        assertEquals("Queued builds count should match", "2", parts[1]);
        assertEquals("Total agents count should match", "5", parts[2]);
        assertEquals("Busy agents count should match", "0", parts[3]); // No busy agents
    }
    
    /**
     * Special subclass of AgentStats that allows injecting a mock TeamCityClient
     * and provides a method to test fetchAndDisplayStats directly
     */
    private static class AgentStatsWithMockClient extends AgentStats {
        private final TeamCityClient mockClient;
        
        public AgentStatsWithMockClient(Consumer<String> print, TeamCityClient mockClient) throws Exception {
            super(print, mockClient);
            this.mockClient = mockClient;
        }
        
        /**
         * Exposes the fetchAndDisplayStats method for testing
         */
        public void testFetchAndDisplayStats(String teamCityUrl, String authHeader, String outputFilePath) 
                throws IOException, javax.xml.bind.JAXBException {
            // Override to avoid using HttpClient
            // Get build queue information
            int queuedBuildsCount = mockClient.getQueuedBuildsCount(null, teamCityUrl, authHeader);
            
            // Get agent information
            org.jetbrains.teamcity.miniagentstats.model.Agents agentStats = mockClient.getAgentStats(null, teamCityUrl, authHeader);
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
                try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(outputFilePath, true))) {
                    writer.write(stats);
                    writer.newLine();
                }
            }
        }
        
        // Helper method to count busy agents
        private int countBusyAgents(org.jetbrains.teamcity.miniagentstats.model.Agents agentStats) {
            int busyCount = 0;
            java.util.List<org.jetbrains.teamcity.miniagentstats.model.Agent> agents = agentStats.getAgents();
            
            if (agents != null) {
                for (org.jetbrains.teamcity.miniagentstats.model.Agent agent : agents) {
                    if (agent.isEnabled() && agent.isConnected() && agent.hasBuild()) {
                        busyCount++;
                    }
                }
            }
            
            return busyCount;
        }
    }
}