package com.uber.server.net;

import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.Base64Encoding;
import com.uber.server.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Represents a TCP connection with a client.
 * Thread-safe with proper synchronization.
 */
public class TcpConnection {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnection.class);
    private static final int RCV_BUFFER_SIZE = 512;
    
    private final long id;
    private final Instant created;
    private final AsynchronousSocketChannel channel;
    private final AtomicBoolean isAlive;
    private final AtomicBoolean isReceiving;
    
    private final ByteBuffer receiveBuffer;
    private Consumer<byte[]> dataRouter;
    
    private SocketAddress remoteAddress;
    
    public TcpConnection(long id, AsynchronousSocketChannel channel) {
        this.id = id;
        this.channel = channel;
        this.created = Instant.now();
        this.isAlive = new AtomicBoolean(true);
        this.isReceiving = new AtomicBoolean(false);
        this.receiveBuffer = ByteBuffer.allocate(RCV_BUFFER_SIZE);
        
        try {
            this.remoteAddress = channel.getRemoteAddress();
        } catch (IOException e) {
            logger.error("Failed to get remote address for connection {}: {}", id, e.getMessage(), e);
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
        return isAlive.get() && channel.isOpen();
    }
    
    /**
     * Starts receiving data from the connection.
     * @param dataRouter Callback to handle received data
     */
    public void start(Consumer<byte[]> dataRouter) {
        if (!isAlive.get()) {
            return;
        }
        
        this.dataRouter = dataRouter;
        waitForData();
    }
    
    /**
     * Stops the connection and closes the channel.
     */
    public synchronized void stop() {
        if (!isAlive.getAndSet(false)) {
            return;
        }
        
        try {
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            logger.debug("Error closing channel for connection {}: {}", id, e.getMessage());
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
            ByteBuffer testBuffer = ByteBuffer.wrap(new byte[]{0});
            return channel.write(testBuffer).get() > 0;
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
        
        ByteBuffer buffer = ByteBuffer.wrap(data);
        channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (attachment.hasRemaining()) {
                    // Continue writing remaining data
                    channel.write(attachment, attachment, this);
                }
            }
            
            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                logger.warn("Failed to send data to connection {}: {}", id, exc.getMessage());
                connectionDead();
            }
        });
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
        
        byte[] messageBytes = message.getBytes();
        sendData(messageBytes);
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
     * Waits for data from the client asynchronously.
     */
    private synchronized void waitForData() {
        if (!isAlive() || isReceiving.get()) {
            return;
        }
        
        isReceiving.set(true);
        receiveBuffer.clear();
        
        channel.read(receiveBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                isReceiving.set(false);
                
                if (result < 0) {
                    // Connection closed
                    connectionDead();
                    return;
                }
                
                if (result == 0) {
                    // No data received, continue waiting
                    if (isAlive()) {
                        waitForData();
                    }
                    return;
                }
                
                // Process received data
                receiveBuffer.flip();
                byte[] receivedData = new byte[receiveBuffer.remaining()];
                receiveBuffer.get(receivedData);
                
                // Route the data to the handler
                if (dataRouter != null) {
                    try {
                        dataRouter.accept(receivedData);
                    } catch (Exception e) {
                        logger.error("Error in data router for connection {}: {}", id, e.getMessage(), e);
                    }
                }
                
                // Continue waiting for more data
                if (isAlive()) {
                    waitForData();
                }
            }
            
            @Override
            public void failed(Throwable exc, Void attachment) {
                isReceiving.set(false);
                logger.warn("Failed to receive data from connection {}: {}", id, exc.getMessage());
                connectionDead();
            }
        });
    }
    
    /**
     * Called when the connection is dead.
     */
    private void connectionDead() {
        if (isAlive.getAndSet(false)) {
            logger.debug("Connection [{}] closed", id);
            // This will be handled by GameClientManager
        }
    }
    
    /**
     * Gets the underlying AsynchronousSocketChannel.
     * @return The socket channel
     */
    public AsynchronousSocketChannel getChannel() {
        return channel;
    }
}
