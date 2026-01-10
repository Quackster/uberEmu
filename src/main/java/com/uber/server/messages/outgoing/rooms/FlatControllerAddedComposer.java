package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomRightsGivenMessageEvent (ID 510).
 * Notifies client that room rights were given to a user.
 */
public class FlatControllerAddedComposer extends OutgoingMessageComposer {
    private final long roomId;
    private final long userId;
    private final String username;
    
    public FlatControllerAddedComposer(long roomId, long userId, String username) {
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(510); // _events[510] = RoomRightsGivenMessageEvent
        msg.appendUInt(roomId);
        msg.appendUInt(userId);
        msg.appendStringWithBreak(username);
        return msg;
    }
}
