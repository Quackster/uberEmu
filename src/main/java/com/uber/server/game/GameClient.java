package com.uber.server.game;

import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.PacketHandlerRegistry;
import com.uber.server.messages.ServerMessage;
import com.uber.server.net.TcpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a game client connection.
 * Thread-safe with proper synchronization.
 */
public class GameClient {
    private static final Logger logger = LoggerFactory.getLogger(GameClient.class);
    private static final int MAX_MESSAGE_LENGTH = 65536; // Maximum reasonable message size (64KB)
    private static final int MAX_BODY_LENGTH = MAX_MESSAGE_LENGTH - 2; // Account for 2-byte ID
    
    private final long clientId;
    private final TcpConnection connection;
    private final PacketHandlerRegistry handlerRegistry;
    private final AtomicBoolean pongOK;
    
    // Habbo (user) object
    private Habbo habbo;
    
    public GameClient(long clientId, TcpConnection connection, PacketHandlerRegistry handlerRegistry) {
        this.clientId = clientId;
        this.connection = connection;
        this.handlerRegistry = handlerRegistry;
        this.pongOK = new AtomicBoolean(true);
    }
    
    public long getClientId() {
        return clientId;
    }
    
    public TcpConnection getConnection() {
        return connection;
    }
    
    public boolean isLoggedIn() {
        return habbo != null;
    }
    
    public boolean isPongOK() {
        return pongOK.get();
    }
    
    public void setPongOK(boolean value) {
        pongOK.set(value);
    }
    
    /**
     * Starts the connection and begins receiving messages.
     * With Netty, message reception is handled automatically by the pipeline.
     */
    public void startConnection() {
        if (connection == null) {
            return;
        }
        
        pongOK.set(true);
        
        // With Netty, data reception is handled automatically by the pipeline
        // This method is kept for compatibility but doesn't need to start anything
        logger.debug("Client {} started (Netty pipeline active)", clientId);
    }
    
    /**
     * Handles incoming connection data.
     * Thread-safe message parsing and routing.
     */
    private synchronized void handleConnectionData(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        
        // Check for cross-domain policy request (data[0] != 64)
        if (data[0] != 64) {
            // Send cross-domain policy (to be implemented if needed)
            logger.debug("Received cross-domain policy request from client {}", clientId);
            return;
        }
        
        // Parse batched messages
        int pos = 0;
        while (pos < data.length) {
            try {
                // Parse message: [3 bytes: Base64 length][2 bytes: Base64 message ID][body]
                if (pos + 5 > data.length) {
                    logger.warn("Invalid message format: not enough bytes");
                    break;
                }
                
                // Decode message length (3 bytes Base64)
                byte[] lengthBytes = {data[pos++], data[pos++], data[pos++]};
                int messageLength = com.uber.server.encoding.base64.Base64Encoding.decodeInt32(lengthBytes);
                
                // Validate message length (must be at least 2 for the ID bytes, reasonable max)
                // Check for negative values and integer overflow issues
                if (messageLength < 2 || messageLength > MAX_MESSAGE_LENGTH) {
                    logger.warn("Invalid message length: {} (client: {})", messageLength, clientId);
                    break; // Skip invalid packet
                }
                
                // Decode message ID (2 bytes Base64)
                byte[] idBytes = {data[pos++], data[pos++]};
                long messageId = com.uber.server.encoding.base64.Base64Encoding.decodeUInt32(idBytes);
                
                // Extract message body
                int bodyLength = messageLength - 2;
                
                // Validate body length before allocation (prevent integer underflow and excessive allocation)
                if (bodyLength < 0 || bodyLength > MAX_BODY_LENGTH) {
                    logger.warn("Invalid body length: {} (messageLength: {}) (client: {})", 
                        bodyLength, messageLength, clientId);
                    break; // Skip invalid packet
                }
                
                if (pos + bodyLength > data.length) {
                    logger.warn("Invalid message format: body length exceeds available data");
                    break;
                }
                
                byte[] body = new byte[bodyLength];
                System.arraycopy(data, pos, body, 0, bodyLength);
                pos += bodyLength;
                
                // Create ClientMessage and route to handler
                ClientMessage message = new ClientMessage(messageId, body);
                handleMessage(message);
                
            } catch (Exception e) {
                logger.error("Error parsing message from client {}: {}", clientId, e.getMessage(), e);
                break;
            }
        }
    }
    
    /**
     * Handles a parsed client message.
     * Thread-safe handler lookup and invocation.
     * Called by Netty channel handler or internal packet parser.
     */
    public void handleMessage(ClientMessage message) {
        int messageId = (int) message.getId();
        
        logger.info("[{}] --> {}", messageId, message.getBody());
        
        if (messageId < 0 || messageId > 4004) { // HIGHEST_MESSAGE_ID
            logger.warn("Warning - out of protocol request: {}", message.getHeader());
            return;
        }
        
        PacketHandler handler = handlerRegistry.getHandler(messageId);
        if (handler == null) {
            logger.debug("No handler registered for message ID: {}", messageId);
            return;
        }
        
        try {
            handler.handle(this, message);
        } catch (Exception e) {
            logger.error("Error handling message {} from client {}: {}", messageId, clientId, e.getMessage(), e);
        }
    }
    
    /**
     * Sends a message to the client.
     * Thread-safe.
     */
    public void sendMessage(ServerMessage message) {
        if (connection != null && connection.isAlive()) {
            connection.sendMessage(message);
        }
    }
    
    /**
     * Stops the client connection.
     */
    public synchronized void stop() {
        if (connection != null) {
            connection.stop();
        }
        habbo = null;
    }
    
    public Habbo getHabbo() {
        return habbo;
    }
    
    public void setHabbo(Habbo habbo) {
        this.habbo = habbo;
    }
    
    /**
     * Sends a notification message to the client.
     * @param message Notification message
     */
    public void sendNotif(String message) {
        var composer = new com.uber.server.messages.outgoing.handshake.AuthenticationOKComposer(message);
        sendMessage(composer.compose());
    }
    
    /**
     * Sends a notification message with a boolean flag (typically for caution/alerts).
     * @param message Notification message
     * @param flag Boolean flag (e.g., caution, isAdmin)
     */
    public void sendNotif(String message, boolean flag) {
        // For now, just send the message (flag may be used for styling in client)
        sendNotif(message);
    }
    
    /**
     * Sends a notification message with a URL.
     * @param message Notification message
     * @param url URL to include
     */
    public void sendNotif(String message, String url) {
        // TODO: Replace with NotificationMessageEventComposer (ID 161) when created
        ServerMessage notif = new ServerMessage(161);
        notif.appendStringWithBreak(message);
        notif.appendStringWithBreak(url);
        sendMessage(notif);
    }
    
    /**
     * Disconnects the client.
     */
    public void disconnect() {
        stop();
    }
}
