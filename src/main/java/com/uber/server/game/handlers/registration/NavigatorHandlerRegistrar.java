package com.uber.server.game.handlers.registration;

import com.uber.server.game.Game;
import com.uber.server.messages.PacketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers navigator-related packet handlers.
 */
public class NavigatorHandlerRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(NavigatorHandlerRegistrar.class);
    
    private final PacketHandlerRegistry registry;
    private final Game game;
    
    public NavigatorHandlerRegistrar(PacketHandlerRegistry registry, Game game) {
        this.registry = registry;
        this.game = game;
    }
    
    /**
     * Registers all navigator handlers.
     */
    public void register() {
        // GetRoomCategories handler (ID 151)
        registry.register(151, new com.uber.server.messages.incoming.navigator.GetUserFlatCatsMessageComposerHandler(game)); // GetUserFlatCatsMessageComposer (ID 151)
        
        // GetPubs handler (ID 380)
        registry.register(380, new com.uber.server.messages.incoming.navigator.GetOfficialRoomsMessageComposerHandler(game)); // GetOfficialRoomsMessageComposer (ID 380)
        
        // Favorites and room navigation handlers
        registry.register(19, new com.uber.server.messages.incoming.navigator.AddFavouriteRoomMessageComposerHandler(game)); // AddFavouriteRoomMessageComposer (ID 19)
        registry.register(20, new com.uber.server.messages.incoming.navigator.DeleteFavouriteRoomMessageComposerHandler(game)); // DeleteFavouriteRoomMessageComposer (ID 20)
        registry.register(53, new com.uber.server.messages.incoming.rooms.QuitMessageComposerHandler(game)); // QuitMessageComposer (ID 53)
        registry.register(233, new com.uber.server.messages.incoming.navigator.EnterInquiredRoomMessageComposerHandler(game)); // EnterInquiredRoomMessageComposer (ID 233) - name inferred, verify against XML
        registry.register(385, new com.uber.server.messages.incoming.navigator.GetGuestRoomMessageComposerHandler(game)); // GetGuestRoomMessageComposer (ID 385)
        
        // Room listings handlers
        registry.register(430, new com.uber.server.messages.incoming.navigator.PopularRoomsSearchMessageComposerHandler(game)); // PopularRoomsSearchMessageComposer (ID 430)
        registry.register(431, new com.uber.server.messages.incoming.navigator.RoomsWithHighestScoreSearchMessageComposerHandler(game)); // RoomsWithHighestScoreSearchMessageComposer (ID 431)
        registry.register(432, new com.uber.server.messages.incoming.navigator.MyFriendsRoomsSearchMessageComposerHandler(game)); // MyFriendsRoomsSearchMessageComposer (ID 432)
        registry.register(433, new com.uber.server.messages.incoming.navigator.RoomsWhereMyFriendsAreSearchMessageComposerHandler(game)); // RoomsWhereMyFriendsAreSearchMessageComposer (ID 433)
        registry.register(434, new com.uber.server.messages.incoming.navigator.MyRoomsSearchMessageComposerHandler(game)); // MyRoomsSearchMessageComposer (ID 434)
        registry.register(435, new com.uber.server.messages.incoming.navigator.MyFavouriteRoomsSearchMessageComposerHandler(game)); // MyFavouriteRoomsSearchMessageComposer (ID 435)
        registry.register(436, new com.uber.server.messages.incoming.navigator.MyRoomHistorySearchMessageComposerHandler(game)); // MyRoomHistorySearchMessageComposer (ID 436)
        registry.register(439, new com.uber.server.messages.incoming.navigator.LatestEventsSearchMessageComposerHandler(game)); // LatestEventsSearchMessageComposer (ID 439)
        registry.register(382, new com.uber.server.messages.incoming.navigator.GetPopularRoomTagsMessageComposerHandler(game)); // GetPopularRoomTagsMessageComposer (ID 382)
        registry.register(437, new com.uber.server.messages.incoming.navigator.RoomTextSearchMessageComposerHandler(game)); // RoomTextSearchMessageComposer (ID 437)
        registry.register(438, new com.uber.server.messages.incoming.navigator.RoomTagSearchMessageComposerHandler(game)); // RoomTagSearchMessageComposer (ID 438)
        
        logger.debug("Registered navigator handlers");
    }
}
