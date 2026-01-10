package com.uber.server.messages.outgoing.navigator;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

import java.util.List;

/**
 * Composer for FavouritesEvent (ID 458).
 * Sends favorite rooms list to the client.
 */
public class FavouritesComposer extends OutgoingMessageComposer {
    private final int maxFavorites;
    private final List<Long> favoriteRoomIds;
    
    public FavouritesComposer(int maxFavorites, List<Long> favoriteRoomIds) {
        this.maxFavorites = maxFavorites;
        this.favoriteRoomIds = favoriteRoomIds != null ? favoriteRoomIds : List.of();
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(458); // _events[458] = FavouritesEvent
        msg.appendInt32(maxFavorites);
        msg.appendInt32(favoriteRoomIds.size());
        for (Long roomId : favoriteRoomIds) {
            msg.appendUInt(roomId);
        }
        return msg;
    }
}
