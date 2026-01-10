package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for SaveRoomDataMessageComposer (ID 401).
 * Processes room data save requests from the client.
 */
public class SaveRoomDataMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SaveRoomDataMessageComposerHandler.class);
    private final Game game;
    
    public SaveRoomDataMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int id = message.popWiredInt32();
        String name = message.popFixedString();
        String description = message.popFixedString();
        int state = message.popWiredInt32();
        String password = message.popFixedString();
        int maxUsers = message.popWiredInt32();
        int categoryId = message.popWiredInt32();
        int tagCount = message.popWiredInt32();
        
        com.uber.server.event.packet.room.SaveRoomDataEvent event = new com.uber.server.event.packet.room.SaveRoomDataEvent(client, message, id, name, description, state, password, maxUsers, categoryId, tagCount);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        id = event.getRoomId();
        name = event.getName();
        description = event.getDescription();
        state = event.getState();
        password = event.getPassword();
        maxUsers = event.getMaxUsers();
        categoryId = event.getCategoryId();
        tagCount = event.getTags();
        
        // Filter injection characters
        name = StringUtil.filterInjectionChars(name, true);
        description = StringUtil.filterInjectionChars(description, true);
        password = StringUtil.filterInjectionChars(password, true);
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tagCount; i++) {
            String tag = StringUtil.filterInjectionChars(message.popFixedString(), true).toLowerCase();
            if (tag != null && !tag.isEmpty()) {
                tags.add(tag);
            }
        }
        
        // Read boolean flags (as bytes)
        int allowPetsByte = message.popWiredInt32();
        int allowPetsEatByte = message.popWiredInt32();
        int allowWalkthroughByte = message.popWiredInt32();
        
        boolean allowPets = (allowPetsByte == 65); // 65 = 'A' in ASCII
        boolean allowPetsEating = (allowPetsEatByte == 65);
        boolean allowWalkthrough = (allowWalkthroughByte == 65);
        
        // Validation
        if (name == null || name.length() < 1) {
            return;
        }
        
        if (state < 0 || state > 2) {
            return;
        }
        
        if (maxUsers != 10 && maxUsers != 15 && maxUsers != 20 && maxUsers != 25) {
            return;
        }
        
        // Check category permissions
        com.uber.server.game.navigator.RoomCategory roomCategory = game.getNavigator().getRoomCategory(categoryId);
        if (roomCategory != null && roomCategory.getMinRank() > habbo.getRank()) {
            client.sendNotif("You are not allowed to use this category. Your room has been moved to no category instead.");
            categoryId = 0;
        }
        
        if (tagCount > 2) {
            return;
        }
        
        // Update room settings
        if (room.updateRoomSettings(name, description, state, password, maxUsers, categoryId, tags,
                                   allowPets, allowPetsEating, allowWalkthrough)) {
            // Send confirmation messages
            var savedComposer = new com.uber.server.messages.outgoing.rooms.RoomSettingsSavedComposer(room.getRoomId());
            client.sendMessage(savedComposer.compose());
            
            var updatedComposer = new com.uber.server.messages.outgoing.rooms.RoomInfoUpdatedComposer(room.getRoomId());
            client.sendMessage(updatedComposer.compose());
            
            // Send updated room data
            ServerMessage response454 = new ServerMessage(454);
            response454.appendBoolean(false);
            room.getData().serialize(response454, false);
            var roomDataComposer = new com.uber.server.messages.outgoing.rooms.GetGuestRoomResultComposer(response454);
            client.sendMessage(roomDataComposer.compose());
        }
    }
}
