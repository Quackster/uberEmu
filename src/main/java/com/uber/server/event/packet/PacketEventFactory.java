package com.uber.server.event.packet;

import com.uber.server.event.packet.catalog.*;
import com.uber.server.event.packet.global.PongEvent;
import com.uber.server.event.packet.handshake.*;
import com.uber.server.event.packet.help.*;
import com.uber.server.event.packet.messenger.*;
import com.uber.server.event.packet.navigator.*;
import com.uber.server.event.packet.room.*;
import com.uber.server.event.packet.user.*;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating packet events from incoming messages.
 * Maps packet IDs to event classes.
 * 
 * NOTE: This factory is currently NOT USED in the codebase. Handlers create events directly
 * within their handle() methods. This class serves as a reference implementation showing
 * the correct event creation patterns for each packet ID. It may be used in the future
 * for centralized event creation or plugin systems.
 * 
 * If you need to understand how to create events for a specific packet, refer to this
 * factory's implementation as the authoritative source for event creation patterns.
 */
public class PacketEventFactory {
    private static final Logger logger = LoggerFactory.getLogger(PacketEventFactory.class);
    
    /**
     * Creates a packet event from a message.
     * This method is not currently called by the codebase - handlers create events directly.
     * Kept as a reference implementation for event creation patterns.
     * 
     * @param client GameClient that sent the message
     * @param message ClientMessage received
     * @return PacketReceiveEvent instance, or GenericPacketEvent if event type not supported
     */
    public static PacketReceiveEvent createEvent(GameClient client, ClientMessage message) {
        int packetId = (int) message.getId();
        
        try {
            // Reset message pointer to start for parsing
            message.resetPointer();
            
            PacketReceiveEvent event = null;
            
            switch (packetId) {
                // Handshake events
                case 206:
                    event = new SendSessionParametersEvent(client, message);
                    break;
                case 1817:
                    event = new GetSessionParametersEvent(client, message);
                    break;
                case 415:
                    String ssoTicket = message.popFixedString();
                    event = new SSOTicketEvent(client, message, ssoTicket);
                    break;
                
                // User events
                case 7:
                    event = new InfoRetrieveEvent(client, message);
                    break;
                case 8:
                    event = new GetCreditsInfoEvent(client, message);
                    break;
                case 26:
                    event = new GetSubscriptionDataEvent(client, message);
                    break;
                case 44:
                    String gender = message.popFixedString();
                    String figure = message.popFixedString();
                    event = new ChangeLooksEvent(client, message, gender, figure);
                    break;
                case 157:
                    event = new GetBadgesEvent(client, message);
                    break;
                case 158:
                    List<String> badges = new ArrayList<>();
                    int badgeCount = message.popWiredInt32();
                    for (int i = 0; i < badgeCount; i++) {
                        badges.add(message.popFixedString());
                    }
                    event = new SetActivatedBadgesEvent(client, message, badges);
                    break;
                case 263:
                    long userId = message.popWiredUInt();
                    event = new GetUserTagsEvent(client, message, userId);
                    break;
                case 370:
                    event = new GetAchievementsEvent(client, message);
                    break;
                case 375:
                    event = new GetWardrobeEvent(client, message);
                    break;
                case 376:
                    int slotId = message.popWiredInt32();
                    String wardrobeFigure = message.popFixedString();
                    String wardrobeGender = message.popFixedString();
                    event = new SaveWardrobeEvent(client, message, slotId, wardrobeFigure, wardrobeGender);
                    break;
                case 404:
                    event = new RequestFurniInventoryEvent(client, message);
                    break;
                case 3000:
                    event = new GetPetInventoryEvent(client, message);
                    break;
                
                // Room events - Chat
                case 52:
                    String chatMsg = message.popFixedString();
                    event = new ChatMessageEvent(client, message, chatMsg);
                    break;
                case 55:
                    String shoutMsg = message.popFixedString();
                    event = new ShoutMessageEvent(client, message, shoutMsg);
                    break;
                case 56:
                    String whisperMsg = message.popFixedString();
                    String targetUser = message.popFixedString();
                    event = new WhisperMessageEvent(client, message, whisperMsg, targetUser);
                    break;
                
                // Room events - Movement
                case 75:
                    int moveX = message.popWiredInt32();
                    int moveY = message.popWiredInt32();
                    event = new MoveAvatarEvent(client, message, moveX, moveY);
                    break;
                
                // Room events - Entry
                case 215:
                    int roomId1 = message.popWiredInt32();
                    event = new GetRoomData1Event(client, message, roomId1);
                    break;
                case 390:
                    int roomId2 = message.popWiredInt32();
                    event = new GetRoomData2Event(client, message, roomId2);
                    break;
                case 126:
                    int roomId3 = message.popWiredInt32();
                    event = new GetRoomData3Event(client, message, roomId3);
                    break;
                case 391:
                    int roomIdOpen = message.popWiredInt32();
                    event = new OpenConnectionEvent(client, message, roomIdOpen);
                    break;
                case 53:
                    event = new QuitRoomEvent(client, message);
                    break;
                
                // Room events - Items
                case 90:
                    long placeItemId = message.popWiredUInt();
                    int placeX = message.popWiredInt32();
                    int placeY = message.popWiredInt32();
                    int placeRot = message.popWiredInt32();
                    event = new PlaceItemEvent(client, message, placeItemId, placeX, placeY, placeRot);
                    break;
                case 67:
                    long takeItemId = message.popWiredUInt();
                    event = new TakeItemEvent(client, message, takeItemId);
                    break;
                case 73:
                    long moveItemId = message.popWiredUInt();
                    int moveItemX = message.popWiredInt32();
                    int moveItemY = message.popWiredInt32();
                    int moveItemRot = message.popWiredInt32();
                    event = new MoveItemEvent(client, message, moveItemId, moveItemX, moveItemY, moveItemRot);
                    break;
                case 392:
                case 393:
                case 232:
                case 314:
                case 247:
                case 76:
                    long triggerItemId = message.popWiredUInt();
                    int parameter = message.popWiredInt32();
                    event = new TriggerItemEvent(client, message, packetId, triggerItemId, parameter);
                    break;
                
                // Room events - Trade
                case 71:
                    long tradeUserId = message.popWiredUInt();
                    event = new InitTradeEvent(client, message, tradeUserId);
                    break;
                case 72:
                    long offerItemId = message.popWiredUInt();
                    event = new OfferTradeItemEvent(client, message, offerItemId);
                    break;
                case 405:
                    long takeBackItemId = message.popWiredUInt();
                    event = new TakeBackTradeItemEvent(client, message, takeBackItemId);
                    break;
                case 69:
                    event = new AcceptTradeEvent(client, message);
                    break;
                case 68:
                    event = new UnacceptTradeEvent(client, message);
                    break;
                case 70:
                case 403:
                    event = new StopTradeEvent(client, message, packetId);
                    break;
                case 402:
                    event = new CompleteTradeEvent(client, message);
                    break;
                
                // Room events - Management
                case 400:
                    int editRoomId = message.popWiredInt32();
                    event = new GetRoomEditDataEvent(client, message, editRoomId);
                    break;
                case 401:
                    int saveRoomId = message.popWiredInt32();
                    String roomName = message.popFixedString();
                    String roomDesc = message.popFixedString();
                    int roomState = message.popWiredInt32();
                    String roomPassword = message.popFixedString();
                    int roomMaxUsers = message.popWiredInt32();
                    int roomCategory = message.popWiredInt32();
                    int roomTags = message.popWiredInt32();
                    event = new SaveRoomDataEvent(client, message, saveRoomId, roomName, roomDesc, 
                                                 roomState, roomPassword, roomMaxUsers, roomCategory, roomTags);
                    break;
                case 386:
                    int iconRoomId = message.popWiredInt32();
                    String iconData = message.popFixedString();
                    event = new SaveRoomIconEvent(client, message, iconRoomId, iconData);
                    break;
                case 23:
                    int deleteRoomId = message.popWiredInt32();
                    event = new DeleteRoomEvent(client, message, deleteRoomId);
                    break;
                case 96:
                    long giveRightsUserId = message.popWiredUInt();
                    event = new GiveRightsEvent(client, message, giveRightsUserId);
                    break;
                case 97:
                    long takeRightsUserId = message.popWiredUInt();
                    event = new TakeRightsEvent(client, message, takeRightsUserId);
                    break;
                case 155:
                    event = new TakeAllRightsEvent(client, message);
                    break;
                case 95:
                    long kickUserId = message.popWiredUInt();
                    event = new KickUserEvent(client, message, kickUserId);
                    break;
                case 320:
                    long banUserId = message.popWiredUInt();
                    event = new BanUserEvent(client, message, banUserId);
                    break;
                case 384:
                    int homeRoomId = message.popWiredInt32();
                    event = new SetHomeRoomEvent(client, message, homeRoomId);
                    break;
                
                // Room events - Actions
                case 94:
                    event = new WaveEvent(client, message);
                    break;
                case 93:
                    int danceId = message.popWiredInt32();
                    event = new DanceEvent(client, message, danceId);
                    break;
                case 79:
                    int lookX = message.popWiredInt32();
                    int lookY = message.popWiredInt32();
                    event = new LookAtEvent(client, message, lookX, lookY);
                    break;
                case 361:
                    event = new StartTypingEvent(client, message);
                    break;
                case 318:
                    event = new StopTypingEvent(client, message);
                    break;
                case 319:
                    String ignoreUser = message.popFixedString();
                    event = new IgnoreUserEvent(client, message, ignoreUser);
                    break;
                case 322:
                    String unignoreUser = message.popFixedString();
                    event = new UnignoreUserEvent(client, message, unignoreUser);
                    break;
                case 261:
                    int rateRoomId = message.popWiredInt32();
                    int rating = message.popWiredInt32();
                    event = new RateFlatEvent(client, message, rateRoomId, rating);
                    break;
                case 98:
                    String doorbellUser = message.popFixedString();
                    byte[] doorbellResultBytes = message.readBytes(1);
                    byte doorbellAnswer = (doorbellResultBytes != null && doorbellResultBytes.length > 0) ? doorbellResultBytes[0] : 0;
                    event = new AnswerDoorbellEvent(client, message, doorbellUser, doorbellAnswer);
                    break;
                case 371:
                    long respectUserId = message.popWiredUInt();
                    event = new GiveRespectEvent(client, message, respectUserId);
                    break;
                case 372:
                    int applyEffectId = message.popWiredInt32();
                    event = new ApplyEffectEvent(client, message, applyEffectId);
                    break;
                case 373:
                    int enableEffectId = message.popWiredInt32();
                    event = new EnableEffectEvent(client, message, enableEffectId);
                    break;
                case 345:
                    event = new CanCreateRoomEventEvent(client, message);
                    break;
                case 346:
                    int startEventId = message.popWiredInt32();
                    int startCategory = message.popWiredInt32();
                    String startName = message.popFixedString();
                    String startDesc = message.popFixedString();
                    int startTagCount = message.popWiredInt32();
                    List<String> startTags = new ArrayList<>();
                    for (int i = 0; i < startTagCount; i++) {
                        startTags.add(message.popFixedString());
                    }
                    event = new StartEventEvent(client, message, startEventId, startCategory, startName, startDesc, startTags);
                    break;
                case 347:
                    int stopEventId = message.popWiredInt32();
                    event = new StopEventEvent(client, message, stopEventId);
                    break;
                case 348:
                    int editEventId = message.popWiredInt32();
                    String eventName = message.popFixedString();
                    String eventDesc = message.popFixedString();
                    int editCategory = message.popWiredInt32();
                    int editTagCount = message.popWiredInt32();
                    List<String> editTags = new ArrayList<>();
                    for (int i = 0; i < editTagCount; i++) {
                        editTags.add(message.popFixedString());
                    }
                    event = new EditEventEvent(client, message, editEventId, eventName, eventDesc, editCategory, editTags);
                    break;
                case 341:
                    event = new GetMoodlightEvent(client, message);
                    break;
                case 342:
                    int moodlightPreset = message.popWiredInt32();
                    String moodlightColor = message.popFixedString();
                    int moodlightIntensity = message.popWiredInt32();
                    boolean moodlightBgOnly = message.popWiredBoolean();
                    event = new UpdateMoodlightEvent(client, message, moodlightPreset, moodlightColor,
                                                    moodlightIntensity, moodlightBgOnly);
                    break;
                case 343:
                    int switchPresetId = message.popWiredInt32();
                    event = new SwitchMoodlightStatusEvent(client, message, switchPresetId);
                    break;
                case 83:
                    long openPostitId = message.popWiredUInt();
                    event = new OpenPostitEvent(client, message, openPostitId);
                    break;
                case 84:
                    long savePostitId = message.popWiredUInt();
                    String postitText = message.popFixedString();
                    String postitColor = message.popFixedString();
                    event = new SavePostitEvent(client, message, savePostitId, postitText, postitColor);
                    break;
                case 85:
                    long deletePostitId = message.popWiredUInt();
                    event = new DeletePostitEvent(client, message, deletePostitId);
                    break;
                case 78:
                    long openPresentId = message.popWiredUInt();
                    event = new OpenPresentEvent(client, message, openPresentId);
                    break;
                case 66:
                    int roomEffectId = message.popWiredInt32();
                    event = new ApplyRoomEffectEvent(client, message, roomEffectId);
                    break;
                case 414:
                    List<Long> recycleItemIds = new ArrayList<>();
                    int recycleCount = message.popWiredInt32();
                    for (int i = 0; i < recycleCount; i++) {
                        recycleItemIds.add(message.popWiredUInt());
                    }
                    event = new RecycleItemsEvent(client, message, recycleItemIds);
                    break;
                case 183:
                    long redeemItemId = message.popWiredUInt();
                    event = new RedeemExchangeFurniEvent(client, message, redeemItemId);
                    break;
                case 182:
                    event = new GetAdvertisementEvent(client, message);
                    break;
                case 230:
                    event = new GetGroupBadgesEvent(client, message);
                    break;
                case 59:
                    long loadRoomUserId = message.popWiredUInt();
                    event = new ReqLoadRoomForUserEvent(client, message, loadRoomUserId);
                    break;
                case 388:
                    event = new GetPubEvent(client, message);
                    break;
                case 2:
                    event = new OpenPubEvent(client, message);
                    break;
                case 3002:
                    long placePetId = message.popWiredUInt();
                    int placePetX = message.popWiredInt32();
                    int placePetY = message.popWiredInt32();
                    event = new PlacePetEvent(client, message, placePetId, placePetX, placePetY);
                    break;
                case 3001:
                    long getPetInfoId = message.popWiredUInt();
                    event = new GetPetInfoEvent(client, message, getPetInfoId);
                    break;
                case 3003:
                    long pickUpPetId = message.popWiredUInt();
                    event = new PickUpPetEvent(client, message, pickUpPetId);
                    break;
                case 3005:
                    long respectPetId = message.popWiredUInt();
                    event = new RespectPetEvent(client, message, respectPetId);
                    break;
                case 441:
                    int kickBotId = message.popWiredInt32();
                    event = new KickBotEvent(client, message, kickBotId);
                    break;
                case 113:
                    event = new EnterInfobusEvent(client, message);
                    break;
                case 159:
                    long getUserBadgesId = message.popWiredUInt();
                    event = new GetUserBadgesEvent(client, message, getUserBadgesId);
                    break;
                
                // Navigator events
                case 151:
                    event = new GetUserFlatCatsEvent(client, message);
                    break;
                case 380:
                    event = new GetOfficialRoomsEvent(client, message);
                    break;
                case 19:
                    int addFavRoomId = message.popWiredInt32();
                    event = new AddFavouriteRoomEvent(client, message, addFavRoomId);
                    break;
                case 20:
                    int delFavRoomId = message.popWiredInt32();
                    event = new DeleteFavouriteRoomEvent(client, message, delFavRoomId);
                    break;
                case 233:
                    int enterRoomId = message.popWiredInt32();
                    event = new EnterInquiredRoomEvent(client, message, enterRoomId);
                    break;
                case 385:
                    int guestRoomId = message.popWiredInt32();
                    event = new GetGuestRoomEvent(client, message, guestRoomId);
                    break;
                case 29:
                    String flatName = message.popFixedString();
                    String flatDesc = message.popFixedString();
                    String flatModel = message.popFixedString();
                    int flatCategory = message.popWiredInt32();
                    int flatMaxUsers = message.popWiredInt32();
                    int flatTradeMode = message.popWiredInt32();
                    event = new CreateFlatEvent(client, message, flatName, flatDesc, flatModel, 
                                               flatCategory, flatMaxUsers, flatTradeMode);
                    break;
                case 387:
                    event = new CanCreateRoomEvent(client, message);
                    break;
                case 430:
                    event = new PopularRoomsSearchEvent(client, message);
                    break;
                case 431:
                    event = new RoomsWithHighestScoreSearchEvent(client, message);
                    break;
                case 432:
                    event = new MyFriendsRoomsSearchEvent(client, message);
                    break;
                case 433:
                    event = new RoomsWhereMyFriendsAreSearchEvent(client, message);
                    break;
                case 434:
                    event = new MyRoomsSearchEvent(client, message);
                    break;
                case 435:
                    event = new MyFavouriteRoomsSearchEvent(client, message);
                    break;
                case 436:
                    event = new MyRoomHistorySearchEvent(client, message);
                    break;
                case 437:
                    String searchText = message.popFixedString();
                    event = new RoomTextSearchEvent(client, message, searchText);
                    break;
                case 438:
                    String tag = message.popFixedString();
                    event = new RoomTagSearchEvent(client, message, tag);
                    break;
                case 439:
                    event = new LatestEventsSearchEvent(client, message);
                    break;
                case 382:
                    event = new GetPopularRoomTagsEvent(client, message);
                    break;
                
                // Messenger events
                case 12:
                    event = new MessengerInitEvent(client, message);
                    break;
                case 15:
                    event = new FriendListUpdateEvent(client, message);
                    break;
                case 39:
                    String requestBuddyName = message.popFixedString();
                    event = new RequestBuddyEvent(client, message, requestBuddyName);
                    break;
                case 40:
                    long removeBuddyId = message.popWiredUInt();
                    event = new RemoveBuddyEvent(client, message, removeBuddyId);
                    break;
                case 41:
                    String searchQuery = message.popFixedString();
                    event = new HabboSearchEvent(client, message, searchQuery);
                    break;
                case 37:
                    List<Long> acceptIds = new ArrayList<>();
                    int acceptCount = message.popWiredInt32();
                    for (int i = 0; i < acceptCount; i++) {
                        acceptIds.add(message.popWiredUInt());
                    }
                    event = new AcceptBuddyEvent(client, message, acceptIds);
                    break;
                case 38:
                    List<Long> declineIds = new ArrayList<>();
                    int declineCount = message.popWiredInt32();
                    for (int i = 0; i < declineCount; i++) {
                        declineIds.add(message.popWiredUInt());
                    }
                    event = new DeclineBuddyEvent(client, message, declineIds);
                    break;
                case 33:
                    long msgUserId = message.popWiredUInt();
                    String msgText = message.popFixedString();
                    event = new SendMsgEvent(client, message, msgUserId, msgText);
                    break;
                case 262:
                    long followUserId = message.popWiredUInt();
                    event = new FollowFriendEvent(client, message, followUserId);
                    break;
                case 34:
                    List<Long> inviteIds = new ArrayList<>();
                    int inviteCount = message.popWiredInt32();
                    for (int i = 0; i < inviteCount; i++) {
                        inviteIds.add(message.popWiredUInt());
                    }
                    String inviteMsg = message.popFixedString();
                    event = new SendRoomInviteEvent(client, message, inviteIds, inviteMsg);
                    break;
                
                // Catalog events
                case 101:
                    event = new GetCatalogIndexEvent(client, message);
                    break;
                case 102:
                    int pageId = message.popWiredInt32();
                    event = new GetCatalogPageEvent(client, message, pageId);
                    break;
                case 100:
                    int purchasePageId = message.popWiredInt32();
                    List<Integer> purchaseItemIds = new ArrayList<>();
                    int purchaseItemCount = message.popWiredInt32();
                    for (int i = 0; i < purchaseItemCount; i++) {
                        purchaseItemIds.add(message.popWiredInt32());
                    }
                    String purchaseExtraData = message.popFixedString();
                    String purchaseRecipient = message.popFixedString();
                    String purchaseGiftMsg = message.popFixedString();
                    event = new HandlePurchaseEvent(client, message, purchasePageId, purchaseItemIds, 
                                                   purchaseExtraData, purchaseRecipient, purchaseGiftMsg);
                    break;
                case 129:
                    String voucherCode = message.popFixedString();
                    event = new RedeemVoucherEvent(client, message, voucherCode);
                    break;
                case 42:
                    String petName = message.popFixedString();
                    event = new CheckPetNameEvent(client, message, petName);
                    break;
                case 412:
                    event = new GetRecyclerRewardsEvent(client, message);
                    break;
                case 3030:
                    event = new CanGiftEvent(client, message);
                    break;
                case 3011:
                    event = new GetCatalogData1Event(client, message);
                    break;
                case 473:
                    event = new GetCatalogData2Event(client, message);
                    break;
                case 472:
                    int giftPageId = message.popWiredInt32();
                    int giftItemId = message.popWiredInt32();
                    String giftExtraData = message.popFixedString();
                    String giftRecipient = message.popFixedString();
                    String giftMsg = message.popFixedString();
                    int giftSpriteId = message.popWiredInt32();
                    int giftRibbon = message.popWiredInt32();
                    int giftBox = message.popWiredInt32();
                    event = new PurchaseGiftEvent(client, message, giftPageId, giftItemId, giftExtraData,
                                                  giftRecipient, giftMsg, giftSpriteId, giftRibbon, giftBox);
                    break;
                case 3012:
                    event = new MarketplaceCanSellEvent(client, message);
                    break;
                case 3010:
                    long postItemId = message.popWiredUInt();
                    int postPrice = message.popWiredInt32();
                    event = new MarketplacePostItemEvent(client, message, postItemId, postPrice);
                    break;
                case 3019:
                    event = new MarketplaceGetOwnOffersEvent(client, message);
                    break;
                case 3015:
                    int takeBackOfferId = message.popWiredInt32();
                    event = new MarketplaceTakeBackEvent(client, message, takeBackOfferId);
                    break;
                case 3016:
                    event = new MarketplaceClaimCreditsEvent(client, message);
                    break;
                case 3018:
                    String marketplaceSearch = message.popFixedString();
                    int minPrice = message.popWiredInt32();
                    int maxPrice = message.popWiredInt32();
                    event = new MarketplaceGetOffersEvent(client, message, marketplaceSearch, minPrice, maxPrice);
                    break;
                case 3014:
                    int purchaseOfferId = message.popWiredInt32();
                    event = new MarketplacePurchaseEvent(client, message, purchaseOfferId);
                    break;
                
                // Help events
                case 416:
                    event = new GetClientFaqsEvent(client, message);
                    break;
                case 417:
                    event = new GetFaqCategoriesEvent(client, message);
                    break;
                case 418:
                    int topicId = message.popWiredInt32();
                    event = new GetFaqTextEvent(client, message, topicId);
                    break;
                case 419:
                    String faqSearch = message.popFixedString();
                    event = new SearchFaqsEvent(client, message, faqSearch);
                    break;
                case 420:
                    int categoryId = message.popWiredInt32();
                    event = new GetFaqCategoryEvent(client, message, categoryId);
                    break;
                case 238:
                    event = new DeletePendingCallsForHelpEvent(client, message);
                    break;
                case 440:
                    event = new CallGuideBotEvent(client, message);
                    break;
                case 453:
                    String helpMsg = message.popFixedString();
                    int helpCategoryId = message.popWiredInt32();
                    int helpRoomId = message.popWiredInt32();
                    event = new CallForHelpEvent(client, message, helpMsg, helpCategoryId, helpRoomId);
                    break;
                
                // Moderation events
                case 200:
                    int alertRoomId = message.popWiredInt32();
                    String alertMsg = message.popFixedString();
                    event = new ModSendRoomAlertEvent(client, message, alertRoomId, alertMsg);
                    break;
                case 450:
                    int pickTicketId = message.popWiredInt32();
                    event = new ModPickTicketEvent(client, message, pickTicketId);
                    break;
                case 451:
                    int releaseTicketId = message.popWiredInt32();
                    event = new ModReleaseTicketEvent(client, message, releaseTicketId);
                    break;
                case 452:
                    int closeTicketId = message.popWiredInt32();
                    event = new ModCloseTicketEvent(client, message, closeTicketId);
                    break;
                case 454:
                    long modUserInfoId = message.popWiredUInt();
                    event = new ModGetUserInfoEvent(client, message, modUserInfoId);
                    break;
                case 455:
                    long modChatlogUserId = message.popWiredUInt();
                    event = new ModGetUserChatlogEvent(client, message, modChatlogUserId);
                    break;
                case 456:
                    int modRoomChatlogId = message.popWiredInt32();
                    event = new ModGetRoomChatlogEvent(client, message, modRoomChatlogId);
                    break;
                case 457:
                    int modTicketChatlogId = message.popWiredInt32();
                    event = new ModGetTicketChatlogEvent(client, message, modTicketChatlogId);
                    break;
                case 458:
                    int modRoomVisitsId = message.popWiredInt32();
                    event = new ModGetRoomVisitsEvent(client, message, modRoomVisitsId);
                    break;
                case 459:
                    int modRoomToolId = message.popWiredInt32();
                    event = new ModGetRoomToolEvent(client, message, modRoomToolId);
                    break;
                case 460:
                    int modRoomActionId = message.popWiredInt32();
                    int modAction = message.popWiredInt32();
                    event = new ModPerformRoomActionEvent(client, message, modRoomActionId, modAction);
                    break;
                case 461:
                    long modCautionUserId = message.popWiredUInt();
                    String cautionMsg = message.popFixedString();
                    event = new ModSendUserCautionEvent(client, message, modCautionUserId, cautionMsg);
                    break;
                case 462:
                    long modMessageUserId = message.popWiredUInt();
                    String modUserMsg = message.popFixedString();
                    event = new ModSendUserMessageEvent(client, message, modMessageUserId, modUserMsg);
                    break;
                case 463:
                    long modKickUserId = message.popWiredUInt();
                    String kickMsg = message.popFixedString();
                    event = new ModKickUserEvent(client, message, modKickUserId, kickMsg);
                    break;
                case 464:
                    long modBanUserId = message.popWiredUInt();
                    String banMsg = message.popFixedString();
                    int banHours = message.popWiredInt32();
                    event = new ModBanUserEvent(client, message, modBanUserId, banMsg, banHours);
                    break;
                
                // Global events
                case 196:
                    event = new PongEvent(client, message);
                    break;
                
                default:
                    // Use generic event for packets without specific event classes
                    logger.debug("Using generic event for packet ID: {}", packetId);
                    return new GenericPacketEvent(client, message, packetId);
            }
            
            // Reset message pointer so handlers can read from it normally
            message.resetPointer();
            
            return event;
            
        } catch (Exception e) {
            logger.error("Error creating event for packet ID {}: {}", packetId, e.getMessage(), e);
            return null;
        }
    }
}
