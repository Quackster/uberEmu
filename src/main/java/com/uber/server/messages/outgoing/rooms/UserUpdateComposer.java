package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomUserStatusUpdateMessageEvent (ID 34).
 * Sends room user status updates to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class UserUpdateComposer extends OutgoingMessageComposer {
    private final ServerMessage statusUpdateMessage;
    
    public UserUpdateComposer(ServerMessage statusUpdateMessage) {
        this.statusUpdateMessage = statusUpdateMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return statusUpdateMessage; // Already built with ID 34
    }
}
