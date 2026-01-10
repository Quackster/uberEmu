package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomEditInfoMessageEvent (ID 465).
 * Sends room edit data to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class RoomSettingsDataComposer extends OutgoingMessageComposer {
    private final ServerMessage editInfoMessage;
    
    public RoomSettingsDataComposer(ServerMessage editInfoMessage) {
        this.editInfoMessage = editInfoMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return editInfoMessage; // Already built with ID 465
    }
}
