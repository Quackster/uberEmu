package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomDataMessageEvent (ID 454).
 * Sends room data for private rooms to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class GetGuestRoomResultComposer extends OutgoingMessageComposer {
    private final ServerMessage roomDataMessage;
    
    public GetGuestRoomResultComposer(ServerMessage roomDataMessage) {
        this.roomDataMessage = roomDataMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return roomDataMessage; // Already built with ID 454
    }
}
