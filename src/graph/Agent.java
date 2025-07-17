package graph;

/**
 * Core interface for computational agents in the graph system.
 * Agents can subscribe to topics, process messages, and publish results.
 * 
 * This interface enables the Strategy pattern - different agent implementations
 * can be swapped transparently as long as they implement this interface.
 * 
 * @author Advanced Programming Course
 */
public interface Agent {
    /**
     * Returns the unique name/identifier of this agent.
     * @return The agent's name
     */
    String getName();
    
    /**
     * Resets the agent to its initial state.
     * Called when the system needs to clear all computations.
     */
    void reset();
    
    /**
     * Callback method invoked when a subscribed topic publishes a message.
     * This is where the agent performs its computation.
     * 
     * @param topic The name of the topic that published the message
     * @param msg The message containing data to process
     */
    void callback(String topic, Message msg);
    
    /**
     * Cleanup method called when the agent is being removed from the system.
     * Should unsubscribe from all topics and release any resources.
     */
    void close();
}
