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
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        String roomName = StringUtil.filterInjectionChars(message.popFixedString(), true);
        String modelName = message.popFixedString();
        String roomState = message.popFixedString(); // Unused - room open by default on creation
        
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
