package com.uber.server.util;

/**
 * String utility functions for filtering and sanitizing user input.
 */
public class StringUtil {
    /**
     * Filters injection characters from input string.
     * Replaces control characters (1, 2, 9) with spaces.
     * @param input The input string to filter
     * @return The filtered string
     */
    public static String filterInjectionChars(String input) {
        return filterInjectionChars(input, false);
    }
    
    /**
     * Filters injection characters from input string.
     * Replaces control characters (1, 2, 9, optionally 13) with spaces.
     * @param input The input string to filter
     * @param allowLinebreaks If true, allows character 13 (carriage return), otherwise replaces it with space
     * @return The filtered string
     */
    public static String filterInjectionChars(String input, boolean allowLinebreaks) {
        if (input == null) {
            return null;
        }
        
        input = input.replace((char) 1, ' ');
        input = input.replace((char) 2, ' ');
        // Character 3 (ETX) is not replaced (allowed in protocol)
        input = input.replace((char) 9, ' '); // Tab character
        
        if (!allowLinebreaks) {
            input = input.replace((char) 13, ' '); // Carriage return
        }
        
        return input;
    }
    
    /**
     * Checks if a string contains only alphanumeric characters.
     * @param inputStr The string to validate
     * @return True if string is alphanumeric only
     */
    public static boolean isValidAlphaNumeric(String inputStr) {
        if (inputStr == null || inputStr.isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < inputStr.length(); i++) {
            char c = inputStr.charAt(i);
            if (!Character.isLetter(c) && !Character.isDigit(c)) {
                return false;
            }
        }
        
        return true;
    }
}
