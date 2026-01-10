package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for triggering an item (message IDs 392, 393, 232, 314, 247, 76).
 * Ported from Messages/Requests/Rooms.cs TriggerItem() and TriggerItemDiceSpecial()
 * 
 * Note: Full item interaction logic will be implemented when FurniInteractor is ported (Phase 11).
 * For now, this handler provides basic structure.
 */
public class TriggerItemHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(TriggerItemHandler.class);
    private final Game game;
    private final boolean isDiceSpecial; // For TriggerItemDiceSpecial (uses -1 as request)
    
    public TriggerItemHandler(Game game) {
        this(game, false);
    }
    
    public TriggerItemHandler(Game game, boolean isDiceSpecial) {
        this.game = game;
        this.isDiceSpecial = isDiceSpecial;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        long itemId = message.popWiredUInt();
        RoomItem item = room.getItem(itemId);
        
        if (item == null) {
            return;
        }
        
        boolean hasRights = room.checkRights(client);
        int request = isDiceSpecial ? -1 : message.popWiredInt32();
        
        // TODO: Call item interactor OnTrigger when FurniInteractor is ported (Phase 11)
        // item.getInteractor().onTrigger(client, item, request, hasRights);
        
        logger.debug("Item {} triggered by user {} (request: {}, hasRights: {})", 
                    itemId, habbo.getId(), request, hasRights);
    }
}
