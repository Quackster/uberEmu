package com.uber.server.handlers.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Handler for getting user wardrobe (message ID 375).
 */
public class GetWardrobeHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetWardrobeHandler.class);
    private final Game game;
    
    public GetWardrobeHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.user.GetWardrobeEvent event = new com.uber.server.event.packet.user.GetWardrobeEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        // Send wardrobe response (ID 267)
        ServerMessage response = new ServerMessage(267);
        
        boolean hasFuse = habbo.hasFuse("fuse_use_wardrobe");
        response.appendBoolean(hasFuse);
        
        if (hasFuse && game.getWardrobeRepository() != null) {
            List<Map<String, Object>> wardrobe = game.getWardrobeRepository().loadWardrobe(habbo.getId());
            
            if (wardrobe == null || wardrobe.isEmpty()) {
                response.appendInt32(0);
            } else {
                response.appendInt32(wardrobe.size());
                
                for (Map<String, Object> item : wardrobe) {
                    response.appendUInt(((Number) item.get("slot_id")).longValue());
                    response.appendStringWithBreak((String) item.get("look"));
                    response.appendStringWithBreak((String) item.get("gender"));
                }
            }
        }
        
        client.sendMessage(response);
    }
}
