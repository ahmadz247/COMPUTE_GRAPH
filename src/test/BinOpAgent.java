package test;

import java.util.function.BinaryOperator;

public class BinOpAgent implements Agent {
    private final String name;
    private final String inputTopic1;
    private final String inputTopic2;
    private final String outputTopic;
    private final BinaryOperator<Double> operation;
    private Message lastMessage1;
    private Message lastMessage2;
    
    public BinOpAgent(String name, String inputTopic1, String inputTopic2, 
                      String outputTopic, BinaryOperator<Double> operation) {
        this.name = name;
        this.inputTopic1 = inputTopic1;
        this.inputTopic2 = inputTopic2;
        this.outputTopic = outputTopic;
        this.operation = operation;
        
        // Subscribe to input topics
        TopicManagerSingleton.get().getTopic(inputTopic1).subscribe(this);
        TopicManagerSingleton.get().getTopic(inputTopic2).subscribe(this);
        
        // Register as publisher to output topic
        TopicManagerSingleton.get().getTopic(outputTopic).addPublisher(this);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void reset() {
        lastMessage1 = new Message(0.0);
        lastMessage2 = new Message(0.0);
    }
    
    @Override
    public void callback(String topic, Message msg) {
        // Update the appropriate message based on which topic sent it
        if (topic.equals(inputTopic1)) {
            lastMessage1 = msg;
        } else if (topic.equals(inputTopic2)) {
            lastMessage2 = msg;
        }
        
        // If both inputs have valid Double messages, compute and publish result
        if (lastMessage1 != null && lastMessage2 != null &&
            !Double.isNaN(lastMessage1.asDouble) && !Double.isNaN(lastMessage2.asDouble)) {
            
            double result = operation.apply(lastMessage1.asDouble, lastMessage2.asDouble);
            TopicManagerSingleton.get().getTopic(outputTopic).publish(new Message(result));
        }
    }
    
    @Override
    public void close() {
        // Unsubscribe from input topics
        TopicManagerSingleton.get().getTopic(inputTopic1).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(inputTopic2).unsubscribe(this);
        
        // Remove as publisher from output topic
        TopicManagerSingleton.get().getTopic(outputTopic).removePublisher(this);
    }
}