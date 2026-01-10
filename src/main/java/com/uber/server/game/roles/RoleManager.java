package com.uber.server.game.roles;

import com.uber.server.game.Habbo;
import com.uber.server.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user roles and permissions (fuses).
 */
public class RoleManager {
    private static final Logger logger = LoggerFactory.getLogger(RoleManager.class);
    
    private final ConcurrentHashMap<Long, Role> roles;
    private final ConcurrentHashMap<String, Long> rights; // fuse -> min rank
    private final ConcurrentHashMap<String, String> subRights; // fuse -> subscription id
    private final RoleRepository roleRepository;
    
    public RoleManager(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        this.roles = new ConcurrentHashMap<>();
        this.rights = new ConcurrentHashMap<>();
        this.subRights = new ConcurrentHashMap<>();
    }
    
    /**
     * Loads all roles from the database.
     */
    public void loadRoles() {
        clearRoles();
        
        List<Map<String, Object>> roleData = roleRepository.loadRoles();
        for (Map<String, Object> row : roleData) {
            long id = ((Number) row.get("id")).longValue();
            String name = (String) row.get("name");
            roles.put(id, new Role(id, name));
        }
        
        logger.info("Loaded {} roles", roles.size());
    }
    
    /**
     * Loads all rights from the database.
     */
    public void loadRights() {
        clearRights();
        
        Map<String, Long> rightsData = roleRepository.loadRights();
        rights.putAll(rightsData);
        
        Map<String, String> subRightsData = roleRepository.loadSubRights();
        subRights.putAll(subRightsData);
        
        logger.info("Loaded {} rights and {} subscription rights", rights.size(), subRights.size());
    }
    
    /**
     * Checks if a rank has a specific right (fuse).
     * @param rankId The rank ID to check
     * @param fuse The fuse/permission name
     * @return True if the rank has the right
     */
    public boolean rankHasRight(long rankId, String fuse) {
        if (fuse == null) {
            return false;
        }
        
        String fuseLower = fuse.toLowerCase();
        if (!rights.containsKey(fuseLower)) {
            return false;
        }
        
        long minRank = rights.get(fuseLower);
        return rankId >= minRank;
    }
    
    /**
     * Checks if a subscription has a specific right (fuse).
     * @param subId The subscription ID
     * @param fuse The fuse/permission name
     * @return True if the subscription has the right
     */
    public boolean subHasRight(String subId, String fuse) {
        if (subId == null || fuse == null) {
            return false;
        }
        
        String fuseLower = fuse.toLowerCase();
        if (subRights.containsKey(fuseLower)) {
            return subId.equals(subRights.get(fuseLower));
        }
        
        return false;
    }
    
    /**
     * Gets a role by ID.
     * @param id Role ID
     * @return Role object, or null if not found
     */
    public Role getRole(long id) {
        return roles.get(id);
    }
    
    /**
     * Gets all rights for a Habbo (rank + subscriptions).
     * @param habbo The Habbo user
     * @return List of fuse names
     */
    public List<String> getRightsForHabbo(Habbo habbo) {
        List<String> userRights = new ArrayList<>();
        
        // Add rights from rank
        userRights.addAll(getRightsForRank(habbo.getRank()));
        
        // Add rights from subscriptions
        if (habbo.getSubscriptionManager() != null) {
            List<String> subList = habbo.getSubscriptionManager().getSubList();
            for (String subscriptionId : subList) {
                userRights.addAll(getRightsForSub(subscriptionId));
            }
        }
        
        return userRights;
    }
    
    /**
     * Gets all rights for a rank.
     * @param rankId The rank ID
     * @return List of fuse names
     */
    public List<String> getRightsForRank(long rankId) {
        List<String> userRights = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : rights.entrySet()) {
            if (rankId >= entry.getValue() && !userRights.contains(entry.getKey())) {
                userRights.add(entry.getKey());
            }
        }
        
        return userRights;
    }
    
    /**
     * Gets all rights for a subscription.
     * @param subId The subscription ID
     * @return List of fuse names
     */
    public List<String> getRightsForSub(String subId) {
        List<String> userRights = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : subRights.entrySet()) {
            if (subId.equals(entry.getValue())) {
                userRights.add(entry.getKey());
            }
        }
        
        return userRights;
    }
    
    /**
     * Checks if a role exists.
     * @param id Role ID
     * @return True if role exists
     */
    public boolean containsRole(long id) {
        return roles.containsKey(id);
    }
    
    /**
     * Checks if a right exists.
     * @param right Fuse name
     * @return True if right exists
     */
    public boolean containsRight(String right) {
        if (right == null) {
            return false;
        }
        return rights.containsKey(right.toLowerCase());
    }
    
    /**
     * Clears all roles.
     */
    public void clearRoles() {
        roles.clear();
    }
    
    /**
     * Clears all rights.
     */
    public void clearRights() {
        rights.clear();
        subRights.clear();
    }
}
