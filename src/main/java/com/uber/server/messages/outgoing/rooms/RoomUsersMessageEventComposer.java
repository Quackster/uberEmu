package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomUsersMessageEvent (ID 28).
 * Wraps a pre-built ServerMessage containing users data.
 */
public class RoomUsersMessageEventComposer extends OutgoingMessageComposer {
    private final ServerMessage usersMessage;
    
    public RoomUsersMessageEventComposer(ServerMessage usersMessage) {
        this.usersMessage = usersMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return usersMessage;
    }
}
