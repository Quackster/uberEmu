package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomRatingEvent (ID 345).
 * Sends room score/rating update to the client.
 */
public class RoomRatingComposer extends OutgoingMessageComposer {
    private final int score;
    
    public RoomRatingComposer(int score) {
        this.score = score;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(345); // _events[345] = RoomRatingEvent
        msg.appendInt32(score);
        return msg;
    }
}
