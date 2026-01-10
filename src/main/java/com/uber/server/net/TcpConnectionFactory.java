package com.uber.server.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Factory for creating TCP connections with unique IDs.
 */
public class TcpConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnectionFactory.class);
    private final AtomicLong connectionCounter;
    
    public TcpConnectionFactory() {
        this.connectionCounter = new AtomicLong(0);
    }
    
    /**
     * Creates a new TCP connection.
     * @param channel The socket channel
     * @return A new TcpConnection, or null if channel is null
     */
    public TcpConnection createConnection(AsynchronousSocketChannel channel) {
        if (channel == null) {
            return null;
        }
        
        long connectionId = connectionCounter.getAndIncrement();
        TcpConnection connection = new TcpConnection(connectionId, channel);
        
        logger.info("Accepted new connection. [{} / {}]", connectionId, connection.getIPAddress());
        
        return connection;
    }
    
    /**
     * Gets the total number of connections created.
     * @return Total connection count
     */
    public long getCount() {
        return connectionCounter.get();
    }
}
