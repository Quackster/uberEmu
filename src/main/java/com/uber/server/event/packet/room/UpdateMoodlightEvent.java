package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client updates moodlight (packet ID 342).
 */
public class UpdateMoodlightEvent extends PacketReceiveEvent {
    private int presetId;
    private String color;
    private int intensity;
    private boolean backgroundOnly;
    
    public UpdateMoodlightEvent(GameClient client, ClientMessage message, int presetId, 
                                String color, int intensity, boolean backgroundOnly) {
        super(client, message, 342);
        this.presetId = presetId;
        this.color = color;
        this.intensity = intensity;
        this.backgroundOnly = backgroundOnly;
    }
    
    public int getPresetId() {
        return presetId;
    }
    
    public void setPresetId(int presetId) {
        this.presetId = presetId;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public int getIntensity() {
        return intensity;
    }
    
    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }
    
    public boolean isBackgroundOnly() {
        return backgroundOnly;
    }
    
    public void setBackgroundOnly(boolean backgroundOnly) {
        this.backgroundOnly = backgroundOnly;
    }
}
