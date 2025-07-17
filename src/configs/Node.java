package configs;

import graph.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a node in the computational graph.
 * Can be either a Topic node or an Agent node.
 * 
 * SOLID: Single Responsibility - Handles node data and relationships only.
 * The visualization and business logic are handled by other classes.
 */
public class Node {
    private String name;
    private List<Node> edges;  // Outgoing edges to other nodes
    private Message message;   // Optional message data for visualization
    
    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }
    
    // Standard getters and setters
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
    
    /**
     * Adds an outgoing edge from this node to another node.
     * @param node The destination node
     */
    public void addEdge(Node node) {
        edges.add(node);
    }
    
    /**
     * Detects if there's a cycle starting from this node using DFS.
     * Uses the "white-gray-black" DFS algorithm:
     * - White (not in any set): unvisited
     * - Gray (in recursionStack): currently being processed
     * - Black (in visited but not in recursionStack): completely processed
     * 
     * @return true if a cycle is detected, false otherwise
     */
    public boolean hasCycles() {
        Set<Node> visited = new HashSet<>();        // All processed nodes
        Set<Node> recursionStack = new HashSet<>();  // Current DFS path
        return hasCyclesHelper(visited, recursionStack);
    }
    
    /**
     * Recursive helper for cycle detection using DFS.
     * 
     * Algorithm explanation:
     * 1. If node is in recursionStack (gray), we've found a back edge = cycle
     * 2. If node is in visited (black), it's already processed = no cycle here
     * 3. Otherwise, mark as gray, visit neighbors, then mark as black
     * 
     * Time Complexity: O(V + E) where V is vertices, E is edges
     * Space Complexity: O(V) for the sets
     * 
     * @param visited Set of completely processed nodes
     * @param recursionStack Set of nodes in current DFS path
     * @return true if cycle found, false otherwise
     */
    private boolean hasCyclesHelper(Set<Node> visited, Set<Node> recursionStack) {
        // Base case 1: Found a back edge (cycle detected)
        if (recursionStack.contains(this)) {
            return true;
        }
        
        // Base case 2: Already fully processed this subtree
        if (visited.contains(this)) {
            return false;
        }
        
        // Mark node as being processed (add to current path)
        visited.add(this);
        recursionStack.add(this);
        
        // Recursively check all outgoing edges
        for (Node neighbor : edges) {
            if (neighbor.hasCyclesHelper(visited, recursionStack)) {
                return true;  // Propagate cycle detection up the call stack
            }
        }
        
        // Done processing this node, remove from current path
        recursionStack.remove(this);
        return false;
    }
}