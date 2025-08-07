package org.jetbrains.teamcity.miniagentstats;

import javax.xml.bind.JAXBException;

/**
 * TeamCity Agent Statistics Monitor
 * 
 * This application periodically retrieves and displays statistics from a TeamCity server:
 * - Number of builds in the build queue
 * - Total number of live agents
 * - Number of agents currently running builds
 */
public class Main {
    
    public static void main(String[] args) {
        // Validate command-line arguments
        if (args.length < 3 || args.length > 4) {
            System.err.println("Usage: java -jar AgentStats.jar <interval_seconds> <teamcity_url> <teamcity_token> [output_file_path]");
            System.exit(1);
        }
        
        try {
            // Parse command-line arguments
            int intervalSeconds = Integer.parseInt(args[0]);
            String teamCityUrl = args[1];
            String teamCityToken = args[2];
            String outputFilePath = args.length == 4 ? args[3] : null;
            
            if (intervalSeconds <= 0) {
                System.err.println("Interval must be a positive number of seconds");
                System.exit(1);
            }
            
            // Remove trailing slash from URL if present
            if (teamCityUrl.endsWith("/")) {
                teamCityUrl = teamCityUrl.substring(0, teamCityUrl.length() - 1);
            }
            
            // Create and start the AgentStats instance
            AgentStats agentStats = new AgentStats(System.out::println);
            agentStats.startMonitoring(intervalSeconds, teamCityUrl, teamCityToken, outputFilePath);
        } catch (NumberFormatException e) {
            System.err.println("Interval must be a valid integer");
            System.exit(1);
        } catch (JAXBException e) {
            System.err.println("Error initializing TeamCity client: " + e.getMessage());
            System.exit(1);
        }
    }
}