package servlets;

import server.RequestParser.RequestInfo;
import graph.TopicManagerSingleton;
import graph.Topic;
import graph.Agent;
import java.io.*;

public class ResetTopicsServlet implements Servlet {
    
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        // Get all topics and reset their values
        var topicManager = TopicManagerSingleton.get();
        
        // Reset all agents (which clears their internal state)
        // We need to collect all unique agents first to avoid resetting the same agent multiple times
        java.util.Set<Agent> allAgents = new java.util.HashSet<>();
        for (Topic topic : topicManager.getTopics()) {
            allAgents.addAll(topic.subs);
            allAgents.addAll(topic.pubs);
        }
        
        // Reset each agent once
        for (Agent agent : allAgents) {
            agent.reset();
        }
        
        // Clear the lastMessage from all topics using reflection
        try {
            java.lang.reflect.Field lastMessageField = Topic.class.getDeclaredField("lastMessage");
            lastMessageField.setAccessible(true);
            
            for (Topic topic : topicManager.getTopics()) {
                lastMessageField.set(topic, null);
            }
        } catch (Exception e) {
            // If reflection fails, at least we reset the agents
            System.err.println("Warning: Could not clear topic messages: " + e.getMessage());
        }
        
        // Send success response that redirects to topic view
        String response = "<!DOCTYPE html><html><head>" +
                         "<meta http-equiv='refresh' content='0;url=/topics?reset=true'>" +
                         "</head><body>" +
                         "<h2>Topics Reset Complete</h2>" +
                         "<p>All agents have been reset.</p>" +
                         "</body></html>";
        
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                             "Content-Type: text/html\r\n" +
                             "Content-Length: " + response.length() + "\r\n" +
                             "\r\n" +
                             response;
        
        toClient.write(httpResponse.getBytes());
        toClient.flush();
    }
    
    @Override
    public void close() throws IOException {
        // No resources to close
    }
}