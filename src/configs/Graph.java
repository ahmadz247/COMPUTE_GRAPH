package configs;

import graph.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a directed graph for computational flow visualization.
 * Extends ArrayList of Node for easy iteration and manipulation.
 * 
 * SOLID Principle: Single Responsibility - This class focuses solely on
 * graph structure management and cycle detection, not rendering or business logic.
 * 
 * @author Advanced Programming Course
 */
public class Graph extends ArrayList<Node> {
    
    /**
     * Detects cycles in the directed graph using Depth-First Search (DFS).
     * A cycle exists if we can reach a node that is already in the current path.
     * 
     * Algorithm complexity: O(V + E) where V is vertices and E is edges
     * 
     * @return true if any cycle is detected, false otherwise
     */
    public boolean hasCycles() {
        // Check each node as a potential starting point for cycle detection
        // This ensures we catch cycles in disconnected components
        for (Node node : this) {
            if (node.hasCycles()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Builds the graph structure from the current TopicManager state.
     * Creates nodes for topics and agents, and edges representing their relationships.
     * 
     * Graph structure:
     * - Topics are sources that publish to subscriber agents
     * - Agents process data and publish to output topics
     * - Edges: Topic -> Subscriber Agents -> Output Topics
     * 
     * SOLID: Open/Closed Principle - New agent types can be added without
     * modifying this method, as it works with the Agent interface.
     */
    public void createFromTopics() {
        // Clear any existing graph structure
        this.clear();
        
        // Use maps to ensure each topic/agent has exactly one node
        // This prevents duplicate nodes in the graph
        Map<String, Node> topicNodes = new HashMap<>();
        Map<String, Node> agentNodes = new HashMap<>();
        
        // Phase 1: Create nodes and edges FROM topics TO their subscribers
        for (Topic topic : TopicManagerSingleton.get().getTopics()) {
            // Only create nodes for active topics (those with connections)
            if (!topic.subs.isEmpty() || !topic.pubs.isEmpty()) {
                // Create topic node with "T" prefix for identification
                Node topicNode = new Node("T" + topic.name);
                topicNodes.put(topic.name, topicNode);
                this.add(topicNode);
                
                // Create edges: Topic -> Subscriber Agents
                for (Agent subscriber : topic.subs) {
                    String agentName = subscriber.getName();
                    Node agentNode = agentNodes.get(agentName);
                    
                    // Create agent node if not exists
                    if (agentNode == null) {
                        agentNode = new Node("A" + agentName);
                        agentNodes.put(agentName, agentNode);
                        this.add(agentNode);
                    }
                    
                    // Edge represents data flow from topic to agent
                    topicNode.addEdge(agentNode);
                }
            }
        }
        
        // Phase 2: Create edges FROM agents TO their output topics
        for (Topic topic : TopicManagerSingleton.get().getTopics()) {
            Node topicNode = topicNodes.get(topic.name);
            if (topicNode != null) {
                // For each publisher agent of this topic
                for (Agent publisher : topic.pubs) {
                    Node agentNode = agentNodes.get(publisher.getName());
                    if (agentNode != null) {
                        // Edge represents data flow from agent to topic
                        agentNode.addEdge(topicNode);
                    }
                }
            }
        }
    }
}