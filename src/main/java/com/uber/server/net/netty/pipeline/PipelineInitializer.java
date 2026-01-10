package com.uber.server.net.netty.pipeline;

import com.uber.server.game.GameClientManager;
import com.uber.server.net.TcpConnection;
import com.uber.server.net.TcpConnectionManager;
import com.uber.server.net.netty.codec.HabboPacketDecoder;
import com.uber.server.net.netty.codec.HabboPacketEncoder;
import com.uber.server.net.netty.handler.HabboChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes Netty pipeline for Habbo protocol.
 * Extracted from TcpConnectionListener to decouple pipeline setup.
 */
public class PipelineInitializer {
    private static final Logger logger = LoggerFactory.getLogger(PipelineInitializer.class);
    private static final AttributeKey<Long> CONNECTION_ID_KEY = AttributeKey.valueOf("connectionId");
    
    private final TcpConnectionManager connectionManager;
    private final GameClientManager gameClientManager;
    
    public PipelineInitializer(TcpConnectionManager connectionManager, GameClientManager gameClientManager) {
        this.connectionManager = connectionManager;
        this.gameClientManager = gameClientManager;
    }
    
    /**
     * Initializes the Netty pipeline for a new channel.
     * @param ch The socket channel to initialize
     */
    public void initializePipeline(SocketChannel ch) {
        // Create TcpConnection first
        TcpConnection connection = connectionManager.getFactory().createConnection(ch);
        if (connection == null) {
            ch.close();
            return;
        }
        
        // Set connection ID in channel attributes
        ch.attr(CONNECTION_ID_KEY).set(connection.getId());
        
        // Add connection to manager (this will create GameClient)
        connectionManager.handleNewConnection(connection);
        
        // Set up the pipeline with decoder, encoder, and handler
        if (gameClientManager == null) {
            logger.error("GameClientManager not set in TcpConnectionManager");
            ch.close();
            return;
        }
        
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoder", new HabboPacketDecoder());
        pipeline.addLast("encoder", new HabboPacketEncoder());
        pipeline.addLast("handler", new HabboChannelHandler(connectionManager, gameClientManager));
    }
}
