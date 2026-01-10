package com.uber.server.game.advertisements;

import com.uber.server.repository.AdvertisementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages room advertisements.
 */
public class AdvertisementManager {
    private static final Logger logger = LoggerFactory.getLogger(AdvertisementManager.class);
    
    private final AdvertisementRepository repository;
    private final ConcurrentHashMap<Long, RoomAdvertisement> roomAdvertisements;
    private final Random random;
    
    public AdvertisementManager(AdvertisementRepository repository) {
        this.repository = repository;
        this.roomAdvertisements = new ConcurrentHashMap<>();
        this.random = new Random();
    }
    
    /**
     * Loads room advertisements from database.
     */
    public void loadRoomAdvertisements() {
        roomAdvertisements.clear();
        
        List<Map<String, Object>> adData = repository.loadRoomAdvertisements();
        for (Map<String, Object> row : adData) {
            try {
                long id = ((Number) row.get("id")).longValue();
                String adImage = (String) row.get("ad_image");
                String adLink = (String) row.get("ad_link");
                int views = ((Number) row.get("views")).intValue();
                int viewsLimit = ((Number) row.get("views_limit")).intValue();
                
                RoomAdvertisement ad = new RoomAdvertisement(id, adImage, adLink, views, viewsLimit, repository);
                roomAdvertisements.put(id, ad);
            } catch (Exception e) {
                logger.error("Failed to load advertisement from row: {}", e.getMessage(), e);
            }
        }
        
        logger.info("Loaded {} room advertisement(s)", roomAdvertisements.size());
    }
    
    /**
     * Gets a random room advertisement that hasn't exceeded its limit.
     * @return RoomAdvertisement, or null if none available
     */
    public RoomAdvertisement getRandomRoomAdvertisement() {
        if (roomAdvertisements.isEmpty()) {
            return null;
        }
        
        // Filter advertisements that haven't exceeded limit
        List<RoomAdvertisement> availableAds = new ArrayList<>();
        for (RoomAdvertisement ad : roomAdvertisements.values()) {
            if (!ad.isExceededLimit()) {
                availableAds.add(ad);
            }
        }
        
        if (availableAds.isEmpty()) {
            return null;
        }
        
        // Return random advertisement
        return availableAds.get(random.nextInt(availableAds.size()));
    }
    
    /**
     * Gets all room advertisements.
     * @return Map of all advertisements (ID -> RoomAdvertisement)
     */
    public Map<Long, RoomAdvertisement> getAllAdvertisements() {
        return new ConcurrentHashMap<>(roomAdvertisements);
    }
}
