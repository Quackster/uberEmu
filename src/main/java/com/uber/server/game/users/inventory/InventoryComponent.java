package com.uber.server.game.users.inventory;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.InventoryRepository;
import com.uber.server.repository.PetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages user inventory (items and pets).
 */
public class InventoryComponent {
    private static final Logger logger = LoggerFactory.getLogger(InventoryComponent.class);
    
    private final long userId;
    private final Game game;
    private final InventoryRepository inventoryRepository;
    private final PetRepository petRepository;
    private final CopyOnWriteArrayList<UserItem> inventoryItems;
    private final CopyOnWriteArrayList<Map<String, Object>> inventoryPets; // Simplified - full Pet objects can be added later
    
    public InventoryComponent(long userId, Game game, InventoryRepository inventoryRepository, PetRepository petRepository) {
        this.userId = userId;
        this.game = game;
        this.inventoryRepository = inventoryRepository;
        this.petRepository = petRepository;
        this.inventoryItems = new CopyOnWriteArrayList<>();
        this.inventoryPets = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Gets the number of items in inventory.
     * @return Item count
     */
    public int getItemCount() {
        return inventoryItems.size();
    }
    
    /**
     * Gets the number of pets in inventory.
     * @return Pet count
     */
    public int getPetCount() {
        return inventoryPets.size();
    }
    
    /**
     * Loads inventory from the database.
     */
    public void loadInventory() {
        inventoryItems.clear();
        inventoryPets.clear();
        
        // Load items
        List<Map<String, Object>> itemData = inventoryRepository.loadUserItems(userId);
        for (Map<String, Object> row : itemData) {
            try {
                long id = ((Number) row.get("id")).longValue();
                long baseItem = ((Number) row.get("base_item")).longValue();
                String extraData = (String) row.get("extra_data");
                
                UserItem item = new UserItem(id, baseItem, extraData, game.getItemManager());
                inventoryItems.add(item);
            } catch (Exception e) {
                logger.error("Failed to load inventory item: {}", e.getMessage(), e);
            }
        }
        
        // Load pets (simplified - full Pet implementation can be added later)
        List<Map<String, Object>> petData = inventoryRepository.loadUserPets(userId);
        inventoryPets.addAll(petData);
    }
    
    /**
     * Updates items in inventory and notifies client.
     * @param fromDatabase If true, reloads from database first
     */
    public void updateItems(boolean fromDatabase) {
        if (fromDatabase) {
            loadInventory();
        }
        
        GameClient client = getClient();
        if (client != null) {
            var composer = new com.uber.server.messages.outgoing.users.InventoryRefreshMessageEventComposer();
            client.sendMessage(composer.compose());
        }
    }
    
    /**
     * Updates pets in inventory and notifies client.
     * @param fromDatabase If true, reloads from database first
     */
    public void updatePets(boolean fromDatabase) {
        if (fromDatabase) {
            loadInventory();
        }
        
        GameClient client = getClient();
        if (client != null) {
            client.sendMessage(serializePetInventory());
        }
    }
    
    /**
     * Gets a pet by ID as a Map.
     * @param petId Pet ID
     * @return Pet data map, or null if not found
     */
    public Map<String, Object> getPet(long petId) {
        for (Map<String, Object> pet : inventoryPets) {
            if (((Number) pet.get("id")).longValue() == petId) {
                return pet;
            }
        }
        return null;
    }
    
    /**
     * Gets a pet by ID as a Pet object.
     * @param petId Pet ID
     * @return Pet object, or null if not found
     */
    public com.uber.server.game.pets.Pet getPetObject(long petId) {
        Map<String, Object> petData = getPet(petId);
        if (petData == null || game == null || game.getPetRepository() == null) {
            return null;
        }
        return com.uber.server.game.pets.Pet.fromRow(petData, game.getPetRepository(), game);
    }
    
    /**
     * Gets an item by ID.
     * @param itemId Item ID
     * @return UserItem object, or null if not found
     */
    public UserItem getItem(long itemId) {
        for (UserItem item : inventoryItems) {
            if (item.getId() == itemId) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Adds an item to inventory.
     * @param id Item ID
     * @param baseItem Base item ID
     * @param extraData Extra data
     */
    public void addItem(long id, long baseItem, String extraData) {
        UserItem item = new UserItem(id, baseItem, extraData, game.getItemManager());
        inventoryItems.add(item);
        
        // Save to database
        if (!inventoryRepository.createUserItem(id, userId, baseItem, extraData)) {
            logger.error("Failed to add item {} to user {} in database", id, userId);
            inventoryItems.remove(item);
        }
    }
    
    /**
     * Adds a pet to inventory (from Map).
     * @param pet Pet data map
     */
    public void addPet(Map<String, Object> pet) {
        if (pet == null) {
            return;
        }
        
        inventoryPets.add(pet);
        
        // Update pet in database to move it to inventory (room_id = 0)
        long petId = ((Number) pet.get("id")).longValue();
        if (!petRepository.updatePetRoom(petId, 0, 0, 0, 0.0)) {
            logger.error("Failed to update pet {} room in database", petId);
            inventoryPets.remove(pet);
            return;
        }
        
        // Send add message to client
        GameClient client = getClient();
        if (client != null) {
            ServerMessage message = new ServerMessage(603);
            com.uber.server.game.pets.Pet petObj = getPetObject(petId);
            if (petObj != null) {
                petObj.serializeInventory(message);
            } else {
                message.appendUInt(petId);
            }
            var composer = new com.uber.server.messages.outgoing.users.PetAddedToInventoryComposer(message);
            client.sendMessage(composer.compose());
        }
    }
    
    /**
     * Adds a pet to inventory (from Pet object).
     * @param pet Pet object
     */
    public void addPet(com.uber.server.game.pets.Pet pet) {
        if (pet == null) {
            return;
        }
        
        // Convert Pet to Map for storage (or we could store Pet objects directly)
        Map<String, Object> petMap = new java.util.HashMap<>();
        petMap.put("id", pet.getPetId());
        petMap.put("user_id", pet.getOwnerId());
        petMap.put("name", pet.getName());
        petMap.put("type", pet.getType());
        petMap.put("race", pet.getRace());
        petMap.put("color", pet.getColor());
        petMap.put("expirience", pet.getExperience());
        petMap.put("energy", pet.getEnergy());
        petMap.put("nutrition", pet.getNutrition());
        petMap.put("respect", pet.getRespect());
        petMap.put("createstamp", pet.getCreationStamp());
        petMap.put("room_id", pet.getRoomId());
        petMap.put("x", pet.getX());
        petMap.put("y", pet.getY());
        petMap.put("z", pet.getZ());
        
        addPet(petMap);
    }
    
    /**
     * Removes a pet from inventory.
     * @param petId Pet ID
     * @return True if pet was removed
     */
    public boolean removePet(long petId) {
        Map<String, Object> pet = getPet(petId);
        if (pet == null) {
            return false;
        }
        
        inventoryPets.remove(pet);
        
        // Send remove message to client
        GameClient client = getClient();
        if (client != null) {
            var composer = new com.uber.server.messages.outgoing.users.PetRemovedFromInventoryComposer(petId);
            client.sendMessage(composer.compose());
        }
        
        // Note: We don't update the database here - movePetToRoom handles that
        return true;
    }
    
    /**
     * Moves a pet to a room.
     * @param petId Pet ID
     * @param roomId Room ID
     */
    public void movePetToRoom(long petId, long roomId) {
        // Remove pet from inventory
        if (removePet(petId)) {
            // Update pet room in database
            if (!petRepository.updatePetRoom(petId, roomId, 0, 0, 0.0)) {
                logger.error("Failed to move pet {} to room {} in database", petId, roomId);
            }
        }
    }
    
    /**
     * Removes an item from inventory.
     * @param itemId Item ID
     */
    public void removeItem(long itemId) {
        UserItem item = getItem(itemId);
        if (item == null) {
            return;
        }
        
        // Send remove message to client
        GameClient client = getClient();
        if (client != null) {
            var composer = new com.uber.server.messages.outgoing.users.FurniListRemoveComposer(itemId);
            client.sendMessage(composer.compose());
        }
        
        inventoryItems.remove(item);
        
        // Delete from database
        if (inventoryRepository != null) {
            inventoryRepository.deleteItem(userId, itemId);
        }
    }
    
    /**
     * Serializes item inventory to a ServerMessage.
     * @return ServerMessage with inventory data (ID 140)
     */
    public ServerMessage serializeItemInventory() {
        ServerMessage message = new ServerMessage(140);
        message.appendInt32(getItemCount());
        
        for (UserItem item : inventoryItems) {
            item.serialize(message, true);
        }
        
        message.appendInt32(getItemCount());
        return message;
    }
    
    /**
     * Serializes pet inventory to a ServerMessage.
     * @return ServerMessage with pet inventory data (ID 600)
     */
    public ServerMessage serializePetInventory() {
        ServerMessage message = new ServerMessage(600);
        message.appendInt32(inventoryPets.size());
        
        // Serialize each pet
        for (Map<String, Object> pet : inventoryPets) {
            long petId = ((Number) pet.get("id")).longValue();
            com.uber.server.game.pets.Pet petObj = getPetObject(petId);
            if (petObj != null) {
                petObj.serializeInventory(message);
            } else {
                // Fallback: basic serialization
                message.appendUInt(petId);
                message.appendStringWithBreak((String) pet.get("name"));
                message.appendStringWithBreak((String) pet.get("type") + " " + pet.get("race") + " " + pet.get("color"));
                message.appendBoolean(false);
            }
        }
        
        return message;
    }
    
    /**
     * Clears all items from inventory.
     */
    public void clearItems() {
        // Delete all items from database
        if (inventoryRepository != null) {
            inventoryRepository.deleteAllItems(userId);
        }
        inventoryItems.clear();
        updateItems(false);
    }
    
    /**
     * Clears all pets from inventory.
     */
    public void clearPets() {
        // Delete all pets from database
        if (petRepository != null) {
            petRepository.deleteAllPets(userId);
        }
        inventoryPets.clear();
        updatePets(false);
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
