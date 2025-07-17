package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Decorator pattern implementation that adds asynchronous message processing to any Agent.
 * Wraps an existing agent and processes its messages in a separate thread.
 * 
 * Design Pattern: Decorator Pattern
 * - Extends Agent functionality without modifying the original class
 * - Maintains the same interface allowing transparent substitution
 * - Adds concurrent processing capability to any agent implementation
 * 
 * Key Features:
 * - Non-blocking message reception via bounded queue
 * - Dedicated worker thread for message processing
 * - Preserves message ordering (FIFO)
 * - Graceful shutdown with thread cleanup
 * 
 * Use Case: Prevents slow agents from blocking the topic publishing thread
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles message queuing/threading
 * - Open/Closed: Extends agent behavior without modification
 * - Liskov Substitution: Can replace any Agent transparently
 * - Interface Segregation: Implements only the Agent interface
 * - Dependency Inversion: Depends on Agent interface, not concrete classes
 * 
 * @author Advanced Programming Course
 */
public class ParallelAgent implements Agent {
    private final Agent agent;
    private final BlockingQueue<MessageWrapper> queue;
    private final Thread workerThread;
    private volatile boolean running = true;
    
    /**
     * Inner class to bundle topic and message for queue storage.
     * Immutable design ensures thread safety.
     */
    private static class MessageWrapper {
        final String topic;
        final Message message;
        
        MessageWrapper(String topic, Message message) {
            this.topic = topic;
            this.message = message;
        }
    }
    
    /**
     * Creates a parallel agent wrapping the given agent.
     * 
     * Threading model:
     * - Main thread: Receives messages and adds to queue
     * - Worker thread: Takes from queue and processes via wrapped agent
     * 
     * Capacity parameter controls backpressure:
     * - If queue fills, callback() will block (preventing memory overflow)
     * - Choose capacity based on expected message rate and processing time
     * 
     * @param agent The agent to decorate with parallel processing
     * @param capacity Maximum queue size (must be positive)
     */
    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(capacity);
        
        this.workerThread = new Thread(() -> {
            while (running) {
                try {
                    MessageWrapper wrapper = queue.take();
                    agent.callback(wrapper.topic, wrapper.message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        workerThread.start();
    }
    
    /**
     * Delegates to wrapped agent - decorator transparency.
     * The ParallelAgent appears identical to the wrapped agent.
     */
    @Override
    public String getName() {
        return agent.getName();
    }
    
    /**
     * Delegates reset to wrapped agent.
     * Note: Does not clear the message queue - in-flight messages remain.
     */
    @Override
    public void reset() {
        agent.reset();
    }
    
    /**
     * Receives messages and queues them for asynchronous processing.
     * 
     * Behavior:
     * - Non-blocking if queue has space
     * - Blocks if queue is full (backpressure)
     * - Preserves interrupt status on interruption
     * 
     * This is the key decorator method - it intercepts the callback
     * and redirects to the queue instead of immediate processing.
     * 
     * @param topic The topic that published the message
     * @param msg The message to process
     */
    @Override
    public void callback(String topic, Message msg) {
        try {
            queue.put(new MessageWrapper(topic, msg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Performs graceful shutdown of the parallel processing.
     * 
     * Shutdown sequence:
     * 1. Set running flag to stop worker loop
     * 2. Interrupt worker thread (wakes from queue.take())
     * 3. Wait for worker to finish current message
     * 4. Close the wrapped agent
     * 
     * Thread safety:
     * - volatile 'running' ensures visibility across threads
     * - join() ensures worker completes before agent cleanup
     * 
     * Note: Any messages still in queue will be lost
     */
    @Override
    public void close() {
        running = false;
        workerThread.interrupt();
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        agent.close();
    }
}