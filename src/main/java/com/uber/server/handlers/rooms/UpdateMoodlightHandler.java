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
 * Handler for updating moodlight settings (message ID 342).
 */
public class UpdateMoodlightHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateMoodlightHandler.class);
    private final Game game;
    
    public UpdateMoodlightHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int presetId = message.popWiredInt32();
        int backgroundMode = message.popWiredInt32();
        String color = message.popFixedString();
        int intensity = message.popWiredInt32();
        boolean backgroundOnly = (backgroundMode == 2);
        
        com.uber.server.event.packet.room.UpdateMoodlightEvent event = new com.uber.server.event.packet.room.UpdateMoodlightEvent(client, message, presetId, color, intensity, backgroundOnly);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        presetId = event.getPresetId();
        color = event.getColor();
        intensity = event.getIntensity();
        backgroundOnly = event.isBackgroundOnly();
        
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
        
        int preset = presetId;
        String colorCode = message.popFixedString();
        int intensity = message.popWiredInt32();
        
        boolean backgroundOnly = (backgroundMode >= 2);
        
        com.uber.server.game.items.MoodlightData moodlight = room.getMoodlightData();
        moodlight.setEnabled(true);
        moodlight.setCurrentPreset(preset);
        moodlight.updatePreset(preset, colorCode, intensity, backgroundOnly);
        
        // Update item extra data
        dimmerItem.setExtraData(moodlight.generateExtraData());
        dimmerItem.updateState(true, true);
    }
}
