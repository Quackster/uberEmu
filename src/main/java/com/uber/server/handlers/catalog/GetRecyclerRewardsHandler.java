package com.uber.server.handlers.catalog;

import com.uber.server.game.catalog.Catalog;
import com.uber.server.game.catalog.EcotronReward;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.items.Item;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handler for getting recycler rewards (message ID 412).
 */
public class GetRecyclerRewardsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetRecyclerRewardsHandler.class);
    private final Game game;
    
    public GetRecyclerRewardsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.catalog.GetRecyclerRewardsEvent event = new com.uber.server.event.packet.catalog.GetRecyclerRewardsEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Catalog catalog = game.getCatalog();
        if (catalog == null) {
            return;
        }
        
        ServerMessage response = new ServerMessage(506);
        response.appendInt32(5); // 5 levels
        
        // Iterate from level 5 down to 1
        for (long level = 5; level >= 1; level--) {
            response.appendUInt(level);
            
            // Required items count for each level
            if (level <= 1) {
                response.appendInt32(0);
            } else if (level == 2) {
                response.appendInt32(4);
            } else if (level == 3) {
                response.appendInt32(40);
            } else if (level == 4) {
                response.appendInt32(200);
            } else if (level >= 5) {
                response.appendInt32(2000);
            }
            
            List<EcotronReward> rewards = catalog.getEcotronRewardsForLevel(level);
            response.appendInt32(rewards.size());
            
            for (EcotronReward reward : rewards) {
                Item baseItem = reward.getBaseItem(game.getItemManager());
                if (baseItem != null) {
                    response.appendStringWithBreak(baseItem.getType().toLowerCase());
                } else {
                    response.appendStringWithBreak("s"); // Default to floor item
                }
                response.appendUInt(reward.getDisplayId());
            }
        }
        
        client.sendMessage(response);
    }
}
