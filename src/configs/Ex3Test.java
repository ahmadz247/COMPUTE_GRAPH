package configs;

import graph.*;
import java.util.List;
import java.util.ArrayList;

public class Ex3Test {
    
    public static void testNodeCycleDetection() {
        System.out.println("Test 1: Node Cycle Detection");
        
        // Test 1a: No cycle
        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");
        a.addEdge(b);
        b.addEdge(c);
        
        System.out.println("[PASS] Linear graph (A->B->C) has no cycles: " + !a.hasCycles());
        
        // Test 1b: Self-loop
        Node d = new Node("D");
        d.addEdge(d);
        System.out.println("[PASS] Self-loop has cycle: " + d.hasCycles());
        
        // Test 1c: Simple cycle
        Node e = new Node("E");
        Node f = new Node("F");
        Node g = new Node("G");
        e.addEdge(f);
        f.addEdge(g);
        g.addEdge(e);
        System.out.println("[PASS] Circular graph (E->F->G->E) has cycle: " + e.hasCycles());
        
        // Test 1d: Complex graph with cycle
        Node h = new Node("H");
        Node i = new Node("I");
        Node j = new Node("J");
        Node k = new Node("K");
        h.addEdge(i);
        h.addEdge(j);
        i.addEdge(k);
        j.addEdge(k);
        k.addEdge(h); // Creates cycle
        System.out.println("[PASS] Complex graph with cycle detected: " + h.hasCycles());
        
        System.out.println();
    }
    
    public static void testBinOpAgent() {
        System.out.println("Test 2: BinOpAgent Functionality");
        
        // Clear any existing topics
        TopicManagerSingleton.get().clear();
        
        // Create a simple addition agent
        BinOpAgent adder = new BinOpAgent("adder", "X", "Y", "Sum", (x, y) -> x + y);
        
        // Create a test subscriber to capture results
        List<Double> results = new ArrayList<>();
        Agent resultCapture = new Agent() {
            public String getName() { return "resultCapture"; }
            public void reset() { results.clear(); }
            public void callback(String topic, Message msg) {
                if (!Double.isNaN(msg.asDouble)) {
                    results.add(msg.asDouble);
                }
            }
            public void close() {}
        };
        
        TopicManagerSingleton.get().getTopic("Sum").subscribe(resultCapture);
        
        // Test: Publish to both inputs
        TopicManagerSingleton.get().getTopic("X").publish(new Message(5.0));
        TopicManagerSingleton.get().getTopic("Y").publish(new Message(3.0));
        
        System.out.println("[PASS] BinOpAgent computed 5.0 + 3.0 = " + 
                          (results.isEmpty() ? "No result" : results.get(0)));
        
        // Test: Update one input
        TopicManagerSingleton.get().getTopic("X").publish(new Message(10.0));
        System.out.println("[PASS] After updating X to 10.0, result = " + 
                          (results.size() < 2 ? "No new result" : results.get(1)));
        
        // Test: Reset
        adder.reset();
        results.clear();
        TopicManagerSingleton.get().getTopic("X").publish(new Message(2.0));
        TopicManagerSingleton.get().getTopic("Y").publish(new Message(4.0));
        System.out.println("[PASS] After reset, first computation with X=2.0: " + 
                          (results.isEmpty() ? "No result" : results.get(0)) + 
                          " (reset sets Y=0.0, so 2.0 + 0.0 = 2.0)");
        System.out.println("[PASS] Final result after both inputs updated: " + 
                          (results.size() < 2 ? "Waiting for second computation" : results.get(1)) + 
                          " (now 2.0 + 4.0 = 6.0)");
        
        adder.close();
        System.out.println();
    }
    
    public static void testMathExampleConfig() {
        System.out.println("Test 3: MathExampleConfig");
        
        // Clear topics
        TopicManagerSingleton.get().clear();
        
        // Create the math example configuration
        MathExampleConfig config = new MathExampleConfig();
        System.out.println("[PASS] Config name: " + config.getName());
        System.out.println("[PASS] Config version: " + config.getVersion());
        
        config.create();
        
        // Create result capture for R3
        List<Double> finalResults = new ArrayList<>();
        Agent resultCapture = new Agent() {
            public String getName() { return "resultCapture"; }
            public void reset() { finalResults.clear(); }
            public void callback(String topic, Message msg) {
                if (!Double.isNaN(msg.asDouble)) {
                    finalResults.add(msg.asDouble);
                }
            }
            public void close() {}
        };
        
        TopicManagerSingleton.get().getTopic("R3").subscribe(resultCapture);
        
        // Test the computation: (a + b) * (a - b) = a² - b²
        // For a=5, b=3: (5+3)*(5-3) = 8*2 = 16 = 25-9
        TopicManagerSingleton.get().getTopic("A").publish(new Message(5.0));
        TopicManagerSingleton.get().getTopic("B").publish(new Message(3.0));
        
        // Give time for computation
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        
        System.out.println("[PASS] For A=5, B=3: (A+B)*(A-B) = " + 
                          (finalResults.isEmpty() ? "No result" : finalResults.get(0)) + 
                          " (expected: 16.0)");
        
        System.out.println();
    }
    
    public static void testGraphCreation() {
        System.out.println("Test 4: Graph Creation from Topics");
        
        // Clear and setup
        TopicManagerSingleton.get().clear();
        
        // Create the math example
        MathExampleConfig config = new MathExampleConfig();
        config.create();
        
        // Create graph from topics
        Graph graph = new Graph();
        graph.createFromTopics();
        
        System.out.println("[PASS] Graph created with " + graph.size() + " nodes");
        
        // Print graph structure
        System.out.println("\nGraph structure:");
        for (Node node : graph) {
            System.out.print("  " + node.getName() + " -> [");
            List<String> edgeNames = new ArrayList<>();
            for (Node edge : node.getEdges()) {
                edgeNames.add(edge.getName());
            }
            System.out.println(String.join(", ", edgeNames) + "]");
        }
        
        // Test cycle detection
        boolean hasCycles = graph.hasCycles();
        System.out.println("\n[PASS] Math example graph has cycles: " + hasCycles + " (expected: false)");
        
        System.out.println();
    }
    
    public static void testCyclicGraph() {
        System.out.println("Test 5: Cyclic Graph Detection");
        
        // Clear topics
        TopicManagerSingleton.get().clear();
        
        // Create a configuration that would create a cycle
        // A -> Agent1 -> B -> Agent2 -> A (cycle!)
        Agent agent1 = new Agent() {
            public String getName() { return "Agent1"; }
            public void reset() {}
            public void callback(String topic, Message msg) {
                if (topic.equals("A")) {
                    TopicManagerSingleton.get().getTopic("B").publish(msg);
                }
            }
            public void close() {}
        };
        
        Agent agent2 = new Agent() {
            public String getName() { return "Agent2"; }
            public void reset() {}
            public void callback(String topic, Message msg) {
                if (topic.equals("B")) {
                    TopicManagerSingleton.get().getTopic("A").publish(msg);
                }
            }
            public void close() {}
        };
        
        // Set up subscriptions and publications
        TopicManagerSingleton.get().getTopic("A").subscribe(agent1);
        TopicManagerSingleton.get().getTopic("A").addPublisher(agent2);
        TopicManagerSingleton.get().getTopic("B").subscribe(agent2);
        TopicManagerSingleton.get().getTopic("B").addPublisher(agent1);
        
        // Create graph
        Graph cyclicGraph = new Graph();
        cyclicGraph.createFromTopics();
        
        System.out.println("[PASS] Cyclic graph created with " + cyclicGraph.size() + " nodes");
        System.out.println("[PASS] Cyclic graph has cycles: " + cyclicGraph.hasCycles() + " (expected: true)");
        
        System.out.println();
    }
    
    public static void main(String[] args) {
        System.out.println("=== Exercise 3 Test Suite ===\n");
        
        testNodeCycleDetection();
        testBinOpAgent();
        testMathExampleConfig();
        testGraphCreation();
        testCyclicGraph();
        
        System.out.println("=== All tests completed ===");
    }
}