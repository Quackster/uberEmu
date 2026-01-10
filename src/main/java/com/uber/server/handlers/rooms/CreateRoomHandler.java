package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for creating a room (message ID 29).
 */
public class CreateRoomHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CreateRoomHandler.class);
    private final Game game;
    
    public CreateRoomHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String name = message.popFixedString();
        String description = message.popFixedString();
        String model = message.popFixedString();
        int categoryId = message.popWiredInt32();
        int maxUsers = message.popWiredInt32();
        int tradeMode = message.popWiredInt32();
        
        com.uber.server.event.packet.navigator.CreateFlatEvent event = new com.uber.server.event.packet.navigator.CreateFlatEvent(client, message, name, description, model, categoryId, maxUsers, tradeMode);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        name = event.getName();
        description = event.getDescription();
        model = event.getModel();
        categoryId = event.getCategoryId();
        maxUsers = event.getMaxUsers();
        tradeMode = event.getTradeMode();
        
        // Filter injection characters
        String roomName = StringUtil.filterInjectionChars(name, true);
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        String modelName = model;
        
        if (roomName == null || modelName == null) {
            return;
        }
        
        com.uber.server.game.rooms.RoomData newRoom = game.getRoomManager().createRoom(client, roomName, modelName);
        
        if (newRoom != null) {
            var composer = new com.uber.server.messages.outgoing.navigator.FlatCreatedComposer(
                newRoom.getId(), newRoom.getName());
            client.sendMessage(composer.compose());
        }
    }
}
