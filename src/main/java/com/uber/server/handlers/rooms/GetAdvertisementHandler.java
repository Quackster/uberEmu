package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting room advertisement (message ID 182).
 * Ported from Messages/Requests/Rooms.cs GetAdvertisement()
 * 
 * Note: AdvertisementManager not yet ported - placeholder implementation
 */
public class GetAdvertisementHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetAdvertisementHandler.class);
    private final Game game;
    
    public GetAdvertisementHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        // TODO: Implement when AdvertisementManager is ported (Phase 10)
        // For now, send empty response
        ServerMessage response = new ServerMessage(258);
        response.appendStringWithBreak("");
        response.appendStringWithBreak("");
        client.sendMessage(response);
    }
}
