package test;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.Constructor;

public class GenericConfig implements Config {
    private String configFilePath;
    private List<Agent> agents = new ArrayList<>();
    private List<ParallelAgent> parallelAgents = new ArrayList<>();
    
    public void setConfFile(String path) {
        this.configFilePath = path;
    }
    
    @Override
    public void create() {
        if (configFilePath == null) {
            throw new IllegalStateException("Configuration file path not set");
        }
        
        try {
            // Read all lines from the configuration file
            List<String> lines = Files.readAllLines(Paths.get(configFilePath));
            
            // Check if valid config file (lines divisible by 3)
            if (lines.size() % 3 != 0) {
                throw new IllegalArgumentException("Invalid configuration file: number of lines must be divisible by 3");
            }
            
            // Process each group of 3 lines
            for (int i = 0; i < lines.size(); i += 3) {
                String className = lines.get(i).trim();
                String inputTopics = lines.get(i + 1).trim();
                String outputTopics = lines.get(i + 2).trim();
                
                // Parse input and output topics
                String[] subs = inputTopics.isEmpty() ? new String[0] : inputTopics.split(",");
                String[] pubs = outputTopics.isEmpty() ? new String[0] : outputTopics.split(",");
                
                // Trim whitespace from topic names
                for (int j = 0; j < subs.length; j++) {
                    subs[j] = subs[j].trim();
                }
                for (int j = 0; j < pubs.length; j++) {
                    pubs[j] = pubs[j].trim();
                }
                
                // Create agent instance using reflection
                Agent agent = createAgent(className, subs, pubs);
                agents.add(agent);
                
                // Wrap in ParallelAgent with queue capacity of 100
                ParallelAgent parallelAgent = new ParallelAgent(agent, 100);
                parallelAgents.add(parallelAgent);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Error reading configuration file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error creating agents: " + e.getMessage(), e);
        }
    }
    
    private Agent createAgent(String className, String[] subs, String[] pubs) throws Exception {
        // Load the class
        Class<?> clazz = Class.forName(className);
        
        // Check if it implements Agent interface
        if (!Agent.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class " + className + " does not implement Agent interface");
        }
        
        // Get constructor that takes String[], String[]
        Constructor<?> constructor = clazz.getConstructor(String[].class, String[].class);
        
        // Create instance
        return (Agent) constructor.newInstance((Object) subs, (Object) pubs);
    }
    
    @Override
    public String getName() {
        return "Generic Configuration";
    }
    
    @Override
    public int getVersion() {
        return 1;
    }
    
    @Override
    public void close() {
        // Close all ParallelAgents (which will close the wrapped agents)
        for (ParallelAgent parallelAgent : parallelAgents) {
            parallelAgent.close();
        }
        agents.clear();
        parallelAgents.clear();
    }
}