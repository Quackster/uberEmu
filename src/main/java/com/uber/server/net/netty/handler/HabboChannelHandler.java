package com.uber.server.net.netty.handler;

import com.uber.server.game.GameClient;
import com.uber.server.game.GameClientManager;
import com.uber.server.messages.ClientMessage;
import com.uber.server.net.TcpConnectionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty channel handler for Habbo protocol.
 * Routes ClientMessage to GameClient for processing.
 */
public class HabboChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HabboChannelHandler.class);
    private static final AttributeKey<Long> CONNECTION_ID_KEY = AttributeKey.valueOf("connectionId");
    
    private final TcpConnectionManager connectionManager;
    private final GameClientManager gameClientManager;
    
    public HabboChannelHandler(TcpConnectionManager connectionManager, GameClientManager gameClientManager) {
        this.connectionManager = connectionManager;
        this.gameClientManager = gameClientManager;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Connection established - get connection ID from channel attributes
        Long connectionId = ctx.channel().attr(CONNECTION_ID_KEY).get();
        if (connectionId != null) {
            logger.debug("Channel active for connection {}", connectionId);
        } else {
            logger.warn("Channel active but no connection ID found in channel attributes");
        }
        
        super.channelActive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ClientMessage message) {
            
            // Get connection ID from channel attributes
            Long connectionId = ctx.channel().attr(CONNECTION_ID_KEY).get();
            if (connectionId == null) {
                logger.warn("Received message but no connection ID found");
                return;
            }
            
            // Get the GameClient for this connection
            GameClient client = gameClientManager.getClient(connectionId);
            if (client == null) {
                logger.warn("Received message for unknown client {}", connectionId);
                return;
            }
            
            // Route message to GameClient
            client.handleMessage(message);
        } else {
            logger.warn("Received unexpected message type: {}", msg.getClass().getName());
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Get connection ID from channel attributes
        Long connectionId = ctx.channel().attr(CONNECTION_ID_KEY).get();
        if (connectionId != null) {
            logger.debug("Channel inactive for connection {}", connectionId);
            // Notify game client manager to stop the client
            gameClientManager.stopClient(connectionId);
        }
        
        super.channelInactive(ctx);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Long connectionId = ctx.channel().attr(CONNECTION_ID_KEY).get();
        
        // Handle connection reset gracefully - this is normal when client disconnects
        if (cause instanceof java.net.SocketException se) {
            String message = se.getMessage();
            if (message != null && (message.contains("Connection reset") || 
                                    message.contains("Broken pipe") ||
                                    message.contains("Connection reset by peer"))) {
                logger.debug("Connection reset for connection {} (client disconnected)", connectionId);
                // Close channel gracefully
                if (ctx.channel().isActive()) {
                    ctx.close();
                }
                return;
            }
        }
        
        // Handle other exceptions
        if (cause instanceof java.io.IOException ioEx) {
            logger.debug("IO exception for connection {}: {}", connectionId, ioEx.getMessage());
        } else {
            logger.warn("Channel exception for connection {}: {}", connectionId, cause.getMessage(), cause);
        }
        
        // Close channel if still active
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }
    
    /**
     * Sets the connection ID for a channel.
     * @param ctx Channel context
     * @param connectionId Connection ID
     */
    public static void setConnectionId(ChannelHandlerContext ctx, long connectionId) {
        ctx.channel().attr(CONNECTION_ID_KEY).set(connectionId);
    }
}
