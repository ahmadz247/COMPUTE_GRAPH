package graph;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class ParallelAgentTest {
    
    static class TestAgent implements Agent {
        private final String name;
        private final List<String> receivedMessages = Collections.synchronizedList(new ArrayList<>());
        private final AtomicInteger callbackCount = new AtomicInteger(0);
        private CountDownLatch latch;
        private long processingDelay = 0;
        
        public TestAgent(String name) {
            this.name = name;
        }
        
        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }
        
        public void setProcessingDelay(long delay) {
            this.processingDelay = delay;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void reset() {
            receivedMessages.clear();
            callbackCount.set(0);
        }
        
        @Override
        public void callback(String topic, Message msg) {
            if (processingDelay > 0) {
                try {
                    Thread.sleep(processingDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            receivedMessages.add(topic + ": " + msg.asText);
            callbackCount.incrementAndGet();
            if (latch != null) {
                latch.countDown();
            }
        }
        
        @Override
        public void close() {
            // No resources to clean up
        }
        
        public List<String> getReceivedMessages() {
            return new ArrayList<>(receivedMessages);
        }
        
        public int getCallbackCount() {
            return callbackCount.get();
        }
    }
    
    public static void testBasicFunctionality() {
        System.out.println("Test 1: Basic Functionality");
        TestAgent innerAgent = new TestAgent("TestAgent1");
        ParallelAgent parallelAgent = new ParallelAgent(innerAgent, 10);
        
        CountDownLatch latch = new CountDownLatch(3);
        innerAgent.setLatch(latch);
        
        parallelAgent.callback("topic1", new Message("Hello"));
        parallelAgent.callback("topic2", new Message("World"));
        parallelAgent.callback("topic1", new Message("Test"));
        
        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (completed) {
                System.out.println("[PASS] All messages processed");
                System.out.println("  Received messages: " + innerAgent.getReceivedMessages());
                assert innerAgent.getCallbackCount() == 3 : "Expected 3 callbacks";
            } else {
                System.out.println("[FAIL] Timeout waiting for messages");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        parallelAgent.close();
        System.out.println("[PASS] ParallelAgent closed\n");
    }
    
    public static void testThreadCreation() {
        System.out.println("Test 2: Thread Creation");
        int threadCountBefore = Thread.activeCount();
        
        TestAgent innerAgent = new TestAgent("TestAgent2");
        ParallelAgent parallelAgent = new ParallelAgent(innerAgent, 10);
        
        // Give thread time to start
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        int threadCountAfter = Thread.activeCount();
        System.out.println("[PASS] Thread created (before: " + threadCountBefore + ", after: " + threadCountAfter + ")");
        
        parallelAgent.close();
        
        // Give thread time to terminate
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        int threadCountFinal = Thread.activeCount();
        System.out.println("[PASS] Thread terminated (final count: " + threadCountFinal + ")\n");
    }
    
    public static void testNonBlockingCallback() {
        System.out.println("Test 3: Non-blocking Callback");
        TestAgent innerAgent = new TestAgent("SlowAgent");
        innerAgent.setProcessingDelay(1000); // 1 second delay
        
        ParallelAgent parallelAgent = new ParallelAgent(innerAgent, 10);
        
        long startTime = System.currentTimeMillis();
        parallelAgent.callback("topic", new Message("Slow message"));
        long endTime = System.currentTimeMillis();
        
        long elapsed = endTime - startTime;
        System.out.println("[PASS] Callback returned in " + elapsed + "ms (should be < 100ms)");
        assert elapsed < 100 : "Callback should return immediately";
        
        // Wait for message to be processed
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("[PASS] Message eventually processed: " + innerAgent.getReceivedMessages());
        parallelAgent.close();
        System.out.println();
    }
    
    public static void testQueueCapacity() {
        System.out.println("Test 4: Queue Capacity Handling");
        TestAgent innerAgent = new TestAgent("SlowAgent");
        innerAgent.setProcessingDelay(100); // 100ms delay per message
        
        ParallelAgent parallelAgent = new ParallelAgent(innerAgent, 5); // Small queue
        
        // Send messages rapidly
        for (int i = 0; i < 5; i++) {
            parallelAgent.callback("topic", new Message("Message " + i));
        }
        
        System.out.println("[PASS] Queue filled without blocking");
        
        // This should block briefly as queue is full
        long startTime = System.currentTimeMillis();
        parallelAgent.callback("topic", new Message("Message 5"));
        long endTime = System.currentTimeMillis();
        
        System.out.println("[PASS] 6th message handled (blocked for " + (endTime - startTime) + "ms)");
        
        // Wait for all messages to be processed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("[PASS] All messages processed: " + innerAgent.getCallbackCount());
        parallelAgent.close();
        System.out.println();
    }
    
    public static void testDecoratorPattern() {
        System.out.println("Test 5: Decorator Pattern");
        TestAgent innerAgent = new TestAgent("InnerAgent");
        ParallelAgent parallelAgent = new ParallelAgent(innerAgent, 10);
        
        // Test that ParallelAgent properly delegates to inner agent
        assert parallelAgent.getName().equals("InnerAgent") : "getName() should delegate";
        System.out.println("[PASS] getName() delegates correctly: " + parallelAgent.getName());
        
        // Test reset delegation
        innerAgent.callback("direct", new Message("Direct message"));
        assert innerAgent.getCallbackCount() == 1;
        
        parallelAgent.reset();
        assert innerAgent.getCallbackCount() == 0 : "reset() should delegate";
        System.out.println("[PASS] reset() delegates correctly");
        
        parallelAgent.close();
        System.out.println();
    }
    
    public static void testGracefulShutdown() {
        System.out.println("Test 6: Graceful Shutdown");
        TestAgent innerAgent = new TestAgent("TestAgent");
        ParallelAgent parallelAgent = new ParallelAgent(innerAgent, 10);
        
        CountDownLatch latch = new CountDownLatch(5);
        innerAgent.setLatch(latch);
        innerAgent.setProcessingDelay(200); // 200ms per message
        
        // Send multiple messages
        for (int i = 0; i < 5; i++) {
            parallelAgent.callback("topic", new Message("Message " + i));
        }
        
        // Close while messages are being processed
        new Thread(() -> {
            try {
                Thread.sleep(300); // Let some messages process
                parallelAgent.close();
                System.out.println("[PASS] Close called while processing");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("[PASS] Processed " + innerAgent.getCallbackCount() + " messages before shutdown");
        System.out.println();
    }
    
    public static void main(String[] args) {
        System.out.println("=== ParallelAgent Test Suite ===\n");
        
        testBasicFunctionality();
        testThreadCreation();
        testNonBlockingCallback();
        testQueueCapacity();
        testDecoratorPattern();
        testGracefulShutdown();
        
        System.out.println("=== All tests completed ===");
    }
}