package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomHeightmapMessageEvent (ID 31).
 * Sends room heightmap data to the client.
 * Note: This message is built by RoomModel, so we wrap the pre-built message.
 */
public class HeightMapComposer extends OutgoingMessageComposer {
    private final ServerMessage heightmapMessage;
    
    public HeightMapComposer(ServerMessage heightmapMessage) {
        this.heightmapMessage = heightmapMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return heightmapMessage; // Already built with ID 31
    }
}
