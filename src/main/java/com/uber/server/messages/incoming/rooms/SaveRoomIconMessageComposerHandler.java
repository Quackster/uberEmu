package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for SaveRoomIconMessageComposer (ID 386).
 * Processes room icon save requests from the client.
 */
public class SaveRoomIconMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SaveRoomIconMessageComposerHandler.class);
    private final Game game;
    
    public SaveRoomIconMessageComposerHandler(Game game) {
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
        
        int junk = message.popWiredInt32(); // Always 3
        int background = message.popWiredInt32();
        int topLayer = message.popWiredInt32();
        int amountOfItems = message.popWiredInt32();
        
        Map<Integer, Integer> items = new HashMap<>();
        for (int i = 0; i < amountOfItems; i++) {
            int pos = message.popWiredInt32();
            int item = message.popWiredInt32();
            
            if (pos < 0 || pos > 10) {
                return;
            }
            
            if (item < 1 || item > 27) {
                return;
            }
            
            if (items.containsKey(pos)) {
                return;
            }
            
            items.put(pos, item);
        }
        
        if (background < 1 || background > 24) {
            return;
        }
        
        if (topLayer < 0 || topLayer > 11) {
            return;
        }
        
        // Format items string
        StringBuilder formattedItems = new StringBuilder();
        int j = 0;
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            if (j > 0) {
                formattedItems.append("|");
            }
            formattedItems.append(entry.getKey()).append(",").append(entry.getValue());
            j++;
        }
        
        // Update room icon in database
        if (game.getRoomRepository().updateRoomIcon(room.getRoomId(), background, topLayer, formattedItems.toString())) {
            // Update RoomIcon object
            com.uber.server.game.rooms.RoomIcon newIcon = new com.uber.server.game.rooms.RoomIcon(background, topLayer, items);
            room.getData().setIcon(newIcon);
            
            // Send confirmation messages
            var iconSavedComposer = new com.uber.server.messages.outgoing.rooms.RoomThumbnailUpdateResultComposer(room.getRoomId(), true);
            client.sendMessage(iconSavedComposer.compose());
            
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
