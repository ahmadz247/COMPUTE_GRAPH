package graph;

import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe singleton implementation for global topic management.
 * Ensures a single source of truth for all topics in the system.
 * 
 * Design Pattern: Singleton Pattern with Bill Pugh Solution
 * - Uses static inner class for lazy initialization
 * - Thread-safe without synchronization overhead
 * - Prevents reflection attacks with private constructor
 * 
 * SOLID Principles:
 * - Single Responsibility: Only manages topic registry
 * - Open/Closed: New topic types can be added without modification
 * - Dependency Inversion: Components depend on this abstraction, not concrete topics
 * 
 * Thread Safety: ConcurrentHashMap ensures safe concurrent access
 * 
 * @author Advanced Programming Course
 */
public class TopicManagerSingleton {
    private TopicManagerSingleton() {}

    public static TopicManager get() {
        return TopicManager.instance;
    }

    /**
     * Inner singleton class - loaded only when first accessed.
     * Manages the lifecycle and registry of all topics.
     */
    public static class TopicManager {
        private final Map<String, Topic> topics = new ConcurrentHashMap<>();

        private TopicManager() {}

        private static final TopicManager instance = new TopicManager();

        /**
         * Gets or creates a topic by name - factory method pattern.
         * Thread-safe lazy initialization of topics.
         * 
         * @param name Topic identifier
         * @return Existing or newly created topic
         */
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
