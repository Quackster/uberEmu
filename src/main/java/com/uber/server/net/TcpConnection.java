package com.uber.server.net;

import com.uber.server.messages.ServerMessage;
import com.uber.server.encoding.base64.Base64Encoding;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a TCP connection with a client using Netty.
 */
public class TcpConnection {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnection.class);
    
    public static final io.netty.util.AttributeKey<TcpConnection> CONNECTION_ATTR = 
        io.netty.util.AttributeKey.valueOf("TcpConnection");
    
    private final long id;
    private final Instant created;
    private final Channel channel;
    private final AtomicBoolean isAlive;
    
    private SocketAddress remoteAddress;
    
    public TcpConnection(long id, Channel channel) {
        this.id = id;
        this.channel = channel;
        this.created = Instant.now();
        this.isAlive = new AtomicBoolean(true);
        
        // Store this TcpConnection in channel attributes
        if (channel != null) {
            channel.attr(CONNECTION_ATTR).set(this);
            this.remoteAddress = channel.remoteAddress();
            
            // Set connection ID in channel attributes for HabboChannelHandler
            channel.attr(io.netty.util.AttributeKey.valueOf("connectionId")).set(id);
            
            // Close future listener
            channel.closeFuture().addListener(future -> {
                if (future.isDone()) {
                    connectionDead();
                }
            });
        } else {
            this.remoteAddress = null;
        }
    }
    
    public long getId() {
        return id;
    }
    
    public Instant getCreated() {
        return created;
    }
    
    public int getAgeInSeconds() {
        long seconds = Instant.now().getEpochSecond() - created.getEpochSecond();
        return seconds < 0 ? 0 : (int) seconds;
    }
    
    public String getIPAddress() {
        if (remoteAddress == null) {
            return "";
        }
        String addr = remoteAddress.toString();
        int colonIndex = addr.indexOf(':');
        return colonIndex > 0 ? addr.substring(0, colonIndex) : addr;
    }
    
    public boolean isAlive() {
        return isAlive.get() && channel != null && channel.isActive();
    }
    
    /**
     * Starts receiving data from the connection.
     * With Netty, this is handled automatically by the pipeline.
     * @param dataRouter Callback to handle received data (not used with Netty, kept for compatibility)
     */
    public void start(java.util.function.Consumer<byte[]> dataRouter) {
        // With Netty, data reception is handled automatically by the pipeline
        // This method is kept for compatibility but doesn't need to do anything
        if (channel != null && channel.isActive()) {
            logger.debug("Connection {} started (Netty pipeline active)", id);
        }
    }
    
    /**
     * Stops the connection and closes the channel.
     */
    public synchronized void stop() {
        if (!isAlive.getAndSet(false)) {
            return;
        }
        
        if (channel != null && channel.isActive()) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Interrupted while closing channel for connection {}: {}", id, e.getMessage());
            } catch (Exception e) {
                logger.debug("Error closing channel for connection {}: {}", id, e.getMessage());
            }
        }
    }
    
    /**
     * Tests the connection by attempting to send a zero byte.
     * @return True if connection is alive and can send data
     */
    public boolean testConnection() {
        if (!isAlive()) {
            return false;
        }
        
        try {
            // With Netty, we can check if the channel is writable
            return channel.isActive() && channel.isWritable();
        } catch (Exception e) {
            logger.debug("Connection test failed for connection {}: {}", id, e.getMessage());
            return false;
        }
    }
    
    /**
     * Sends data to the client.
     * @param data The data to send
     */
    public synchronized void sendData(byte[] data) {
        if (!isAlive()) {
            return;
        }
        
        if (data == null || data.length == 0) {
            return;
        }
        
        if (channel == null || !channel.isActive()) {
            connectionDead();
            return;
        }
        
        try {
            io.netty.buffer.ByteBuf buf = channel.alloc().buffer(data.length);
            buf.writeBytes(data);
            ChannelFuture future = channel.writeAndFlush(buf);
            
            future.addListener(f -> {
                if (!f.isSuccess()) {
                    logger.warn("Failed to send data to connection {}: {}", id, f.cause().getMessage());
                    connectionDead();
                }
            });
        } catch (Exception e) {
            logger.warn("Error sending data to connection {}: {}", id, e.getMessage());
            connectionDead();
        }
    }
    
    /**
     * Sends a ServerMessage to the client.
     * @param message The message to send
     */
    public synchronized void sendMessage(ServerMessage message) {
        if (message == null) {
            return;
        }
        
        if (logger.isDebugEnabled()) {
            try {
                int messageId = Base64Encoding.decodeInt32(message.getHeader().getBytes());
                String bodyStr = message.toBodyString();
                String formattedBody = formatLogMessage(bodyStr);
                logger.debug("[{}] <-- {} / {}", id, messageId, formattedBody);
            } catch (Exception e) {
                logger.debug("Failed to log message: {}", e.getMessage());
            }
        }
        
        if (channel == null || !channel.isActive()) {
            connectionDead();
            return;
        }
        
        try {
            // Send ServerMessage directly - the encoder will handle it
            ChannelFuture future = channel.writeAndFlush(message);
            
            future.addListener(f -> {
                if (!f.isSuccess()) {
                    f.cause().printStackTrace();
                    logger.warn("Failed to send message to connection {}: {}", id, f.cause().getMessage());
                    connectionDead();
                }
            });
        } catch (Exception e) {
            logger.warn("Error sending message to connection {}: {}", id, e.getMessage());
            connectionDead();
        }
    }
    
    /**
     * Formats log message by replacing control characters.
     */
    private String formatLogMessage(String message) {
        for (int i = 0; i < 14; i++) {
            message = message.replace(String.valueOf((char) i), "[" + i + "]");
        }
        return message;
    }
    
    /**
     * Called when the connection is dead.
     */
    private void connectionDead() {
        if (isAlive.getAndSet(false)) {
            logger.debug("Connection [{}] closed", id);
            // This will be handled by GameClientManager via channelInactive
        }
    }
    
    /**
     * Gets the underlying Netty Channel.
     * @return The channel
     */
    public Channel getChannel() {
        return channel;
    }
}
