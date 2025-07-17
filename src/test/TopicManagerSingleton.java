package test;

import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {
    private TopicManagerSingleton() {}

    public static TopicManager get() {
        return TopicManager.instance;
    }

    public static class TopicManager {
        private final Map<String, Topic> topics = new ConcurrentHashMap<>();

        private TopicManager() {}

        private static final TopicManager instance = new TopicManager();

        public Topic getTopic(String name) {
            return topics.computeIfAbsent(name, Topic::new);
        }

        public Collection<Topic> getTopics() {
            return topics.values();
        }

        public void clear() {
            topics.clear();
        }
    }
}
