package configs;

/**
 * Interface for configuration implementations that set up computational graphs.
 * Configurations define which agents to create and how they connect via topics.
 * 
 * Implementations can load from files, create programmatically, or use any
 * other method to define the graph structure.
 * 
 * @author Advanced Programming Course
 */
public interface Config {
    /**
     * Creates and initializes all agents and topics defined by this configuration.
     * This method should set up the entire computational graph.
     * 
     * @throws RuntimeException if configuration is invalid or creation fails
     */
    void create();
    
    /**
     * Returns a human-readable name for this configuration.
     * @return The configuration name
     */
    String getName();
    
    /**
     * Returns the version number of this configuration format.
     * @return The version number
     */
    int getVersion();
    
    /**
     * Closes all agents and cleans up resources created by this configuration.
     * Called when switching to a new configuration or shutting down.
     */
    void close();
}