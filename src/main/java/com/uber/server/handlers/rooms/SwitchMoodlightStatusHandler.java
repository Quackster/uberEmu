package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for switching moodlight status (message ID 343).
 */
public class SwitchMoodlightStatusHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SwitchMoodlightStatusHandler.class);
    private final Game game;
    
    public SwitchMoodlightStatusHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int presetId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.SwitchMoodlightStatusEvent event = new com.uber.server.event.packet.room.SwitchMoodlightStatusEvent(client, message, presetId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        presetId = event.getPresetId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true) || room.getMoodlightData() == null) {
            return;
        }
        
        // Find the dimmer item
        RoomItem dimmerItem = null;
        for (RoomItem item : room.getItems().values()) {
            com.uber.server.game.items.Item baseItem = item.getBaseItem();
            if (baseItem != null && "dimmer".equalsIgnoreCase(baseItem.getInteractionType())) {
                dimmerItem = item;
                break;
            }
        }
        
        if (dimmerItem == null) {
            return;
        }
        
        com.uber.server.game.items.MoodlightData moodlight = room.getMoodlightData();
        
        // Toggle enabled status
        if (moodlight.isEnabled()) {
            moodlight.disable();
        } else {
            moodlight.enable();
        }
        
        // Update item extra data
        dimmerItem.setExtraData(moodlight.generateExtraData());
        dimmerItem.updateState(true, true);
    }
}
