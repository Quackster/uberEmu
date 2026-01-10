package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomInfoMessageEvent (ID 471).
 * Sends room info (public/private) to the client.
 */
public class RoomEntryInfoComposer extends OutgoingMessageComposer {
    private final boolean isPrivate;
    private final String modelName;
    private final long roomId;
    private final boolean isOwner;
    
    public RoomEntryInfoComposer(boolean isPrivate, String modelName, long roomId, boolean isOwner) {
        this.isPrivate = isPrivate;
        this.modelName = modelName;
        this.roomId = roomId;
        this.isOwner = isOwner;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(471); // _events[471] = RoomInfoMessageEvent
        if (isPrivate) {
            msg.appendBoolean(true);
            msg.appendUInt(roomId);
            msg.appendBoolean(isOwner);
        } else {
            msg.appendBoolean(false);
            msg.appendStringWithBreak(modelName);
            msg.appendBoolean(false);
        }
        return msg;
    }
}
