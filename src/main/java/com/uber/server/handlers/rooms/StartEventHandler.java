package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for starting a room event (message ID 346).
 */
public class StartEventHandler implements PacketHandler {
    private final Game game;
    
    public StartEventHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int eventId = message.popWiredInt32();
        int category = message.popWiredInt32();
        String name = message.popFixedString();
        String description = message.popFixedString();
        int tagCount = message.popWiredInt32();
        
        // Parse tags
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tagCount; i++) {
            tags.add(message.popFixedString());
        }
        
        // Create and fire event with all fields
        com.uber.server.event.packet.room.StartEventEvent event = new com.uber.server.event.packet.room.StartEventEvent(
            client, message, eventId, category, name, description, tags);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        eventId = event.getEventId();
        category = event.getCategory();
        name = event.getName();
        description = event.getDescription();
        tags = event.getTags();
        
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
        
        // Filter injection characters
        name = StringUtil.filterInjectionChars(name);
        description = StringUtil.filterInjectionChars(description);
        List<String> filteredTags = new ArrayList<>();
        for (String tag : tags) {
            filteredTags.add(StringUtil.filterInjectionChars(tag));
        }
        
        // Create room event
        com.uber.server.game.rooms.RoomEvent roomEvent = new com.uber.server.game.rooms.RoomEvent(
            room.getRoomId(), name, description, category);
        roomEvent.setTags(filteredTags);
        
        // Set event on room
        room.setEvent(roomEvent);
        
        // Broadcast event to room
        room.sendMessage(roomEvent.serialize(client));
    }
}
