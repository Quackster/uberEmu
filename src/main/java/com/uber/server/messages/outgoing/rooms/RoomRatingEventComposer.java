package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomRatingEvent (ID 345).
 * Sent to display room rating score.
 */
public class RoomRatingEventComposer extends OutgoingMessageComposer {
    private final int score;
    
    public RoomRatingEventComposer(int score) {
        this.score = score;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(345);
        msg.appendInt32(score);
        return msg;
    }
}
