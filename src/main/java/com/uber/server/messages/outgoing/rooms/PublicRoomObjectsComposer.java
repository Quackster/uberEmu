package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomStaticFurniMessageEvent (ID 30).
 * Sends static furni map to the client.
 */
public class PublicRoomObjectsComposer extends OutgoingMessageComposer {
    private final String staticFurniMap;
    
    public PublicRoomObjectsComposer(String staticFurniMap) {
        this.staticFurniMap = staticFurniMap;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(30); // _events[30] = RoomStaticFurniMessageEvent
        if (staticFurniMap != null && !staticFurniMap.isEmpty()) {
            msg.appendStringWithBreak(staticFurniMap);
        } else {
            msg.appendInt32(0);
        }
        return msg;
    }
}
