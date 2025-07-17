package configs;

import graph.*;

public class IncAgent implements Agent {
    private static int instanceCounter = 0;
    private final String name;
    private final String[] subs;
    private final String[] pubs;
    
    public IncAgent(String[] subs, String[] pubs) {
        this.name = "IncAgent_" + (++instanceCounter);
        this.subs = subs;
        this.pubs = pubs;
        
        // Subscribe to the first topic in subs
        if (subs.length >= 1) {
            TopicManagerSingleton.get().getTopic(subs[0]).subscribe(this);
        }
        
        // Register as publisher to the first topic in pubs
        if (pubs.length >= 1) {
            TopicManagerSingleton.get().getTopic(pubs[0]).addPublisher(this);
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void reset() {
        // No internal state to reset
    }
    
    @Override
    public void callback(String topic, Message msg) {
        // When receiving a valid number, increment by 1 and publish
        if (subs.length >= 1 && pubs.length >= 1 && topic.equals(subs[0])) {
            if (!Double.isNaN(msg.asDouble)) {
                double result = msg.asDouble + 1.0;
                TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(result));
            }
        }
    }
    
    @Override
    public void close() {
        // Unsubscribe from topic
        if (subs.length >= 1) {
            TopicManagerSingleton.get().getTopic(subs[0]).unsubscribe(this);
        }
        
        // Remove as publisher
        if (pubs.length >= 1) {
            TopicManagerSingleton.get().getTopic(pubs[0]).removePublisher(this);
        }
    }
}