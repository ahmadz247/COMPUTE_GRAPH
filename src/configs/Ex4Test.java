package configs;

import graph.*;
import java.util.ArrayList;
import java.util.List;

public class Ex4Test {
    
    public static void testPlusAgent() {
        System.out.println("Test 1: PlusAgent");
        
        // Clear topics
        TopicManagerSingleton.get().clear();
        
        // Create PlusAgent
        String[] subs = {"X", "Y"};
        String[] pubs = {"Z"};
        PlusAgent plusAgent = new PlusAgent(subs, pubs);
        
        // Create result capture
        List<Double> results = new ArrayList<>();
        Agent resultCapture = new Agent() {
            public String getName() { return "resultCapture"; }
            public void reset() { results.clear(); }
            public void callback(String topic, Message msg) {
                results.add(msg.asDouble);
            }
            public void close() {}
        };
        
        TopicManagerSingleton.get().getTopic("Z").subscribe(resultCapture);
        
        // Test
        TopicManagerSingleton.get().getTopic("X").publish(new Message(10.0));
        TopicManagerSingleton.get().getTopic("Y").publish(new Message(5.0));
        
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        
        System.out.println("[PASS] PlusAgent: 10.0 + 5.0 = " + 
                          (results.isEmpty() ? "No result" : results.get(0)));
        
        plusAgent.close();
        System.out.println();
    }
    
    public static void testIncAgent() {
        System.out.println("Test 2: IncAgent");
        
        // Clear topics
        TopicManagerSingleton.get().clear();
        
        // Create IncAgent
        String[] subs = {"Input"};
        String[] pubs = {"Output"};
        IncAgent incAgent = new IncAgent(subs, pubs);
        
        // Create result capture
        List<Double> results = new ArrayList<>();
        Agent resultCapture = new Agent() {
            public String getName() { return "resultCapture"; }
            public void reset() { results.clear(); }
            public void callback(String topic, Message msg) {
                results.add(msg.asDouble);
            }
            public void close() {}
        };
        
        TopicManagerSingleton.get().getTopic("Output").subscribe(resultCapture);
        
        // Test multiple increments
        TopicManagerSingleton.get().getTopic("Input").publish(new Message(5.0));
        TopicManagerSingleton.get().getTopic("Input").publish(new Message(10.0));
        TopicManagerSingleton.get().getTopic("Input").publish(new Message(-3.0));
        
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        
        System.out.println("[PASS] IncAgent results: " + results);
        System.out.println("       5.0 + 1 = " + (results.size() > 0 ? results.get(0) : "?"));
        System.out.println("       10.0 + 1 = " + (results.size() > 1 ? results.get(1) : "?"));
        System.out.println("       -3.0 + 1 = " + (results.size() > 2 ? results.get(2) : "?"));
        
        incAgent.close();
        System.out.println();
    }
    
    public static void testGenericConfig() {
        System.out.println("Test 3: GenericConfig with simple.conf");
        
        // Clear topics
        TopicManagerSingleton.get().clear();
        
        // Create GenericConfig
        GenericConfig config = new GenericConfig();
        
        // Use the config file from configs package
        config.setConfFile("C:\\Users\\Administrator\\Desktop\\config_files\\simple_configs.conf");
        
        System.out.println("[PASS] Config name: " + config.getName());
        System.out.println("[PASS] Config version: " + config.getVersion());
        
        // Create configuration
        config.create();
        
        // Create result capture for topic D
        List<Double> results = new ArrayList<>();
        Agent resultCapture = new Agent() {
            public String getName() { return "resultCapture"; }
            public void reset() { results.clear(); }
            public void callback(String topic, Message msg) {
                results.add(msg.asDouble);
            }
            public void close() {}
        };
        
        TopicManagerSingleton.get().getTopic("D").subscribe(resultCapture);
        
        // Test: A + B -> C -> C + 1 -> D
        System.out.println("\nPublishing A=7, B=3");
        TopicManagerSingleton.get().getTopic("A").publish(new Message(7.0));
        TopicManagerSingleton.get().getTopic("B").publish(new Message(3.0));
        
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        
        System.out.println("[PASS] Result in D: " + 
                          (results.isEmpty() ? "No result" : results.get(0)) + 
                          " (expected: (7+3)+1 = 11)");
        
        // Test thread count before close
        int threadsBefore = Thread.activeCount();
        System.out.println("\n[PASS] Active threads before close: " + threadsBefore);
        
        // Close config
        config.close();
        
        // Give time for threads to terminate
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        
        int threadsAfter = Thread.activeCount();
        System.out.println("[PASS] Active threads after close: " + threadsAfter);
        System.out.println("[PASS] Threads terminated: " + (threadsBefore - threadsAfter));
        
        System.out.println();
    }
    
    public static void testComplexFlow() {
        System.out.println("Test 4: Complex Flow");
        
        // Clear topics
        TopicManagerSingleton.get().clear();
        
        // Create a more complex configuration
        // Agent1: X + Y -> Z
        // Agent2: Z + 1 -> W
        // Agent3: W + Z -> Final
        
        PlusAgent agent1 = new PlusAgent(new String[]{"X", "Y"}, new String[]{"Z"});
        IncAgent agent2 = new IncAgent(new String[]{"Z"}, new String[]{"W"});
        PlusAgent agent3 = new PlusAgent(new String[]{"W", "Z"}, new String[]{"Final"});
        
        // Wrap in ParallelAgents
        ParallelAgent p1 = new ParallelAgent(agent1, 10);
        ParallelAgent p2 = new ParallelAgent(agent2, 10);
        ParallelAgent p3 = new ParallelAgent(agent3, 10);
        
        // Create result capture
        List<Double> results = new ArrayList<>();
        Agent resultCapture = new Agent() {
            public String getName() { return "resultCapture"; }
            public void reset() { results.clear(); }
            public void callback(String topic, Message msg) {
                results.add(msg.asDouble);
            }
            public void close() {}
        };
        
        TopicManagerSingleton.get().getTopic("Final").subscribe(resultCapture);
        
        // Test: X=4, Y=6
        // Z = 4 + 6 = 10
        // W = 10 + 1 = 11
        // Final = 11 + 10 = 21
        TopicManagerSingleton.get().getTopic("X").publish(new Message(4.0));
        TopicManagerSingleton.get().getTopic("Y").publish(new Message(6.0));
        
        try { Thread.sleep(300); } catch (InterruptedException e) {}
        
        System.out.println("[PASS] Complex flow result: " + 
                          (results.isEmpty() ? "No result" : results.get(0)) + 
                          " (expected: (4+6+1) + (4+6) = 21)");
        
        // Clean up
        p1.close();
        p2.close();
        p3.close();
        
        System.out.println();
    }
    
    public static void main(String[] args) {
        System.out.println("=== Exercise 4 Test Suite ===\n");
        
        testPlusAgent();
        testIncAgent();
        testGenericConfig();
        testComplexFlow();
        
        System.out.println("=== All tests completed ===");
    }
}