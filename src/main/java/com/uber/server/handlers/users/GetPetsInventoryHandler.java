package com.uber.server.handlers.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting pets inventory (message ID 3000).
 * Ported from Messages/Requests/Users.cs GetPetsInventory()
 */
public class GetPetsInventoryHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetPetsInventoryHandler.class);
    private final Game game;
    
    public GetPetsInventoryHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        if (habbo.getInventoryComponent() == null) {
            return;
        }
        
        client.sendMessage(habbo.getInventoryComponent().serializePetInventory());
    }
}
