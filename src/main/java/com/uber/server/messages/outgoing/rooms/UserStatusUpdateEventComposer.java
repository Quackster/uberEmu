package com.uber.server.messages.outgoing.rooms;

import com.uber.server.game.rooms.RoomUser;
import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

import java.util.List;

/**
 * Composer for UserStatusUpdateEvent (ID 34).
 * Sent to update user statuses in a room.
 */
public class UserStatusUpdateEventComposer extends OutgoingMessageComposer {
    private final List<RoomUser> users;
    
    public UserStatusUpdateEventComposer(List<RoomUser> users) {
        this.users = users;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(34);
        msg.appendInt32(users.size());
        
        for (RoomUser user : users) {
            user.serializeStatus(msg);
        }
        
        return msg;
    }
}
