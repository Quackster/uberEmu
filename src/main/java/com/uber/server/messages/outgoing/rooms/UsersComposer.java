package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomUsersMessageEvent (ID 28).
 * Sends users in the room to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class UsersComposer extends OutgoingMessageComposer {
    private final ServerMessage usersMessage;
    
    public UsersComposer(ServerMessage usersMessage) {
        this.usersMessage = usersMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return usersMessage; // Already built with ID 28
    }
}
