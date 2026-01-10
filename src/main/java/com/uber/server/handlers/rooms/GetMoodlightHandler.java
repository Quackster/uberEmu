package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.MoodlightPreset;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting moodlight data (message ID 341).
 */
public class GetMoodlightHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetMoodlightHandler.class);
    private final Game game;
    
    public GetMoodlightHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.GetMoodlightEvent event = new com.uber.server.event.packet.room.GetMoodlightEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true) || room.getMoodlightData() == null) {
            return;
        }
        
        com.uber.server.game.items.MoodlightData moodlight = room.getMoodlightData();
        
        ServerMessage response = new ServerMessage(365);
        response.appendInt32(moodlight.getPresets().size());
        response.appendInt32(moodlight.getCurrentPreset());
        
        int i = 0;
        for (MoodlightPreset preset : moodlight.getPresets()) {
            i++;
            response.appendInt32(i);
            response.appendInt32((preset.isBackgroundOnly() ? 1 : 0) + 1);
            response.appendStringWithBreak(preset.getColorCode());
            response.appendInt32(preset.getColorIntensity());
        }
        
        client.sendMessage(response);
    }
}
