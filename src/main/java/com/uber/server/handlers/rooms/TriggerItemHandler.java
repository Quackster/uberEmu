package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for triggering an item (message IDs 392, 393, 232, 314, 247, 76).
 * 
 * Note: Full item interaction logic with FurniInteractor is being implemented incrementally.
 * This handler provides the basic structure for item interactions.
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
        message.resetPointer();
        
        long itemId = message.popWiredUInt();
        int parameter = isDiceSpecial ? -1 : message.popWiredInt32();
        
        int packetId = (int) message.getId();
        com.uber.server.event.packet.room.TriggerItemEvent event = new com.uber.server.event.packet.room.TriggerItemEvent(client, message, packetId, itemId, parameter);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        itemId = event.getItemId();
        parameter = event.getParameter();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        RoomItem item = room.getItem(itemId);
        
        if (item == null) {
            return;
        }
        
        boolean hasRights = room.checkRights(client);
        int request = parameter;
        
        // Call item interactor OnTrigger
        item.getInteractor().onTrigger(client, item, request, hasRights);
        
        logger.debug("Item {} triggered by user {} (request: {}, hasRights: {})", 
                    itemId, habbo.getId(), request, hasRights);
    }
}
