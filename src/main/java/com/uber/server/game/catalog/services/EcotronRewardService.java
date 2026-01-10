package com.uber.server.game.catalog.services;

import com.uber.server.game.catalog.EcotronReward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for managing Ecotron rewards.
 * Extracted from Catalog.
 */
public class EcotronRewardService {
    private static final Logger logger = LoggerFactory.getLogger(EcotronRewardService.class);
    
    private final Random random;
    
    public EcotronRewardService() {
        this.random = new Random();
    }
    
    /**
     * Gets ecotron rewards for a specific level.
     * @param level Reward level
     * @param ecotronRewards List of all ecotron rewards
     * @return List of rewards for that level
     */
    public List<EcotronReward> getEcotronRewardsForLevel(long level, List<EcotronReward> ecotronRewards) {
        if (ecotronRewards == null) {
            return new ArrayList<>();
        }
        
        List<EcotronReward> rewards = new ArrayList<>();
        for (EcotronReward reward : ecotronRewards) {
            if (reward.getRewardLevel() == level) {
                rewards.add(reward);
            }
        }
        return rewards;
    }
    
    /**
     * Gets a random ecotron reward based on probability.
     * @param ecotronRewards List of all ecotron rewards
     * @return Random EcotronReward
     */
    public EcotronReward getRandomEcotronReward(List<EcotronReward> ecotronRewards) {
        if (ecotronRewards == null || ecotronRewards.isEmpty()) {
            return null;
        }
        
        long level = 1;
        
        // Probability-based level selection
        int rand = random.nextInt(2000) + 1;
        if (rand == 2000) {
            level = 5;
        } else {
            rand = random.nextInt(200) + 1;
            if (rand == 200) {
                level = 4;
            } else {
                rand = random.nextInt(40) + 1;
                if (rand == 40) {
                    level = 3;
                } else {
                    rand = random.nextInt(4) + 1;
                    if (rand == 4) {
                        level = 2;
                    }
                }
            }
        }
        
        List<EcotronReward> levelRewards = getEcotronRewardsForLevel(level, ecotronRewards);
        if (levelRewards.isEmpty()) {
            // Fallback to level 1 if no rewards for selected level
            levelRewards = getEcotronRewardsForLevel(1, ecotronRewards);
        }
        
        if (levelRewards.isEmpty()) {
            return null;
        }
        
        return levelRewards.get(random.nextInt(levelRewards.size()));
    }
}
