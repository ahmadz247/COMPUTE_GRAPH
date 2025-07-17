package test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class Node {
    private String name;
    private List<Node> edges;
    private Message message;
    
    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<Node> getEdges() {
        return edges;
    }
    
    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }
    
    public Message getMessage() {
        return message;
    }
    
    public void setMessage(Message message) {
        this.message = message;
    }
    
    public void addEdge(Node node) {
        edges.add(node);
    }
    
    public boolean hasCycles() {
        Set<Node> visited = new HashSet<>();
        Set<Node> recursionStack = new HashSet<>();
        return hasCyclesHelper(visited, recursionStack);
    }
    
    private boolean hasCyclesHelper(Set<Node> visited, Set<Node> recursionStack) {
        if (recursionStack.contains(this)) {
            return true; // Found a cycle
        }
        
        if (visited.contains(this)) {
            return false; // Already processed this node
        }
        
        visited.add(this);
        recursionStack.add(this);
        
        // Check all neighbors
        for (Node neighbor : edges) {
            if (neighbor.hasCyclesHelper(visited, recursionStack)) {
                return true;
            }
        }
        
        recursionStack.remove(this);
        return false;
    }
}