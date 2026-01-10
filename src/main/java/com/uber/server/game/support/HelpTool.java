package com.uber.server.game.support;

import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.HelpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the help system.
 */
public class HelpTool {
    private static final Logger logger = LoggerFactory.getLogger(HelpTool.class);
    
    private final ConcurrentHashMap<Long, HelpCategory> categories;
    private final ConcurrentHashMap<Long, HelpTopic> topics;
    private final List<HelpTopic> importantTopics;
    private final List<HelpTopic> knownIssues;
    private final HelpRepository helpRepository;
    
    public HelpTool(HelpRepository helpRepository) {
        this.categories = new ConcurrentHashMap<>();
        this.topics = new ConcurrentHashMap<>();
        this.importantTopics = new ArrayList<>();
        this.knownIssues = new ArrayList<>();
        this.helpRepository = helpRepository;
    }
    
    /**
     * Loads all help categories.
     */
    public void loadCategories() {
        categories.clear();
        List<Map<String, Object>> categoryData = helpRepository.loadCategories();
        
        for (Map<String, Object> row : categoryData) {
            long id = ((Number) row.get("id")).longValue();
            String caption = (String) row.get("caption");
            categories.put(id, new HelpCategory(id, caption));
        }
    }
    
    /**
     * Loads all help topics.
     */
    public void loadTopics() {
        topics.clear();
        importantTopics.clear();
        knownIssues.clear();
        
        List<Map<String, Object>> topicData = helpRepository.loadTopics();
        
        for (Map<String, Object> row : topicData) {
            long id = ((Number) row.get("id")).longValue();
            String title = (String) row.get("title");
            String body = (String) row.get("body");
            long subject = ((Number) row.get("subject")).longValue();
            int knownIssue = ((Number) row.get("known_issue")).intValue();
            
            HelpTopic topic = new HelpTopic(id, title, body, subject);
            topics.put(id, topic);
            
            if (knownIssue == 1) {
                knownIssues.add(topic);
            } else if (knownIssue == 2) {
                importantTopics.add(topic);
            }
        }
    }
    
    public HelpCategory getCategory(long categoryId) {
        return categories.get(categoryId);
    }
    
    public HelpTopic getTopic(long topicId) {
        return topics.get(topicId);
    }
    
    public int articlesInCategory(long categoryId) {
        int count = 0;
        for (HelpTopic topic : topics.values()) {
            if (topic.getCategoryId() == categoryId) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Serializes the frontpage (important topics and known issues).
     */
    public ServerMessage serializeFrontpage() {
        ServerMessage frontpage = new ServerMessage(518);
        frontpage.appendInt32(importantTopics.size());
        
        for (HelpTopic topic : importantTopics) {
            frontpage.appendUInt(topic.getTopicId());
            frontpage.appendStringWithBreak(topic.getCaption());
        }
        
        frontpage.appendInt32(knownIssues.size());
        
        for (HelpTopic topic : knownIssues) {
            frontpage.appendUInt(topic.getTopicId());
            frontpage.appendStringWithBreak(topic.getCaption());
        }
        
        return frontpage;
    }
    
    /**
     * Serializes the index (categories).
     */
    public ServerMessage serializeIndex() {
        ServerMessage index = new ServerMessage(519);
        index.appendInt32(categories.size());
        
        for (HelpCategory category : categories.values()) {
            index.appendUInt(category.getCategoryId());
            index.appendStringWithBreak(category.getCaption());
            index.appendInt32(articlesInCategory(category.getCategoryId()));
        }
        
        return index;
    }
    
    /**
     * Serializes a topic.
     */
    public ServerMessage serializeTopic(HelpTopic topic) {
        ServerMessage top = new ServerMessage(520);
        top.appendUInt(topic.getTopicId());
        top.appendStringWithBreak(topic.getBody());
        return top;
    }
    
    /**
     * Serializes search results.
     */
    public ServerMessage serializeSearchResults(String query) {
        ServerMessage search = new ServerMessage(521);
        
        List<Map<String, Object>> results = helpRepository.searchTopics(query);
        
        if (results == null || results.isEmpty()) {
            search.appendBoolean(false);
            return search;
        }
        
        search.appendInt32(results.size());
        
        for (Map<String, Object> row : results) {
            search.appendUInt(((Number) row.get("id")).longValue());
            search.appendStringWithBreak((String) row.get("title"));
        }
        
        return search;
    }
    
    /**
     * Serializes a category with its topics.
     */
    public ServerMessage serializeCategory(HelpCategory category) {
        ServerMessage categoryMessage = new ServerMessage(522);
        categoryMessage.appendUInt(category.getCategoryId());
        categoryMessage.appendStringWithBreak("");
        categoryMessage.appendInt32(articlesInCategory(category.getCategoryId()));
        
        for (HelpTopic topic : topics.values()) {
            if (topic.getCategoryId() == category.getCategoryId()) {
                categoryMessage.appendUInt(topic.getTopicId());
                categoryMessage.appendStringWithBreak(topic.getCaption());
            }
        }
        
        return categoryMessage;
    }
}
