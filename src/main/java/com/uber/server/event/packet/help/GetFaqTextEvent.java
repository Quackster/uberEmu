package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests FAQ text (packet ID 418).
 */
public class GetFaqTextEvent extends PacketReceiveEvent {
    private int topicId;
    
    public GetFaqTextEvent(GameClient client, ClientMessage message, int topicId) {
        super(client, message, 418);
        this.topicId = topicId;
    }
    
    public int getTopicId() {
        return topicId;
    }
    
    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }
}
