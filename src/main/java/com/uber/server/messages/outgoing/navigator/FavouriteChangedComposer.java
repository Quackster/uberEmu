package com.uber.server.messages.outgoing.navigator;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for FavouriteChangedEvent (ID 459).
 * Notifies the client when a favorite room is added or removed.
 */
public class FavouriteChangedComposer extends OutgoingMessageComposer {
    private final long roomId;
    private final boolean added;
    
    public FavouriteChangedComposer(long roomId, boolean added) {
        this.roomId = roomId;
        this.added = added;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(459); // _events[459] = FavouriteChangedEvent
        msg.appendUInt(roomId);
        msg.appendBoolean(added);
        return msg;
    }
}
