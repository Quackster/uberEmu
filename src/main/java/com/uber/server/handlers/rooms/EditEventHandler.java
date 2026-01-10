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
 * Handler for editing a room event (message ID 348).
 * Ported from Messages/Requests/Rooms.cs EditEvent()
 */
public class EditEventHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(EditEventHandler.class);
    private final Game game;
    
    public EditEventHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true) || !room.hasOngoingEvent()) {
            return;
        }
        
        com.uber.server.rooms.RoomEvent event = room.getEvent();
        if (event == null) {
            return;
        }
        
        int category = message.popWiredInt32();
        String name = StringUtil.filterInjectionChars(message.popFixedString());
        String description = StringUtil.filterInjectionChars(message.popFixedString());
        int tagCount = message.popWiredInt32();
        
        // Update event
        event.setCategory(category);
        event.setName(name);
        event.setDescription(description);
        
        // Update tags
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tagCount; i++) {
            tags.add(StringUtil.filterInjectionChars(message.popFixedString()));
        }
        event.setTags(tags);
        
        // Broadcast updated event to room
        room.sendMessage(event.serialize(client));
    }
}
