package test;

import java.util.List;
import java.util.ArrayList;

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
