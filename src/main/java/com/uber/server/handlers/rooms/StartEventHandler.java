package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for starting a room event (message ID 346).
 * Ported from Messages/Requests/Rooms.cs StartEvent()
 */
public class StartEventHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(StartEventHandler.class);
    private final Game game;
    
    public StartEventHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        // Check if event already exists or room state is not open
        if (room.hasOngoingEvent() || room.getData().getState() != 0) {
            return;
        }
        
        int category = message.popWiredInt32();
        String name = StringUtil.filterInjectionChars(message.popFixedString());
        String description = StringUtil.filterInjectionChars(message.popFixedString());
        int tagCount = message.popWiredInt32();
        
        // Create event
        com.uber.server.game.rooms.RoomEvent event = new com.uber.server.game.rooms.RoomEvent(
            room.getRoomId(), name, description, category);
        
        // Add tags
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tagCount; i++) {
            tags.add(StringUtil.filterInjectionChars(message.popFixedString()));
        }
        event.setTags(tags);
        
        // Set event on room
        room.setEvent(event);
        
        // Broadcast event to room
        room.sendMessage(event.serialize(client));
    }
}
