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
 * Handler for editing a room event (message ID 348).
 */
public class EditEventHandler implements PacketHandler {
    private final Game game;
    
    public EditEventHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int eventId = message.popWiredInt32();
        String name = message.popFixedString();
        String description = message.popFixedString();
        int category = message.popWiredInt32();
        int tagCount = message.popWiredInt32();
        
        // Parse tags
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tagCount; i++) {
            tags.add(message.popFixedString());
        }
        
        // Create and fire event with all fields
        com.uber.server.event.packet.room.EditEventEvent event = new com.uber.server.event.packet.room.EditEventEvent(
            client, message, eventId, name, description, category, tags);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        eventId = event.getEventId();
        name = event.getName();
        description = event.getDescription();
        category = event.getCategory();
        tags = event.getTags();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true) || !room.hasOngoingEvent()) {
            return;
        }
        
        com.uber.server.game.rooms.RoomEvent roomEvent = room.getEvent();
        if (roomEvent == null) {
            return;
        }
        
        // Filter injection characters
        name = StringUtil.filterInjectionChars(name);
        description = StringUtil.filterInjectionChars(description);
        List<String> filteredTags = new ArrayList<>();
        for (String tag : tags) {
            filteredTags.add(StringUtil.filterInjectionChars(tag));
        }
        
        // Update event
        roomEvent.setCategory(category);
        roomEvent.setName(name);
        roomEvent.setDescription(description);
        roomEvent.setTags(filteredTags);
        
        // Broadcast updated event to room
        room.sendMessage(roomEvent.serialize(client));
    }
}
