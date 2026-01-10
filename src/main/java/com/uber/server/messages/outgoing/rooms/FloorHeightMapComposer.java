package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomRelativeHeightmapMessageEvent (ID 470).
 * Sends room relative heightmap data to the client.
 * Note: This message is built by RoomModel, so we wrap the pre-built message.
 */
public class FloorHeightMapComposer extends OutgoingMessageComposer {
    private final ServerMessage relativeHeightmapMessage;
    
    public FloorHeightMapComposer(ServerMessage relativeHeightmapMessage) {
        this.relativeHeightmapMessage = relativeHeightmapMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return relativeHeightmapMessage; // Already built with ID 470
    }
}
