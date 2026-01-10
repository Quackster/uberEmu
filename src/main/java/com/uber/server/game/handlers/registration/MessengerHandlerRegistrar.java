package com.uber.server.game.handlers.registration;

import com.uber.server.game.Game;
import com.uber.server.messages.PacketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers messenger-related packet handlers.
 */
public class MessengerHandlerRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(MessengerHandlerRegistrar.class);
    
    private final PacketHandlerRegistry registry;
    private final Game game;
    
    public MessengerHandlerRegistrar(PacketHandlerRegistry registry, Game game) {
        this.registry = registry;
        this.game = game;
    }
    
    /**
     * Registers all messenger handlers.
     */
    public void register() {
        // InitMessenger handler (ID 12)
        registry.register(12, new com.uber.server.messages.incoming.messenger.MessengerInitMessageComposerHandler()); // MessengerInitMessageComposer (ID 12)
        
        // FriendsListUpdate handler (ID 15)
        registry.register(15, new com.uber.server.messages.incoming.messenger.FriendListUpdateMessageComposerHandler()); // FriendListUpdateMessageComposer (ID 15)
        
        // RequestBuddy handler (ID 39)
        registry.register(39, new com.uber.server.messages.incoming.messenger.RequestBuddyMessageComposerHandler(game)); // RequestBuddyMessageComposer (ID 39)
        
        // Remaining messenger handlers
        registry.register(40, new com.uber.server.messages.incoming.messenger.RemoveBuddyMessageComposerHandler(game)); // RemoveBuddyMessageComposer (ID 40)
        registry.register(41, new com.uber.server.messages.incoming.messenger.HabboSearchMessageComposerHandler(game)); // HabboSearchMessageComposer (ID 41)
        registry.register(37, new com.uber.server.messages.incoming.messenger.AcceptBuddyMessageComposerHandler(game)); // AcceptBuddyMessageComposer (ID 37)
        registry.register(38, new com.uber.server.messages.incoming.messenger.DeclineBuddyMessageComposerHandler(game)); // DeclineBuddyMessageComposer (ID 38)
        registry.register(33, new com.uber.server.messages.incoming.messenger.SendMsgMessageComposerHandler(game)); // SendMsgMessageComposer (ID 33)
        registry.register(262, new com.uber.server.messages.incoming.messenger.FollowFriendMessageComposerHandler(game)); // FollowFriendMessageComposer (ID 262)
        registry.register(34, new com.uber.server.messages.incoming.messenger.SendRoomInviteMessageComposerHandler(game)); // SendRoomInviteMessageComposer (ID 34)
        
        logger.debug("Registered messenger handlers");
    }
}
