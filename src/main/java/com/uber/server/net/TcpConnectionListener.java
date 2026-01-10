package com.uber.server.net;

import com.uber.server.game.GameClientManager;
import com.uber.server.net.netty.pipeline.PipelineInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
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
 */
public class TcpConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnectionListener.class);
    private static final int QUEUE_LENGTH = 1;
    final private static int BACK_LOG = 20;
    final private static int BUFFER_SIZE = 2048;
    
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
            int threads = Runtime.getRuntime().availableProcessors();
            this.bossGroup = (Epoll.isAvailable()) ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
            this.workerGroup = (Epoll.isAvailable()) ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
            
            // Create server bootstrap
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel((Epoll.isAvailable()) ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, BACK_LOG)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_RCVBUF, BUFFER_SIZE)
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(BUFFER_SIZE))
                    .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // Use PipelineInitializer to set up the pipeline
                            GameClientManager gameClientManager = manager.getGameClientManager();
                            if (gameClientManager == null) {
                                logger.error("GameClientManager not set in TcpConnectionManager");
                                ch.close();
                                return;
                            }
                            
                            PipelineInitializer pipelineInitializer = new PipelineInitializer(manager, gameClientManager);
                            pipelineInitializer.initializePipeline(ch);
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

                    shutdown();
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
