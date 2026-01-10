package com.uber.server.game.handlers.registration;

import com.uber.server.game.Game;
import com.uber.server.messages.PacketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers room-related packet handlers.
 */
public class RoomHandlerRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(RoomHandlerRegistrar.class);
    
    private final PacketHandlerRegistry registry;
    private final Game game;
    
    public RoomHandlerRegistrar(PacketHandlerRegistry registry, Game game) {
        this.registry = registry;
        this.game = game;
    }
    
    /**
     * Registers all room handlers.
     */
    public void register() {
        // Chat handlers - using incoming IDs from _-2Pf[ID]
        registry.register(52, new com.uber.server.messages.incoming.rooms.ChatMessageComposerHandler(game)); // ChatMessageComposer (ID 52)
        registry.register(55, new com.uber.server.messages.incoming.rooms.ShoutMessageComposerHandler(game)); // ShoutMessageComposer (ID 55)
        registry.register(56, new com.uber.server.messages.incoming.rooms.WhisperMessageComposerHandler(game)); // WhisperMessageComposer (ID 56)

        registry.register(391, new com.uber.server.messages.incoming.rooms.OpenConnectionMessageComposerHandler(game)); // OpenConnectionMessageComposer (ID 391)
        
        // Room data handlers (room entry sequence)
        registry.register(215, new com.uber.server.messages.incoming.rooms.GetRoomData1MessageComposerHandler(game)); // GetRoomData1MessageComposer (ID 215)
        registry.register(390, new com.uber.server.messages.incoming.rooms.GetRoomData2MessageComposerHandler(game)); // GetRoomData2MessageComposer (ID 390)
        registry.register(126, new com.uber.server.messages.incoming.rooms.GetRoomData3MessageComposerHandler(game)); // GetRoomData3MessageComposer (ID 126)
        
        // Movement and room creation
        registry.register(75, new com.uber.server.messages.incoming.rooms.MoveAvatarMessageComposerHandler(game)); // MoveAvatarMessageComposer (ID 75)
        registry.register(29, new com.uber.server.messages.incoming.navigator.CreateFlatMessageComposerHandler(game)); // CreateFlatMessageComposer (ID 29)
        registry.register(387, new com.uber.server.messages.incoming.navigator.CanCreateRoomMessageComposerHandler(game)); // CanCreateRoomMessageComposer (ID 387)
        
        // Trade handlers
        registry.register(71, new com.uber.server.messages.incoming.rooms.InitTradeMessageComposerHandler(game)); // InitTradeMessageComposer (ID 71)
        registry.register(72, new com.uber.server.messages.incoming.rooms.OfferTradeItemMessageComposerHandler(game)); // OfferTradeItemMessageComposer (ID 72)
        registry.register(405, new com.uber.server.messages.incoming.rooms.TakeBackTradeItemMessageComposerHandler(game)); // TakeBackTradeItemMessageComposer (ID 405)
        registry.register(69, new com.uber.server.messages.incoming.rooms.AcceptTradeMessageComposerHandler(game)); // AcceptTradeMessageComposer (ID 69)
        registry.register(68, new com.uber.server.messages.incoming.rooms.UnacceptTradeMessageComposerHandler(game)); // UnacceptTradeMessageComposer (ID 68)
        registry.register(70, new com.uber.server.messages.incoming.rooms.StopTradeMessageComposerHandler(game)); // StopTradeMessageComposer (ID 70)
        registry.register(403, new com.uber.server.messages.incoming.rooms.StopTradeMessageComposerHandler(game)); // StopTradeMessageComposer (ID 403)
        registry.register(402, new com.uber.server.messages.incoming.rooms.CompleteTradeMessageComposerHandler(game)); // CompleteTradeMessageComposer (ID 402)
        
        // Room item handlers
        registry.register(90, new com.uber.server.messages.incoming.rooms.PlaceItemMessageComposerHandler(game)); // PlaceItemMessageComposer (ID 90)
        registry.register(67, new com.uber.server.messages.incoming.rooms.TakeItemMessageComposerHandler(game)); // TakeItemMessageComposer (ID 67)
        registry.register(73, new com.uber.server.messages.incoming.rooms.MoveItemMessageComposerHandler(game)); // MoveItemMessageComposer (ID 73)
        registry.register(392, new com.uber.server.handlers.rooms.TriggerItemHandler(game)); // TriggerItem (multiple IDs - verify XML)
        registry.register(393, new com.uber.server.handlers.rooms.TriggerItemHandler(game)); // TriggerItem (multiple IDs - verify XML)
        registry.register(232, new com.uber.server.handlers.rooms.TriggerItemHandler(game)); // TriggerItem (multiple IDs - verify XML)
        registry.register(314, new com.uber.server.handlers.rooms.TriggerItemHandler(game)); // TriggerItem (multiple IDs - verify XML)
        registry.register(247, new com.uber.server.handlers.rooms.TriggerItemHandler(game)); // TriggerItem (multiple IDs - verify XML)
        registry.register(76, new com.uber.server.handlers.rooms.TriggerItemHandler(game, true)); // TriggerItemDiceSpecial (ID 76 - verify XML)
        
        // Room management handlers
        registry.register(400, new com.uber.server.messages.incoming.rooms.GetRoomEditDataMessageComposerHandler(game)); // GetRoomEditDataMessageComposer (ID 400)
        registry.register(401, new com.uber.server.messages.incoming.rooms.SaveRoomDataMessageComposerHandler(game)); // SaveRoomDataMessageComposer (ID 401)
        registry.register(386, new com.uber.server.messages.incoming.rooms.SaveRoomIconMessageComposerHandler(game)); // SaveRoomIconMessageComposer (ID 386)
        registry.register(23, new com.uber.server.messages.incoming.rooms.DeleteRoomMessageComposerHandler(game)); // DeleteRoomMessageComposer (ID 23)
        registry.register(96, new com.uber.server.messages.incoming.rooms.GiveRightsMessageComposerHandler(game)); // GiveRightsMessageComposer (ID 96)
        registry.register(97, new com.uber.server.messages.incoming.rooms.TakeRightsMessageComposerHandler(game)); // TakeRightsMessageComposer (ID 97)
        registry.register(155, new com.uber.server.messages.incoming.rooms.TakeAllRightsMessageComposerHandler(game)); // TakeAllRightsMessageComposer (ID 155)
        registry.register(95, new com.uber.server.messages.incoming.rooms.KickUserMessageComposerHandler(game)); // KickUserMessageComposer (ID 95)
        registry.register(320, new com.uber.server.messages.incoming.rooms.BanUserMessageComposerHandler(game)); // BanUserMessageComposer (ID 320)
        registry.register(384, new com.uber.server.messages.incoming.rooms.SetHomeRoomMessageComposerHandler(game)); // SetHomeRoomMessageComposer (ID 384)
        
        // Room user action handlers - using incoming IDs from _-2Pf[ID]
        registry.register(94, new com.uber.server.messages.incoming.rooms.WaveMessageComposerHandler(game)); // WaveMessageComposer (ID 94)
        registry.register(93, new com.uber.server.messages.incoming.rooms.DanceMessageComposerHandler(game)); // DanceMessageComposer (ID 93)
        registry.register(79, new com.uber.server.messages.incoming.rooms.LookAtMessageComposerHandler(game)); // LookAtMessageComposer (ID 79)
        registry.register(361, new com.uber.server.messages.incoming.rooms.StartTypingMessageComposerHandler(game)); // StartTypingMessageComposer (ID 361)
        registry.register(318, new com.uber.server.messages.incoming.rooms.StopTypingMessageComposerHandler(game)); // StopTypingMessageComposer (ID 318)
        registry.register(319, new com.uber.server.messages.incoming.rooms.IgnoreUserMessageComposerHandler(game)); // IgnoreUserMessageComposer (ID 319)
        registry.register(322, new com.uber.server.messages.incoming.rooms.UnignoreUserMessageComposerHandler(game)); // UnignoreUserMessageComposer (ID 322)
        registry.register(263, new com.uber.server.messages.incoming.users.GetUserTagsMessageComposerHandler(game)); // GetUserTagsMessageComposer (ID 263)
        registry.register(159, new com.uber.server.handlers.rooms.GetUserBadgesHandler(game)); // GetUserBadges
        registry.register(261, new com.uber.server.messages.incoming.navigator.RateFlatMessageComposerHandler(game)); // RateFlatMessageComposer (ID 261)
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
        
        // Public room handlers
        registry.register(388, new com.uber.server.handlers.rooms.GetPubHandler(game)); // GetPub (ID 388)
        registry.register(2, new com.uber.server.handlers.rooms.OpenPubHandler(game)); // OpenPub (ID 2)
        
        // Pet handlers
        registry.register(3002, new com.uber.server.handlers.rooms.PlacePetHandler(game)); // PlacePet
        registry.register(3001, new com.uber.server.handlers.rooms.GetPetInfoHandler(game)); // GetPetInfo
        registry.register(3003, new com.uber.server.handlers.rooms.PickUpPetHandler(game)); // PickUpPet
        registry.register(3005, new com.uber.server.handlers.rooms.RespectPetHandler(game)); // RespectPet
        
        // Bot handlers (simplified - full BotManager support in Phase 10.5)
        registry.register(441, new com.uber.server.handlers.rooms.KickBotHandler(game)); // KickBot
        registry.register(113, new com.uber.server.handlers.rooms.EnterInfobusHandler(game)); // EnterInfobus
        
        logger.debug("Registered room handlers");
    }
}
