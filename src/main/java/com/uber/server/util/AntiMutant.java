package com.uber.server.util;

/**
 * Validates user appearance strings to prevent mutant avatars.
 */
public class AntiMutant {
    /**
     * Validates a look string and gender.
     * @param look The appearance string (e.g., "hd-190-1.hr-100-61.ch-255-70.lg-280-64")
     * @param gender The gender (M or F)
     * @return true if the look is valid, false otherwise
     */
    public static boolean validateLook(String look, String gender) {
        boolean hasHead = false;
        
        if (look == null || look.length() < 1) {
            return false;
        }
        
        try {
            String[] sets = look.split("\\.");
            
            if (sets.length < 4) {
                return false;
            }
            
            for (String set : sets) {
                String[] parts = set.split("-");
                
                if (parts.length < 3) {
                    return false;
                }
                
                String name = parts[0];
                int type = Integer.parseInt(parts[1]);
                // Parse color value from parts[2]
                int color = Integer.parseInt(parts[2]);
                
                if (type <= 0 || color < 0) {
                    return false;
                }
                
                if (name.length() != 2) {
                    return false;
                }
                
                if ("hd".equals(name)) {
                    hasHead = true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        
        if (!hasHead || (!"M".equals(gender) && !"F".equals(gender))) {
            return false;
        }
        
        return true;
    }
}
