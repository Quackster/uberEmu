package com.uber.server.game.users.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.MessengerRepository;
import com.uber.server.repository.UserRepository;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages messenger functionality for a user (friends and friend requests).
 */
public class HabboMessenger {
    private static final Logger logger = LoggerFactory.getLogger(HabboMessenger.class);
    
    private final long userId;
    private final Game game;
    private final MessengerRepository messengerRepository;
    private final UserRepository userRepository;
    private final CopyOnWriteArrayList<MessengerBuddy> buddies;
    private final CopyOnWriteArrayList<MessengerRequest> requests;
    private boolean appearOffline;
    
    public HabboMessenger(long userId, Game game, MessengerRepository messengerRepository, UserRepository userRepository) {
        this.userId = userId;
        this.game = game;
        this.messengerRepository = messengerRepository;
        this.userRepository = userRepository;
        this.buddies = new CopyOnWriteArrayList<>();
        this.requests = new CopyOnWriteArrayList<>();
        this.appearOffline = false;
    }
    
    public boolean isAppearOffline() {
        return appearOffline;
    }
    
    public void setAppearOffline(boolean appearOffline) {
        this.appearOffline = appearOffline;
    }
    
    /**
     * Gets the list of buddies.
     * @return Buddy list
     */
    public List<MessengerBuddy> getBuddies() {
        return new ArrayList<>(buddies);
    }
    
    /**
     * Loads buddies from the database.
     */
    public void loadBuddies() {
        buddies.clear();
        
        List<Long> buddyIds = messengerRepository.loadBuddies(userId);
        for (Long buddyId : buddyIds) {
            buddies.add(new MessengerBuddy(buddyId, game, messengerRepository));
        }
    }
    
    /**
     * Loads friend requests from the database.
     */
    public void loadRequests() {
        requests.clear();
        
        List<Map<String, Object>> requestData = messengerRepository.loadRequests(userId);
        for (Map<String, Object> row : requestData) {
            try {
                long requestId = ((Number) row.get("id")).longValue();
                long toId = ((Number) row.get("to_id")).longValue();
                long fromId = ((Number) row.get("from_id")).longValue();
                
                MessengerRequest request = new MessengerRequest(requestId, toId, fromId, game, messengerRepository);
                requests.add(request);
            } catch (Exception e) {
                logger.error("Failed to load request: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Clears all buddies from memory.
     */
    public void clearBuddies() {
        buddies.clear();
    }
    
    /**
     * Clears all requests from memory.
     */
    public void clearRequests() {
        requests.clear();
    }
    
    /**
     * Gets a request by request ID (fromUser ID).
     * @param requestId Request ID (actually fromUser ID)
     * @return MessengerRequest object, or null if not found
     */
    public MessengerRequest getRequest(long requestId) {
        for (MessengerRequest request : requests) {
            if (request.getRequestId() == requestId) {
                return request;
            }
        }
        return null;
    }
    
    /**
     * Gets a request by from user ID.
     * @param fromId From user ID
     * @return MessengerRequest object, or null if not found
     */
    public MessengerRequest getRequestByFromId(long fromId) {
        for (MessengerRequest request : requests) {
            if (request.getFrom() == fromId) {
                return request;
            }
        }
        return null;
    }
    
    /**
     * Notifies all buddies that this user's status has changed.
     * @param instantUpdate If true, forces immediate update
     */
    public void onStatusChanged(boolean instantUpdate) {
        for (MessengerBuddy buddy : buddies) {
            GameClient client = game.getClientManager().getClientByHabbo(buddy.getId());
            
            if (client == null || client.getHabbo() == null) {
                continue;
            }
            
            HabboMessenger buddyMessenger = client.getHabbo().getMessenger();
            if (buddyMessenger == null) {
                continue;
            }
            
            buddyMessenger.setUpdateNeeded(userId);
            
            if (instantUpdate) {
                buddyMessenger.forceUpdate();
            }
        }
    }
    
    /**
     * Marks a buddy as needing an update.
     * @param buddyUserId Buddy user ID
     * @return True if buddy was found and marked
     */
    public boolean setUpdateNeeded(long buddyUserId) {
        for (MessengerBuddy buddy : buddies) {
            if (buddy.getId() == buddyUserId) {
                buddy.setUpdateNeeded(true);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Forces an immediate update to the client.
     */
    public void forceUpdate() {
        GameClient client = getClient();
        if (client != null) {
            client.sendMessage(serializeUpdates());
        }
    }
    
    /**
     * Checks if a request exists between two users.
     * @param userOne First user ID
     * @param userTwo Second user ID
     * @return True if request exists
     */
    public boolean requestExists(long userOne, long userTwo) {
        if (userOne == userTwo) {
            return true;
        }
        
        return messengerRepository.requestExists(userOne, userTwo) ||
               messengerRepository.requestExists(userTwo, userOne);
    }
    
    /**
     * Checks if a friendship exists between two users.
     * @param userOne First user ID
     * @param userTwo Second user ID
     * @return True if friendship exists
     */
    public boolean friendshipExists(long userOne, long userTwo) {
        return messengerRepository.friendshipExists(userOne, userTwo);
    }
    
    /**
     * Handles (deletes) all requests.
     */
    public void handleAllRequests() {
        messengerRepository.deleteAllRequests(userId);
        clearRequests();
    }
    
    /**
     * Handles (deletes) a specific request.
     * @param fromId From user ID
     */
    public void handleRequest(long fromId) {
        messengerRepository.deleteRequest(userId, fromId);
        
        MessengerRequest request = getRequestByFromId(fromId);
        if (request != null) {
            requests.remove(request);
        }
    }
    
    /**
     * Creates a friendship between two users.
     * @param userTwo Other user ID
     */
    public void createFriendship(long userTwo) {
        if (!messengerRepository.createFriendship(userId, userTwo)) {
            logger.error("Failed to create friendship between {} and {} in database", userId, userTwo);
            return;
        }
        
        onNewFriendship(userTwo);
        
        // Notify the other user
        GameClient otherUser = game.getClientManager().getClientByHabbo(userTwo);
        if (otherUser != null && otherUser.getHabbo() != null) {
            HabboMessenger otherMessenger = otherUser.getHabbo().getMessenger();
            if (otherMessenger != null) {
                otherMessenger.onNewFriendship(userId);
            }
        }
    }
    
    /**
     * Destroys a friendship between two users.
     * @param userTwo Other user ID
     */
    public void destroyFriendship(long userTwo) {
        if (!messengerRepository.deleteFriendship(userId, userTwo)) {
            logger.error("Failed to delete friendship between {} and {} in database", userId, userTwo);
            return;
        }
        
        onDestroyFriendship(userTwo);
        
        // Notify the other user
        GameClient otherUser = game.getClientManager().getClientByHabbo(userTwo);
        if (otherUser != null && otherUser.getHabbo() != null) {
            HabboMessenger otherMessenger = otherUser.getHabbo().getMessenger();
            if (otherMessenger != null) {
                otherMessenger.onDestroyFriendship(userId);
            }
        }
    }
    
    /**
     * Handles a new friendship being created.
     * @param friendId Friend user ID
     */
    public void onNewFriendship(long friendId) {
        MessengerBuddy buddy = new MessengerBuddy(friendId, game, messengerRepository);
        buddy.setUpdateNeeded(true);
        buddies.add(buddy);
        
        forceUpdate();
    }
    
    /**
     * Handles a friendship being destroyed.
     * @param friendId Friend user ID
     */
    public void onDestroyFriendship(long friendId) {
        buddies.removeIf(buddy -> buddy.getId() == friendId);
        
        // Send update to client
        GameClient client = getClient();
        if (client != null) {
            var updateComposer = new com.uber.server.messages.outgoing.messenger.FriendListUpdateComposer(0, 1, friendId);
            ServerMessage response = updateComposer.compose();
            client.sendMessage(response);
        }
    }
    
    /**
     * Requests a buddy (sends a friend request).
     * @param userQuery Username to search for
     */
    public void requestBuddy(String userQuery) {
        if (userQuery == null || userQuery.isEmpty()) {
            return;
        }
        
        // Get user info
        Map<String, Object> userData = messengerRepository.getUserByUsername(userQuery.toLowerCase());
        if (userData == null) {
            return;
        }
        
        // Check if user blocks new friends
        String blockNewFriends = (String) userData.get("block_newfriends");
        if ("1".equals(blockNewFriends)) {
            GameClient client = getClient();
            if (client != null) {
                ServerMessage response = new ServerMessage(260);
                response.appendInt32(39);
                response.appendInt32(3);
                client.sendMessage(response);
            }
            return;
        }
        
        long toId = ((Number) userData.get("id")).longValue();
        
        if (requestExists(userId, toId)) {
            return;
        }
        
        // Create request
        long requestId = messengerRepository.createRequest(toId, userId);
        if (requestId == 0) {
            logger.error("Failed to create friend request from {} to {} in database", userId, toId);
            return;
        }
        
        // Notify recipient
        GameClient toUser = game.getClientManager().getClientByHabbo(toId);
        if (toUser != null && toUser.getHabbo() != null) {
            HabboMessenger toMessenger = toUser.getHabbo().getMessenger();
            if (toMessenger != null) {
                MessengerRequest request = new MessengerRequest(requestId, toId, userId, game, messengerRepository);
                toMessenger.onNewRequest(requestId, toId, userId);
                
                ServerMessage notification = new ServerMessage(132);
                request.serialize(notification);
                toUser.sendMessage(notification);
            }
        }
    }
    
    /**
     * Handles a new friend request being received.
     * @param requestId Request ID
     * @param toId To user ID
     * @param fromId From user ID
     */
    public void onNewRequest(long requestId, long toId, long fromId) {
        MessengerRequest request = new MessengerRequest(requestId, toId, fromId, game, messengerRepository);
        requests.add(request);
    }
    
    /**
     * Sends an instant message to a friend.
     * @param toId Recipient user ID
     * @param message Message text
     */
    public void sendInstantMessage(long toId, String message) {
        if (!friendshipExists(toId, userId)) {
            deliverInstantMessageError(6, toId); // Not your friend anymore
            return;
        }
        
        GameClient recipient = game.getClientManager().getClientByHabbo(toId);
        if (recipient == null || recipient.getHabbo() == null) {
            deliverInstantMessageError(5, toId); // Friend not online
            return;
        }
        
        HabboMessenger recipientMessenger = recipient.getHabbo().getMessenger();
        if (recipientMessenger == null) {
            deliverInstantMessageError(5, toId); // Friend not online
            return;
        }
        
        GameClient sender = getClient();
        if (sender == null || sender.getHabbo() == null) {
            return;
        }
        
        // Check if sender is muted
        if (sender.getHabbo().isMuted()) {
            deliverInstantMessageError(4, toId); // You are muted
            return;
        }
        
        // Check if recipient is muted (warning only)
        if (recipient.getHabbo().isMuted()) {
            deliverInstantMessageError(3, toId); // Friend is muted and cannot reply
        }
        
        // Check if busy (in room and not appearing offline)
        if (isBusy(recipient)) {
            deliverInstantMessageError(7, toId); // Your friend is busy
            return;
        }
        
        // Filter message
        message = StringUtil.filterInjectionChars(message, true);
        recipientMessenger.deliverInstantMessage(message, userId);
    }
    
    /**
     * Delivers an instant message to this user.
     * @param message Message text
     * @param conversationId Sender user ID
     */
    public void deliverInstantMessage(String message, long conversationId) {
        GameClient client = getClient();
        if (client == null) {
            return;
        }
        
        ServerMessage instantMessage = new ServerMessage(134);
        instantMessage.appendUInt(conversationId);
        instantMessage.appendString(message);
        client.sendMessage(instantMessage);
    }
    
    /**
     * Delivers an instant message error to the client.
     * Error codes:
     * 3 = Your friend is muted and cannot reply
     * 4 = Your message was not sent because you are muted
     * 5 = Your friend is not online
     * 6 = Receiver is not your friend anymore
     * 7 = Your friend is busy
     * @param errorId Error code
     * @param conversationId User ID
     */
    public void deliverInstantMessageError(int errorId, long conversationId) {
        GameClient client = getClient();
        if (client == null) {
            return;
        }
        
        ServerMessage error = new ServerMessage(261);
        error.appendInt32(errorId);
        error.appendUInt(conversationId);
        client.sendMessage(error);
    }
    
    /**
     * Serializes friends list to a ServerMessage.
     * @return ServerMessage with friends list (ID 12)
     */
    public ServerMessage serializeFriends() {
        ServerMessage friends = new ServerMessage(12);
        friends.appendInt32(600); // Max friends
        friends.appendInt32(200); // Max groups
        friends.appendInt32(600); // Max friends (again)
        friends.appendInt32(900); // Max rooms
        friends.appendBoolean(false);
        friends.appendInt32(buddies.size());
        
        for (MessengerBuddy buddy : buddies) {
            buddy.serialize(friends, false);
        }
        
        return friends;
    }
    
    /**
     * Serializes friend updates to a ServerMessage.
     * @return ServerMessage with friend updates (ID 13)
     */
    public ServerMessage serializeUpdates() {
        List<MessengerBuddy> updateBuddies = new ArrayList<>();
        int updateCount = 0;
        
        for (MessengerBuddy buddy : buddies) {
            if (buddy.isUpdateNeeded()) {
                updateCount++;
                updateBuddies.add(buddy);
                buddy.setUpdateNeeded(false);
            }
        }
        
        ServerMessage updates = new ServerMessage(13); // _events[13] = FriendListUpdateEvent
        updates.appendInt32(0);
        updates.appendInt32(updateCount);
        updates.appendInt32(0);
        
        for (MessengerBuddy buddy : updateBuddies) {
            buddy.serialize(updates, false);
            updates.appendBoolean(false);
        }
        
        // Return wrapped in composer for consistency
        var composer = new com.uber.server.messages.outgoing.messenger.FriendListUpdateComposer(updates);
        return composer.compose();
    }
    
    /**
     * Serializes friend requests to a ServerMessage.
     * @return ServerMessage with friend requests (ID 314)
     */
    public ServerMessage serializeRequests() {
        ServerMessage reqs = new ServerMessage(314);
        reqs.appendInt32(requests.size());
        reqs.appendInt32(requests.size());
        
        for (MessengerRequest request : requests) {
            request.serialize(reqs);
        }
        
        return reqs;
    }
    
    /**
     * Performs a search for users.
     * @param searchQuery Search query (username pattern)
     * @return ServerMessage with search results (ID 435)
     */
    public ServerMessage performSearch(String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            searchQuery = "";
        }
        
        // Filter and prepare query
        searchQuery = StringUtil.filterInjectionChars(searchQuery.toLowerCase()).trim();
        if (searchQuery.length() < 1) {
            searchQuery = "";
        }
        
        List<Long> resultIds = messengerRepository.searchUsers(searchQuery);
        
        List<Long> friendIds = new ArrayList<>();
        List<Long> otherIds = new ArrayList<>();
        
        for (Long id : resultIds) {
            if (friendshipExists(userId, id)) {
                friendIds.add(id);
            } else {
                otherIds.add(id);
            }
        }
        
        ServerMessage search = new ServerMessage(435);
        
        search.appendInt32(friendIds.size());
        for (Long id : friendIds) {
            MessengerBuddy buddy = new MessengerBuddy(id, game, messengerRepository);
            buddy.serialize(search, true);
        }
        
        search.appendInt32(otherIds.size());
        for (Long id : otherIds) {
            MessengerBuddy buddy = new MessengerBuddy(id, game, messengerRepository);
            buddy.serialize(search, true);
        }
        
        return search;
    }
    
    /**
     * Checks if a recipient is busy (in room and not appearing offline).
     * @param recipient GameClient to check
     * @return True if recipient is busy
     */
    private boolean isBusy(GameClient recipient) {
        if (recipient == null || recipient.getHabbo() == null) {
            return false;
        }
        
        HabboMessenger recipientMessenger = recipient.getHabbo().getMessenger();
        if (recipientMessenger == null) {
            return false;
        }
        
        // User is busy if they're in a room and not appearing offline
        return recipient.getHabbo().getCurrentRoomId() > 0 && !recipientMessenger.isAppearOffline();
    }
    
    /**
     * Gets the GameClient for this user.
     * @return GameClient object, or null if user is not online
     */
    private GameClient getClient() {
        if (game == null || game.getClientManager() == null) {
            return null;
        }
        return game.getClientManager().getClientByHabbo(userId);
    }
}
