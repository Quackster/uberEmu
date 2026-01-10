package com.uber.server.net;

import com.uber.server.game.GameClientManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP connection listener using Netty.
 * Ported from C# TcpConnectionListener.cs
 */
public class TcpConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnectionListener.class);
    private static final int QUEUE_LENGTH = 1;
    
    private final String listenerIP;
    private final int listenerPort;
    private final TcpConnectionManager manager;
    
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final AtomicBoolean isListening;
    
    public TcpConnectionListener(String localIP, int port, TcpConnectionManager manager) {
        this.listenerIP = localIP;
        this.listenerPort = port;
        this.manager = manager;
        this.isListening = new AtomicBoolean(false);
    }
    
    /**
     * Starts listening for connections using Netty ServerBootstrap.
     */
    public void start() {
        if (isListening.getAndSet(true)) {
            logger.warn("Listener is already listening");
            return;
        }
        
        try {
            // Create event loop groups
            bossGroup = new NioEventLoopGroup(1); // Single thread for accepting connections
            workerGroup = new NioEventLoopGroup(); // Default thread count for handling connections
            
            // Create server bootstrap
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, QUEUE_LENGTH)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // This is called when a new connection is accepted
                            // Create TcpConnection first
                            TcpConnection connection = manager.getFactory().createConnection(ch);
                            if (connection == null) {
                                ch.close();
                                return;
                            }
                            
                            // Set connection ID in channel attributes for HabboChannelHandler
                            ch.attr(io.netty.util.AttributeKey.valueOf("connectionId")).set(connection.getId());
                            
                            // Add connection to manager (this will create GameClient)
                            manager.handleNewConnection(connection);
                            
                            // Set up the pipeline with decoder, encoder, and handler
                            GameClientManager gameClientManager = manager.getGameClientManager();
                            if (gameClientManager == null) {
                                logger.error("GameClientManager not set in TcpConnectionManager");
                                ch.close();
                                return;
                            }
                            
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new HabboPacketDecoder());
                            pipeline.addLast("encoder", new HabboPacketEncoder());
                            pipeline.addLast("handler", new HabboChannelHandler(manager, gameClientManager));
                        }
                    });
            
            // Bind to address
            InetAddress bindAddress = null;
            try {
                bindAddress = InetAddress.getByName(listenerIP);
            } catch (Exception e) {
                logger.error("Could not parse IP address: {}, falling back to loopback", listenerIP);
                bindAddress = InetAddress.getLoopbackAddress();
            }
            
            ChannelFuture bindFuture = bootstrap.bind(new InetSocketAddress(bindAddress, listenerPort));
            serverChannel = bindFuture.sync().channel();
            
            logger.info("Game socket listening on {}:{}", bindAddress.getHostAddress(), listenerPort);
            
        } catch (Exception e) {
            isListening.set(false);
            logger.error("Failed to start TCP listener on {}:{}: {}", listenerIP, listenerPort, e.getMessage(), e);
            
            // Fallback to loopback if bind failed
            if (!listenerIP.equals("127.0.0.1") && !listenerIP.equals("localhost")) {
                logger.warn("Falling back to loopback address");
                try {
                    shutdown();
                    
                    // Try again with loopback
                    bossGroup = new NioEventLoopGroup(1);
                    workerGroup = new NioEventLoopGroup();
                    bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, QUEUE_LENGTH)
                            .option(ChannelOption.SO_REUSEADDR, true)
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    TcpConnection connection = manager.getFactory().createConnection(ch);
                                    if (connection == null) {
                                        ch.close();
                                        return;
                                    }
                                    
                                    ch.attr(io.netty.util.AttributeKey.valueOf("connectionId")).set(connection.getId());
                                    manager.handleNewConnection(connection);
                                    
                                    GameClientManager gameClientManager = manager.getGameClientManager();
                                    if (gameClientManager == null) {
                                        ch.close();
                                        return;
                                    }
                                    
                                    ChannelPipeline pipeline = ch.pipeline();
                                    pipeline.addLast("decoder", new HabboPacketDecoder());
                                    pipeline.addLast("encoder", new HabboPacketEncoder());
                                    pipeline.addLast("handler", new HabboChannelHandler(manager, gameClientManager));
                                }
                            });
                    
                    InetSocketAddress loopback = new InetSocketAddress("127.0.0.1", listenerPort);
                    ChannelFuture bindFuture = bootstrap.bind(loopback);
                    serverChannel = bindFuture.sync().channel();
                    isListening.set(true);
                    logger.info("Game socket listening on 127.0.0.1:{}", listenerPort);
                } catch (Exception e2) {
                    logger.error("Failed to bind to loopback address: {}", e2.getMessage(), e2);
                    shutdown();
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
            if (serverChannel != null && serverChannel.isActive()) {
                serverChannel.close().sync();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while closing server channel: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error stopping TCP listener: {}", e.getMessage(), e);
        }
        
        shutdown();
        logger.info("TCP listener stopped");
    }
    
    /**
     * Shuts down the event loop groups.
     */
    private void shutdown() {
        if (workerGroup != null && !workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null && !bossGroup.isShutdown()) {
            bossGroup.shutdownGracefully();
        }
    }
    
    /**
     * Destroys the listener and releases resources.
     */
    public void destroy() {
        stop();
        serverChannel = null;
        bootstrap = null;
    }
    
    /**
     * Checks if the listener is currently listening.
     * @return True if listening
     */
    public boolean isListening() {
        return isListening.get();
    }
}
