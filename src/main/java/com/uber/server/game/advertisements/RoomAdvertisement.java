package com.uber.server.game.advertisements;

import com.uber.server.repository.AdvertisementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a room advertisement.
 */
public class RoomAdvertisement {
    private static final Logger logger = LoggerFactory.getLogger(RoomAdvertisement.class);
    
    private final long id;
    private final String adImage;
    private final String adLink;
    private int views;
    private final int viewsLimit;
    private final AdvertisementRepository repository;
    
    public RoomAdvertisement(long id, String adImage, String adLink, int views, int viewsLimit, 
                            AdvertisementRepository repository) {
        this.id = id;
        this.adImage = adImage != null ? adImage : "";
        this.adLink = adLink != null ? adLink : "";
        this.views = views;
        this.viewsLimit = viewsLimit;
        this.repository = repository;
    }
    
    /**
     * Checks if the advertisement has exceeded its view limit.
     * @return True if exceeded limit
     */
    public boolean isExceededLimit() {
        if (viewsLimit <= 0) {
            return false;
        }
        return views >= viewsLimit;
    }
    
    /**
     * Called when the advertisement is viewed.
     * Increments view count and updates database.
     */
    public void onView() {
        this.views++;
        
        if (repository != null) {
            repository.incrementViews(id);
        }
    }
    
    public long getId() { return id; }
    public String getAdImage() { return adImage; }
    public String getAdLink() { return adLink; }
    public int getViews() { return views; }
    public int getViewsLimit() { return viewsLimit; }
}
