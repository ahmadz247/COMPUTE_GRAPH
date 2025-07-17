package test;

public class PlusAgent implements Agent {
    private final String name;
    private final String[] subs;
    private final String[] pubs;
    private Double x = null;
    private Double y = null;
    
    public PlusAgent(String[] subs, String[] pubs) {
        this.name = "PlusAgent_" + System.currentTimeMillis();
        this.subs = subs;
        this.pubs = pubs;
        
        // Subscribe to the first two topics in subs
        if (subs.length >= 1) {
            TopicManagerSingleton.get().getTopic(subs[0]).subscribe(this);
        }
        if (subs.length >= 2) {
            TopicManagerSingleton.get().getTopic(subs[1]).subscribe(this);
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
        x = null;
        y = null;
    }
    
    @Override
    public void callback(String topic, Message msg) {
        // Update x or y based on which topic sent the message
        if (subs.length >= 1 && topic.equals(subs[0])) {
            if (!Double.isNaN(msg.asDouble)) {
                x = msg.asDouble;
            }
        } else if (subs.length >= 2 && topic.equals(subs[1])) {
            if (!Double.isNaN(msg.asDouble)) {
                y = msg.asDouble;
            }
        }
        
        // If both x and y are valid (not null), compute x + y and publish
        if (pubs.length >= 1 && x != null && y != null) {
            double result = x + y;
            TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(result));
        }
    }
    
    @Override
    public void close() {
        // Unsubscribe from topics
        if (subs.length >= 1) {
            TopicManagerSingleton.get().getTopic(subs[0]).unsubscribe(this);
        }
        if (subs.length >= 2) {
            TopicManagerSingleton.get().getTopic(subs[1]).unsubscribe(this);
        }
        
        // Remove as publisher
        if (pubs.length >= 1) {
            TopicManagerSingleton.get().getTopic(pubs[0]).removePublisher(this);
        }
    }
}