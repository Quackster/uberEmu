package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client kicks a bot (packet ID 441).
 */
public class KickBotEvent extends PacketReceiveEvent {
    private int botId;
    
    public KickBotEvent(GameClient client, ClientMessage message, int botId) {
        super(client, message, 441);
        this.botId = botId;
    }
    
    public int getBotId() {
        return botId;
    }
    
    public void setBotId(int botId) {
        this.botId = botId;
    }
}
