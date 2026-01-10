package com.uber.server.messages.outgoing.messenger;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

import java.util.List;

/**
 * Composer for FriendListUpdateEvent (ID 13).
 * Sends friend list update to the client.
 */
public class FriendListUpdateComposer extends OutgoingMessageComposer {
    private final ServerMessage message;
    
    /**
     * Creates a composer with a pre-built message.
     * This is used when the message body is built by HabboMessenger.
     */
    public FriendListUpdateComposer(ServerMessage message) {
        this.message = message;
    }
    
    /**
     * Creates a simple friend list update message.
     */
    public FriendListUpdateComposer(int updateType, int updateCount, long friendId) {
        this.message = new ServerMessage(13); // _events[13] = FriendListUpdateEvent
        message.appendInt32(0);
        message.appendInt32(updateCount);
        message.appendInt32(updateType == 0 ? -1 : 0);
        if (updateType == 0) {
            message.appendUInt(friendId);
        }
    }
    
    @Override
    public ServerMessage compose() {
        return message;
    }
}
