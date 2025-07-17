package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph extends ArrayList<Node> {
    
    public boolean hasCycles() {
        // Check if any node in the graph has cycles
        for (Node node : this) {
            if (node.hasCycles()) {
                return true;
            }
        }
        return false;
    }
    
    public void createFromTopics() {
        // Clear existing nodes
        this.clear();
        
        // Maps to avoid creating duplicate nodes
        Map<String, Node> topicNodes = new HashMap<>();
        Map<String, Node> agentNodes = new HashMap<>();
        
        // Create nodes for all topics and their connections
        for (Topic topic : TopicManagerSingleton.get().getTopics()) {
            // Create topic node if it has subscribers or publishers
            if (!topic.subs.isEmpty() || !topic.pubs.isEmpty()) {
                Node topicNode = new Node("T" + topic.name);
                topicNodes.put(topic.name, topicNode);
                this.add(topicNode);
                
                // Create edges from topic to its subscriber agents
                for (Agent subscriber : topic.subs) {
                    String agentName = subscriber.getName();
                    Node agentNode = agentNodes.get(agentName);
                    if (agentNode == null) {
                        agentNode = new Node("A" + agentName);
                        agentNodes.put(agentName, agentNode);
                        this.add(agentNode);
                    }
                    topicNode.addEdge(agentNode);
                }
            }
        }
        
        // Create edges from agent nodes to their published topics
        for (Topic topic : TopicManagerSingleton.get().getTopics()) {
            Node topicNode = topicNodes.get(topic.name);
            if (topicNode != null) {
                for (Agent publisher : topic.pubs) {
                    Node agentNode = agentNodes.get(publisher.getName());
                    if (agentNode != null) {
                        agentNode.addEdge(topicNode);
                    }
                }
            }
        }
    }
}