package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client switches moodlight status (packet ID 343).
 */
public class SwitchMoodlightStatusEvent extends PacketReceiveEvent {
    private int presetId;
    
    public SwitchMoodlightStatusEvent(GameClient client, ClientMessage message, int presetId) {
        super(client, message, 343);
        this.presetId = presetId;
    }
    
    public int getPresetId() {
        return presetId;
    }
    
    public void setPresetId(int presetId) {
        this.presetId = presetId;
    }
}
