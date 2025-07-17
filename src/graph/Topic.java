package graph;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a communication channel in the publish-subscribe pattern.
 * Topics act as message brokers between publishers and subscribers.
 * 
 * Design Pattern: Observer/Publish-Subscribe Pattern
 * - Decouples message producers from consumers
 * - Supports multiple publishers and subscribers
 * - Maintains last message for late subscribers
 * 
 * SOLID Principles:
 * - Single Responsibility: Only manages message distribution
 * - Open/Closed: New agent types can subscribe without modification
 * - Interface Segregation: Simple, focused API
 * - Dependency Inversion: Depends on Agent interface, not implementations
 * 
 * Thread Safety: Not thread-safe - use with ParallelAgent for concurrency
 * 
 * @author Advanced Programming Course
 */
public class Topic {
    public final String name;
    public final List<Agent> subs = new ArrayList<>();
    public final List<Agent> pubs = new ArrayList<>();
    private Message lastMessage = null;

    Topic(String name) { // package-private
        this.name = name;
    }

    public void subscribe(Agent agent) {
        if (!subs.contains(agent)) subs.add(agent);
    }

    public void unsubscribe(Agent agent) {
        subs.remove(agent);
    }

    /**
     * Publishes a message to all subscribed agents.
     * 
     * Behavior:
     * - Stores message for future subscribers
     * - Notifies all current subscribers synchronously
     * - Order of notification matches subscription order
     * 
     * SOLID: Dependency Inversion - Calls abstract Agent.callback()
     * 
     * @param msg The message to publish
     */
    public void publish(Message msg) {
        this.lastMessage = msg;
        for (Agent agent : subs) {
            agent.callback(name, msg);
        }
    }
    
    public Message getLastMessage() {
        return lastMessage;
    }

    public void addPublisher(Agent agent) {
        if (!pubs.contains(agent)) pubs.add(agent);
    }

    public void removePublisher(Agent agent) {
        pubs.remove(agent);
    }
}
