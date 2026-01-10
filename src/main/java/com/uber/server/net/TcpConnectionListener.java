package com.uber.server.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP connection listener using AsynchronousServerSocketChannel.
 * Matches C# async callback model (BeginAcceptSocket / EndAcceptSocket).
 */
public class TcpConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnectionListener.class);
    private static final int QUEUE_LENGTH = 1;
    
    private final String listenerIP;
    private final int listenerPort;
    private final TcpConnectionManager manager;
    
    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousChannelGroup channelGroup;
    private final AtomicBoolean isListening;
    
    public TcpConnectionListener(String localIP, int port, TcpConnectionManager manager) {
        this.listenerIP = localIP;
        this.listenerPort = port;
        this.manager = manager;
        this.isListening = new AtomicBoolean(false);
    }
    
    /**
     * Starts listening for connections.
     */
    public void start() {
        if (isListening.getAndSet(true)) {
            logger.warn("Listener is already listening");
            return;
        }
        
        try {
            // Create channel group with thread pool
            channelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool());
            
            // Create server socket channel
            serverChannel = AsynchronousServerSocketChannel.open(channelGroup);
            
            // Bind to address
            InetSocketAddress bindAddress = new InetSocketAddress(listenerIP, listenerPort);
            serverChannel.bind(bindAddress, QUEUE_LENGTH);
            
            logger.info("Game socket listening on {}:{}", listenerIP, listenerPort);
            
            // Start accepting connections
            waitForNextConnection();
            
        } catch (IOException e) {
            isListening.set(false);
            logger.error("Failed to start TCP listener on {}:{}: {}", listenerIP, listenerPort, e.getMessage(), e);
            
            // Fallback to loopback if bind failed
            if (!listenerIP.equals("127.0.0.1") && !listenerIP.equals("localhost")) {
                logger.warn("Falling back to loopback address");
                try {
                    InetSocketAddress loopback = new InetSocketAddress("127.0.0.1", listenerPort);
                    serverChannel = AsynchronousServerSocketChannel.open();
                    serverChannel.bind(loopback, QUEUE_LENGTH);
                    isListening.set(true);
                    logger.info("Game socket listening on 127.0.0.1:{}", listenerPort);
                    waitForNextConnection();
                } catch (IOException e2) {
                    logger.error("Failed to bind to loopback address: {}", e2.getMessage(), e2);
                }
            }
        }
    }
    
    /**
     * Stops listening for connections.
     */
    public void stop() {
        if (!isListening.getAndSet(false)) {
            return;
        }
        
        try {
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
            
            if (channelGroup != null && !channelGroup.isShutdown()) {
                channelGroup.shutdown();
            }
            
            logger.info("TCP listener stopped");
        } catch (IOException e) {
            logger.error("Error stopping TCP listener: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Destroys the listener and releases resources.
     */
    public void destroy() {
        stop();
        serverChannel = null;
        channelGroup = null;
    }
    
    /**
     * Waits for the next connection asynchronously.
     * Matches C# BeginAcceptSocket pattern.
     */
    private void waitForNextConnection() {
        if (!isListening.get()) {
            return;
        }
        
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, Void attachment) {
                try {
                    // Create connection using factory from manager
                    TcpConnection connection = manager.getFactory().createConnection(channel);
                    
                    if (connection != null) {
                        manager.handleNewConnection(connection);
                    }
                } catch (Exception e) {
                    logger.warn("Could not handle new connection request: {}", e.getMessage());
                } finally {
                    // Continue accepting more connections
                    if (isListening.get()) {
                        waitForNextConnection();
                    }
                }
            }
            
            @Override
            public void failed(Throwable exc, Void attachment) {
                if (isListening.get()) {
                    logger.warn("Failed to accept connection: {}", exc.getMessage());
                    // Continue accepting more connections
                    waitForNextConnection();
                }
            }
        });
    }
    
    /**
     * Checks if the listener is currently listening.
     * @return True if listening
     */
    public boolean isListening() {
        return isListening.get();
    }
}
