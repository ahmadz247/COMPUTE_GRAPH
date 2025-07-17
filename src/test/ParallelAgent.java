package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent {
    private final Agent agent;
    private final BlockingQueue<MessageWrapper> queue;
    private final Thread workerThread;
    private volatile boolean running = true;
    
    private static class MessageWrapper {
        final String topic;
        final Message message;
        
        MessageWrapper(String topic, Message message) {
            this.topic = topic;
            this.message = message;
        }
    }
    
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
    
    @Override
    public String getName() {
        return agent.getName();
    }
    
    @Override
    public void reset() {
        agent.reset();
    }
    
    @Override
    public void callback(String topic, Message msg) {
        try {
            queue.put(new MessageWrapper(topic, msg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
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