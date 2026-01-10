package com.uber.server.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps message IDs from the XML definition file.
 * 
 * Key mappings:
 * - _events[ID] = Incoming messages from client (handler registration IDs)
 * - _-2Pf[ID] = Outgoing messages to client (ServerMessage/composer IDs)
 */
public class MessageIdMapper {
    private static final Logger logger = LoggerFactory.getLogger(MessageIdMapper.class);
    
    private static final Map<Integer, String> incomingEventMap = new HashMap<>();
    private static final Map<Integer, String> outgoingComposerMap = new HashMap<>();
    private static boolean initialized = false;
    
    private static final Pattern INCOMING_PATTERN = Pattern.compile("_events\\[(0x[0-9A-Fa-f]+|\\d+)\\]\\s*=\\s*[^;]+\\.([^.]+Event);");
    private static final Pattern OUTGOING_PATTERN = Pattern.compile("_-2Pf\\[(0x[0-9A-Fa-f]+|\\d+)\\]\\s*=\\s*[^;]+\\.([^.]+Composer);");
    
    /**
     * Initializes the mapper by parsing the XML file.
     * @param xmlFilePath Path to the habbo_messages.xml file
     */
    public static void initialize(String xmlFilePath) {
        if (initialized) {
            return;
        }
        
        try {
            String content = Files.readString(Paths.get(xmlFilePath));
            parseIncomingEvents(content);
            parseOutgoingComposers(content);
            initialized = true;
            logger.info("Initialized MessageIdMapper: {} incoming events, {} outgoing composers", 
                       incomingEventMap.size(), outgoingComposerMap.size());
        } catch (IOException e) {
            logger.error("Failed to load message ID mappings from {}", xmlFilePath, e);
        }
    }
    
    private static void parseIncomingEvents(String content) {
        Matcher matcher = INCOMING_PATTERN.matcher(content);
        while (matcher.find()) {
            String idStr = matcher.group(1);
            String eventName = matcher.group(2);
            
            int id = parseId(idStr);
            if (id > 0) {
                incomingEventMap.put(id, eventName);
            }
        }
    }
    
    private static void parseOutgoingComposers(String content) {
        Matcher matcher = OUTGOING_PATTERN.matcher(content);
        while (matcher.find()) {
            String idStr = matcher.group(1);
            String composerName = matcher.group(2);
            
            int id = parseId(idStr);
            if (id > 0) {
                outgoingComposerMap.put(id, composerName);
            }
        }
    }
    
    private static int parseId(String idStr) {
        try {
            if (idStr.startsWith("0x")) {
                return Integer.parseInt(idStr.substring(2), 16);
            }
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse ID: {}", idStr);
            return 0;
        }
    }
    
    /**
     * Gets the incoming event name for a given ID.
     * @param id The incoming message ID from _events[ID]
     * @return Event name, or null if not found
     */
    public static String getIncomingEventName(int id) {
        return incomingEventMap.get(id);
    }
    
    /**
     * Gets the outgoing composer name for a given ID.
     * @param id The outgoing message ID from _-2Pf[ID]
     * @return Composer name, or null if not found
     */
    public static String getOutgoingComposerName(int id) {
        return outgoingComposerMap.get(id);
    }
    
    /**
     * Finds the incoming ID for a given event name.
     * @param eventName The event name (e.g., "ChatMessageEvent")
     * @return Incoming ID, or -1 if not found
     */
    public static int findIncomingId(String eventName) {
        return incomingEventMap.entrySet().stream()
            .filter(e -> e.getValue().equals(eventName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(-1);
    }
    
    /**
     * Finds the outgoing ID for a given composer name.
     * @param composerName The composer name (e.g., "ChatMessageComposer")
     * @return Outgoing ID, or -1 if not found
     */
    public static int findOutgoingId(String composerName) {
        return outgoingComposerMap.entrySet().stream()
            .filter(e -> e.getValue().equals(composerName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(-1);
    }
    
    public static Map<Integer, String> getIncomingEventMap() {
        return new HashMap<>(incomingEventMap);
    }
    
    public static Map<Integer, String> getOutgoingComposerMap() {
        return new HashMap<>(outgoingComposerMap);
    }
}