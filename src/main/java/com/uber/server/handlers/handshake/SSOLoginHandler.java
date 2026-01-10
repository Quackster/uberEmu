package com.uber.server.handlers.handshake;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handler for SSO login (message ID 415).
 */
public class SSOLoginHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SSOLoginHandler.class);
    private final Game game;
    
    public SSOLoginHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Read auth ticket
        String authTicket = message.popFixedString();
        
        com.uber.server.event.packet.handshake.SSOTicketEvent event = new com.uber.server.event.packet.handshake.SSOTicketEvent(client, message, authTicket);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        authTicket = event.getSsoTicket();
        
        // Check if already logged in
        if (client.getHabbo() != null) {
            ServerMessage notif = new ServerMessage(3);
            notif.appendStringWithBreak("You are already logged in!");
            client.sendMessage(notif);
            return;
        }
        
        if (authTicket == null || authTicket.length() < 10) {
            logger.warn("Invalid auth ticket from client {}", client.getClientId());
            sendLoginError(client, "Invalid authorization/SSO ticket");
            client.stop();
            return;
        }
        
        try {
            // Authenticate user
            Map<String, Object> userData = game.getUserRepository().authenticateUser(authTicket);
            
            if (userData == null) {
                sendLoginError(client, "Invalid authorization/SSO ticket");
                client.stop();
                return;
            }
            
            // Check newbie status
            int newbieStatus = ((Number) userData.get("newbie_status")).intValue();
            if (newbieStatus == 0) {
                sendLoginError(client, "Not permitted to log in; you are still a noob.");
                client.stop();
                return;
            }
            
            // Create Habbo object
            Habbo habbo = new Habbo(userData, game);
            habbo.loadData();
            
            // Check role permissions (fuse_login)
            if (game.getRoleManager() != null && !habbo.hasFuse("fuse_login")) {
                sendLoginError(client, "You do not have permission to log in.");
                client.stop();
                return;
            }
            
            // Set habbo on client
            client.setHabbo(habbo);
            
            // Check for ban conflicts
            try {
                if (game.getBanManager() != null) {
                    game.getBanManager().checkForBanConflicts(client);
                }
            } catch (com.uber.server.game.support.ModerationBanException e) {
                client.sendNotif(e.getMessage());
                client.disconnect();
                return;
            }
            
            // Update user online status
            String ipAddress = client.getConnection().getIPAddress();
            game.getUserRepository().updateOnlineStatus(habbo.getId(), 1, "", ipAddress);
            game.getUserInfoRepository().updateLoginTimestamp(habbo.getId(), System.currentTimeMillis() / 1000);
            
            // Send rights
            List<String> rights = game.getRoleManager() != null ? 
                game.getRoleManager().getRightsForHabbo(habbo) : new ArrayList<>();
            var rightsComposer = new com.uber.server.messages.outgoing.handshake.UserRightsComposer(rights);
            client.sendMessage(rightsComposer.compose());
            
            // Send moderation tool (if user has fuse_mod)
            if (habbo.hasFuse("fuse_mod") && game.getModerationTool() != null) {
                client.sendMessage(game.getModerationTool().serializeTool());
                game.getModerationTool().sendOpenTickets(client);
            }
            
            // Send avatar effects inventory
            if (habbo.getAvatarEffectsInventoryComponent() != null) {
                client.sendMessage(habbo.getAvatarEffectsInventoryComponent().serialize());
            }
            
            // Send other login responses
            ServerMessage response290 = new ServerMessage(290);
            response290.appendBoolean(true);
            response290.appendBoolean(false);
            client.sendMessage(response290);
            
            ServerMessage response3 = new ServerMessage(3);
            client.sendMessage(response3);
            
            ServerMessage response517 = new ServerMessage(517);
            response517.appendBoolean(true);
            client.sendMessage(response517);
            
            // Send home room (packet 455)
            ServerMessage response455 = new ServerMessage(455);
            response455.appendUInt(habbo.getHomeRoom());
            client.sendMessage(response455);
            
            // Send favorite rooms (packet 458)
            ServerMessage response458 = new ServerMessage(458);
            response458.appendInt32(30);
            List<Long> favoriteRooms = habbo.getFavoriteRooms();
            response458.appendInt32(favoriteRooms.size());
            for (Long roomId : favoriteRooms) {
                response458.appendUInt(roomId);
            }
            client.sendMessage(response458);
            
            // Send welcome notification (packet 161)
            client.sendNotif("Thank you for helping us test the new Uber. Please submit feedback to the UserVoice forum:",
                             "http://uber.uservoice.com/forums/45577-general");
            
            // Send activity points balance (packet 438)
            habbo.updateActivityPointsBalance(false);
            
            logger.info("User {} logged in successfully", habbo.getUsername());
            
        } catch (Exception e) {
            logger.error("Error during login for client {}: {}", client.getClientId(), e.getMessage(), e);
            sendLoginError(client, "Login error: " + e.getMessage());
            client.stop();
        }
    }
    
    private void sendLoginError(GameClient client, String message) {
        ServerMessage error = new ServerMessage(3);
        error.appendStringWithBreak(message);
        client.sendMessage(error);
    }
}
