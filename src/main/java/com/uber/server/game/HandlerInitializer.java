package com.uber.server.game;

import com.uber.server.handlers.global.PongHandler;
import com.uber.server.messages.PacketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes and registers all packet handlers.
 * Handlers are registered with their dependencies injected.
 */
public class HandlerInitializer {
    private static final Logger logger = LoggerFactory.getLogger(HandlerInitializer.class);
    
    private final GameEnvironment environment;
    private final Game game;
    private final PacketHandlerRegistry registry;
    
    public HandlerInitializer(GameEnvironment environment, Game game, PacketHandlerRegistry registry) {
        this.environment = environment;
        this.game = game;
        this.registry = registry;
    }
    
    /**
     * Registers all packet handlers.
     */
    public void registerAllHandlers() {
        logger.info("Registering packet handlers...");
        
        // Global handlers
        registerGlobalHandlers();
        
        // Handshake handlers
        registerHandshakeHandlers();
        
        // User handlers
        registerUserHandlers();
        
        // Messenger handlers
        registerMessengerHandlers();
        
        // Navigator handlers
        registerNavigatorHandlers();
        
        // Room handlers
        registerRoomHandlers();
        
        // Catalog handlers
        registerCatalogHandlers();
        
        // Help handlers
        registerHelpHandlers();
        
        logger.info("Registered {} packet handlers", registry.size());
    }
    
    private void registerGlobalHandlers() {
        // Pong handler (ID 196)
        registry.register(196, new PongHandler());
        
        // Ping handler (ID 50) - to be implemented
        // registry.register(50, new PingHandler());
    }
    
    private void registerHandshakeHandlers() {
        // SendSessionParams handler (ID 206)
        registry.register(206, new com.uber.server.handlers.handshake.SendSessionParamsHandler());
        
        // SSO Login handler (ID 415)
        registry.register(415, new com.uber.server.handlers.handshake.SSOLoginHandler(game));
    }
    
    private void registerUserHandlers() {
        // GetUserInfo handler (ID 7)
        registry.register(7, new com.uber.server.handlers.users.GetUserInfoHandler());
        
        // GetBalance handler (ID 8)
        registry.register(8, new com.uber.server.handlers.users.GetBalanceHandler(game));
        
        // GetSubscriptionData handler (ID 26)
        registry.register(26, new com.uber.server.handlers.users.GetSubscriptionDataHandler(game));
        
        // UpdateLook handler (ID 44)
        registry.register(44, new com.uber.server.handlers.users.UpdateLookHandler(game));
        
        // GetBadges handler (ID 157)
        registry.register(157, new com.uber.server.handlers.users.GetBadgesHandler(game));
        
        // UpdateBadges handler (ID 158)
        registry.register(158, new com.uber.server.handlers.users.UpdateBadgesHandler(game));
        
        // GetInventory handler (ID 404)
        registry.register(404, new com.uber.server.handlers.users.GetInventoryHandler(game));
        
        // GetPetsInventory handler (ID 3000)
        registry.register(3000, new com.uber.server.handlers.users.GetPetsInventoryHandler(game));
        
        // GetAchievements handler (ID 370)
        registry.register(370, new com.uber.server.handlers.users.GetAchievementsHandler(game));
        
        // Other user handlers will be registered here as they are implemented
    }
    
    private void registerMessengerHandlers() {
        // InitMessenger handler (ID 12)
        registry.register(12, new com.uber.server.handlers.messenger.InitMessengerHandler());
        
        // FriendsListUpdate handler (ID 15)
        registry.register(15, new com.uber.server.handlers.messenger.FriendsListUpdateHandler());
        
        // RequestBuddy handler (ID 39)
        registry.register(39, new com.uber.server.handlers.messenger.RequestBuddyHandler(game));
        
        // Remaining messenger handlers
        registry.register(40, new com.uber.server.handlers.messenger.RemoveBuddyHandler(game)); // RemoveBuddy
        registry.register(41, new com.uber.server.handlers.messenger.SearchHabboHandler(game)); // SearchHabbo
        registry.register(37, new com.uber.server.handlers.messenger.AcceptRequestHandler(game)); // AcceptRequest
        registry.register(38, new com.uber.server.handlers.messenger.DeclineRequestHandler(game)); // DeclineRequest
        registry.register(33, new com.uber.server.handlers.messenger.SendInstantMessengerHandler(game)); // SendInstantMessenger
        registry.register(262, new com.uber.server.handlers.messenger.FollowBuddyHandler(game)); // FollowBuddy
        registry.register(34, new com.uber.server.handlers.messenger.SendInstantInviteHandler(game)); // SendInstantInvite
    }
    
    private void registerNavigatorHandlers() {
        // GetRoomCategories handler (ID 151)
        registry.register(151, new com.uber.server.handlers.navigator.GetRoomCategoriesHandler(game));
        
        // GetPubs handler (ID 380)
        registry.register(380, new com.uber.server.handlers.navigator.GetPubsHandler(game));
        
        // Favorites and room navigation handlers
        registry.register(19, new com.uber.server.handlers.navigator.AddFavoriteHandler(game)); // AddFavorite
        registry.register(20, new com.uber.server.handlers.navigator.RemoveFavoriteHandler(game)); // RemoveFavorite
        registry.register(53, new com.uber.server.handlers.navigator.GoToHotelViewHandler(game)); // GoToHotelView
        registry.register(233, new com.uber.server.handlers.navigator.EnterInquiredRoomHandler(game)); // EnterInquiredRoom
        registry.register(385, new com.uber.server.handlers.navigator.GetRoomInfoHandler(game)); // GetRoomInfo
        
        // Room listings handlers
        registry.register(430, new com.uber.server.handlers.navigator.GetPopularRoomsHandler(game)); // GetPopularRooms
        registry.register(431, new com.uber.server.handlers.navigator.GetHighRatedRoomsHandler(game)); // GetHighRatedRooms
        registry.register(432, new com.uber.server.handlers.navigator.GetFriendsRoomsHandler(game)); // GetFriendsRooms
        registry.register(433, new com.uber.server.handlers.navigator.GetRoomsWithFriendsHandler(game)); // GetRoomsWithFriends
        registry.register(434, new com.uber.server.handlers.navigator.GetOwnRoomsHandler(game)); // GetOwnRooms
        registry.register(435, new com.uber.server.handlers.navigator.GetFavoriteRoomsHandler(game)); // GetFavoriteRooms
        registry.register(436, new com.uber.server.handlers.navigator.GetRecentRoomsHandler(game)); // GetRecentRooms
        registry.register(439, new com.uber.server.handlers.navigator.GetEventsHandler(game)); // GetEvents
        registry.register(382, new com.uber.server.handlers.navigator.GetPopularTagsHandler(game)); // GetPopularTags
        registry.register(437, new com.uber.server.handlers.navigator.PerformSearchHandler(game)); // PerformSearch
        registry.register(438, new com.uber.server.handlers.navigator.PerformSearch2Handler(game)); // PerformSearch2
    }
    
    private void registerRoomHandlers() {
        // Chat handlers
        registry.register(52, new com.uber.server.handlers.rooms.ChatHandler(game, 0)); // Talk
        registry.register(55, new com.uber.server.handlers.rooms.ChatHandler(game, 1)); // Shout
        registry.register(56, new com.uber.server.handlers.rooms.ChatHandler(game, 2)); // Whisper
        
        // Enter room handlers
        registry.register(2, new com.uber.server.handlers.rooms.EnterRoomHandler(game, true)); // OpenPublicRoom
        registry.register(391, new com.uber.server.handlers.rooms.EnterRoomHandler(game, false)); // OpenPrivateRoom
        
        // Room data handlers (room entry sequence)
        registry.register(215, new com.uber.server.handlers.rooms.GetRoomData1Handler(game)); // GetRoomData1
        registry.register(390, new com.uber.server.handlers.rooms.GetRoomData2Handler(game)); // GetRoomData2
        registry.register(126, new com.uber.server.handlers.rooms.GetRoomData3Handler(game)); // GetRoomData3
        
        // Movement and room creation
        registry.register(75, new com.uber.server.handlers.rooms.MoveHandler(game)); // Move
        registry.register(29, new com.uber.server.handlers.rooms.CreateRoomHandler(game)); // CreateRoom
        registry.register(387, new com.uber.server.handlers.rooms.CanCreateRoomHandler(game)); // CanCreateRoom
        
        // Trade handlers
        registry.register(71, new com.uber.server.handlers.rooms.InitTradeHandler(game)); // InitTrade
        registry.register(72, new com.uber.server.handlers.rooms.OfferTradeItemHandler(game)); // OfferTradeItem
        registry.register(405, new com.uber.server.handlers.rooms.TakeBackTradeItemHandler(game)); // TakeBackTradeItem
        registry.register(69, new com.uber.server.handlers.rooms.AcceptTradeHandler(game)); // AcceptTrade
        registry.register(68, new com.uber.server.handlers.rooms.UnacceptTradeHandler(game)); // UnacceptTrade
        registry.register(70, new com.uber.server.handlers.rooms.StopTradeHandler(game)); // StopTrade (ID 70)
        registry.register(403, new com.uber.server.handlers.rooms.StopTradeHandler(game)); // StopTrade (ID 403)
        registry.register(402, new com.uber.server.handlers.rooms.CompleteTradeHandler(game)); // CompleteTrade
        
        // Room item handlers
        registry.register(90, new com.uber.server.handlers.rooms.PlaceItemHandler(game)); // PlaceItem
        registry.register(67, new com.uber.server.handlers.rooms.TakeItemHandler(game)); // TakeItem
        registry.register(73, new com.uber.server.handlers.rooms.MoveItemHandler(game)); // MoveItem
        registry.register(392, new com.uber.server.handlers.rooms.TriggerItemHandler(game)); // TriggerItem
        registry.register(393, new com.uber.server.handlers.rooms.TriggerItemHandler(game)); // TriggerItem
        registry.register(232, new com.uber.server.handlers.rooms.TriggerItemHandler(game)); // TriggerItem
        registry.register(314, new com.uber.server.handlers.rooms.TriggerItemHandler(game)); // TriggerItem
        registry.register(247, new com.uber.server.handlers.rooms.TriggerItemHandler(game)); // TriggerItem
        registry.register(76, new com.uber.server.handlers.rooms.TriggerItemHandler(game, true)); // TriggerItemDiceSpecial
        
        // Room management handlers
        registry.register(400, new com.uber.server.handlers.rooms.GetRoomEditDataHandler(game)); // GetRoomEditData
        registry.register(401, new com.uber.server.handlers.rooms.SaveRoomDataHandler(game)); // SaveRoomData
        registry.register(386, new com.uber.server.handlers.rooms.SaveRoomIconHandler(game)); // SaveRoomIcon
        registry.register(23, new com.uber.server.handlers.rooms.DeleteRoomHandler(game)); // DeleteRoom
        registry.register(96, new com.uber.server.handlers.rooms.GiveRightsHandler(game)); // GiveRights
        registry.register(97, new com.uber.server.handlers.rooms.TakeRightsHandler(game)); // TakeRights
        registry.register(155, new com.uber.server.handlers.rooms.TakeAllRightsHandler(game)); // TakeAllRights
        registry.register(95, new com.uber.server.handlers.rooms.KickUserHandler(game)); // KickUser
        registry.register(320, new com.uber.server.handlers.rooms.BanUserHandler(game)); // BanUser
        registry.register(384, new com.uber.server.handlers.rooms.SetHomeRoomHandler(game)); // SetHomeRoom
        
        // Room user action handlers
        registry.register(94, new com.uber.server.handlers.rooms.WaveHandler(game)); // Wave
        registry.register(93, new com.uber.server.handlers.rooms.DanceHandler(game)); // Dance
        registry.register(79, new com.uber.server.handlers.rooms.LookAtHandler(game)); // LookAt
        registry.register(317, new com.uber.server.handlers.rooms.StartTypingHandler(game)); // StartTyping
        registry.register(318, new com.uber.server.handlers.rooms.StopTypingHandler(game)); // StopTyping
        registry.register(319, new com.uber.server.handlers.rooms.IgnoreUserHandler(game)); // IgnoreUser
        registry.register(322, new com.uber.server.handlers.rooms.UnignoreUserHandler(game)); // UnignoreUser
        registry.register(263, new com.uber.server.handlers.rooms.GetUserTagsHandler(game)); // GetUserTags
        registry.register(159, new com.uber.server.handlers.rooms.GetUserBadgesHandler(game)); // GetUserBadges
        registry.register(261, new com.uber.server.handlers.rooms.RateRoomHandler(game)); // RateRoom
        registry.register(98, new com.uber.server.handlers.rooms.AnswerDoorbellHandler(game)); // AnswerDoorbell
        registry.register(371, new com.uber.server.handlers.rooms.GiveRespectHandler(game)); // GiveRespect
        registry.register(372, new com.uber.server.handlers.rooms.ApplyEffectHandler(game)); // ApplyEffect
        registry.register(373, new com.uber.server.handlers.rooms.EnableEffectHandler(game)); // EnableEffect
        
        // Room event handlers
        registry.register(345, new com.uber.server.handlers.rooms.CanCreateRoomEventHandler(game)); // CanCreateRoomEvent
        registry.register(346, new com.uber.server.handlers.rooms.StartEventHandler(game)); // StartEvent
        registry.register(347, new com.uber.server.handlers.rooms.StopEventHandler(game)); // StopEvent
        registry.register(348, new com.uber.server.handlers.rooms.EditEventHandler(game)); // EditEvent
        
        // Moodlight handlers
        registry.register(341, new com.uber.server.handlers.rooms.GetMoodlightHandler(game)); // GetMoodlight
        registry.register(342, new com.uber.server.handlers.rooms.UpdateMoodlightHandler(game)); // UpdateMoodlight
        registry.register(343, new com.uber.server.handlers.rooms.SwitchMoodlightStatusHandler(game)); // SwitchMoodlightStatus
        
        // Postit and Present handlers
        registry.register(83, new com.uber.server.handlers.rooms.OpenPostitHandler(game)); // OpenPostit
        registry.register(84, new com.uber.server.handlers.rooms.SavePostitHandler(game)); // SavePostit
        registry.register(85, new com.uber.server.handlers.rooms.DeletePostitHandler(game)); // DeletePostit
        registry.register(78, new com.uber.server.handlers.rooms.OpenPresentHandler(game)); // OpenPresent
        registry.register(66, new com.uber.server.handlers.rooms.ApplyRoomEffectHandler(game)); // ApplyRoomEffect
        registry.register(414, new com.uber.server.handlers.rooms.RecycleItemsHandler(game)); // RecycleItems
        registry.register(183, new com.uber.server.handlers.rooms.RedeemExchangeFurniHandler(game)); // RedeemExchangeFurni
        
        // Miscellaneous room handlers
        registry.register(182, new com.uber.server.handlers.rooms.GetAdvertisementHandler(game)); // GetAdvertisement
        registry.register(230, new com.uber.server.handlers.rooms.GetGroupBadgesHandler(game)); // GetGroupBadges
        registry.register(59, new com.uber.server.handlers.rooms.ReqLoadRoomForUserHandler(game)); // ReqLoadRoomForUser
        
        // Pet handlers
        registry.register(3002, new com.uber.server.handlers.rooms.PlacePetHandler(game)); // PlacePet
        registry.register(3001, new com.uber.server.handlers.rooms.GetPetInfoHandler(game)); // GetPetInfo
        registry.register(3003, new com.uber.server.handlers.rooms.PickUpPetHandler(game)); // PickUpPet
        registry.register(3005, new com.uber.server.handlers.rooms.RespectPetHandler(game)); // RespectPet
        
        // Bot handlers (simplified - full BotManager support in Phase 10.5)
        registry.register(441, new com.uber.server.handlers.rooms.KickBotHandler(game)); // KickBot
        registry.register(113, new com.uber.server.handlers.rooms.EnterInfobusHandler(game)); // EnterInfobus
    }
    
    private void registerCatalogHandlers() {
        // GetCatalogIndex handler (ID 101)
        registry.register(101, new com.uber.server.handlers.catalog.GetCatalogIndexHandler(game));
        
        // GetCatalogPage handler (ID 102)
        registry.register(102, new com.uber.server.handlers.catalog.GetCatalogPageHandler(game));
        
        // HandlePurchase handler (ID 100) - CRITICAL
        registry.register(100, new com.uber.server.handlers.catalog.HandlePurchaseHandler(game));
        
        // RedeemVoucher handler (ID 129)
        registry.register(129, new com.uber.server.handlers.catalog.RedeemVoucherHandler(game));
        
        // Advanced catalog handlers
        registry.register(472, new com.uber.server.handlers.catalog.PurchaseGiftHandler(game)); // PurchaseGift
        registry.register(412, new com.uber.server.handlers.catalog.GetRecyclerRewardsHandler(game)); // GetRecyclerRewards
        registry.register(3030, new com.uber.server.handlers.catalog.CanGiftHandler(game)); // CanGift
        registry.register(3011, new com.uber.server.handlers.catalog.GetCatalogData1Handler(game)); // GetCatalogData1
        registry.register(473, new com.uber.server.handlers.catalog.GetCatalogData2Handler(game)); // GetCatalogData2
        registry.register(42, new com.uber.server.handlers.catalog.CheckPetNameHandler(game)); // CheckPetName
        
        // Marketplace handlers
        registry.register(3012, new com.uber.server.handlers.catalog.MarketplaceCanSellHandler(game)); // MarketplaceCanSell
        registry.register(3010, new com.uber.server.handlers.catalog.MarketplacePostItemHandler(game)); // MarketplacePostItem
        registry.register(3019, new com.uber.server.handlers.catalog.MarketplaceGetOwnOffersHandler(game)); // MarketplaceGetOwnOffers
        registry.register(3015, new com.uber.server.handlers.catalog.MarketplaceTakeBackHandler(game)); // MarketplaceTakeBack
        registry.register(3016, new com.uber.server.handlers.catalog.MarketplaceClaimCreditsHandler(game)); // MarketplaceClaimCredits
        registry.register(3018, new com.uber.server.handlers.catalog.MarketplaceGetOffersHandler(game)); // MarketplaceGetOffers
        registry.register(3014, new com.uber.server.handlers.catalog.MarketplacePurchaseHandler(game)); // MarketplacePurchase
    }
    
    private void registerHelpHandlers() {
        // InitHelpTool handler (ID 416)
        registry.register(416, new com.uber.server.handlers.help.InitHelpToolHandler(game));
        
        // GetHelpCategories handler (ID 417)
        registry.register(417, new com.uber.server.handlers.help.GetHelpCategoriesHandler(game));
        
        // ViewHelpTopic handler (ID 418)
        registry.register(418, new com.uber.server.handlers.help.ViewHelpTopicHandler(game));
        
        // SearchHelpTopics handler (ID 419)
        registry.register(419, new com.uber.server.handlers.help.SearchHelpTopicsHandler(game));
        
        // GetTopicsInCategory handler (ID 420)
        registry.register(420, new com.uber.server.handlers.help.GetTopicsInCategoryHandler(game));
        
        // Help ticket handlers
        registry.register(453, new com.uber.server.handlers.help.SubmitHelpTicketHandler(game)); // SubmitHelpTicket
        registry.register(238, new com.uber.server.handlers.help.DeletePendingCallForHelpHandler(game)); // DeletePendingCallForHelp
        
        // CallGuideBot handler (ID 440) - placeholder until BotManager is ported
        registry.register(440, new com.uber.server.handlers.help.CallGuideBotHandler(game)); // CallGuideBot
        
        // Moderation handlers (require fuse permissions)
        registry.register(200, new com.uber.server.handlers.help.ModSendRoomAlertHandler(game)); // ModSendRoomAlert
        registry.register(450, new com.uber.server.handlers.help.ModPickTicketHandler(game)); // ModPickTicket
        registry.register(451, new com.uber.server.handlers.help.ModReleaseTicketHandler(game)); // ModReleaseTicket
        registry.register(452, new com.uber.server.handlers.help.ModCloseTicketHandler(game)); // ModCloseTicket
        registry.register(454, new com.uber.server.handlers.help.ModGetUserInfoHandler(game)); // ModGetUserInfo
        registry.register(455, new com.uber.server.handlers.help.ModGetUserChatlogHandler(game)); // ModGetUserChatlog
        registry.register(456, new com.uber.server.handlers.help.ModGetRoomChatlogHandler(game)); // ModGetRoomChatlog
        registry.register(457, new com.uber.server.handlers.help.ModGetTicketChatlogHandler(game)); // ModGetTicketChatlog
        registry.register(458, new com.uber.server.handlers.help.ModGetRoomVisitsHandler(game)); // ModGetRoomVisits
        registry.register(459, new com.uber.server.handlers.help.ModGetRoomToolHandler(game)); // ModGetRoomTool
        registry.register(460, new com.uber.server.handlers.help.ModPerformRoomActionHandler(game)); // ModPerformRoomAction
        registry.register(461, new com.uber.server.handlers.help.ModSendUserCautionHandler(game)); // ModSendUserCaution
        registry.register(462, new com.uber.server.handlers.help.ModSendUserMessageHandler(game)); // ModSendUserMessage
        registry.register(463, new com.uber.server.handlers.help.ModKickUserHandler(game)); // ModKickUser
        registry.register(464, new com.uber.server.handlers.help.ModBanUserHandler(game)); // ModBanUser
    }
}
