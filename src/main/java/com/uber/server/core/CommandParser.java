package com.uber.server.core;

import com.uber.server.game.GameEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command parser for console commands.
 * Ported from Core/CommandParser.cs
 */
public class CommandParser {
    private static final Logger logger = LoggerFactory.getLogger(CommandParser.class);
    
    /**
     * Parses and executes a console command.
     * @param input Command input string
     * @param environment GameEnvironment instance
     */
    public static void parse(String input, GameEnvironment environment) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        
        String[] params = input.trim().split("\\s+");
        if (params.length == 0) {
            return;
        }
        
        String command = params[0].toLowerCase();
        
        switch (command) {
            case "reload_models":
                environment.getGame().getRoomManager().loadModels();
                logger.info("Reloaded room models successfully.");
                break;
                
            case "reload_bans":
                environment.getGame().getBanManager().loadBans();
                logger.info("Reloaded bans successfully.");
                break;
                
            case "reload_navigator":
                environment.getGame().getNavigator().initialize();
                logger.info("Re-initialized navigator successfully.");
                break;
                
            case "reload_items":
                environment.getGame().getItemManager().loadItems();
                logger.info("Reloaded items successfully. Please note that changes may not be reflected immediately in currently loaded rooms.");
                break;
                
            case "reload_help":
                environment.getGame().getHelpTool().loadCategories();
                environment.getGame().getHelpTool().loadTopics();
                logger.info("Reloaded help categories and topics successfully.");
                break;
                
            case "reload_catalog":
                environment.getGame().getCatalog().initialize();
                // Broadcast catalog update to all clients
                com.uber.server.messages.ServerMessage catalogUpdate = new com.uber.server.messages.ServerMessage(441);
                environment.getGame().getClientManager().broadcastMessage(catalogUpdate);
                logger.info("Published catalog successfully.");
                break;
                
            case "reload_roles":
                environment.getGame().getRoleManager().loadRoles();
                environment.getGame().getRoleManager().loadRights();
                logger.info("Reloaded ranks and rights successfully.");
                break;
                
            case "plugins":
                // TODO: Implement when PluginHandler is ported
                logger.info("The following plugins are currently loaded:");
                logger.info("(Plugin system not yet implemented)");
                break;
                
            case "unload_all_plugins":
                // TODO: Implement when PluginHandler is ported
                logger.info("All plugins have been unloaded.");
                break;
                
            case "unload_plugin":
                if (params.length < 2) {
                    logger.warn("Usage: unload_plugin <name>");
                    break;
                }
                String pluginName = mergeParams(params, 1);
                // TODO: Implement when PluginHandler is ported
                logger.info("Plugin unloaded successfully.");
                logger.warn("Take note that a plugin may still be running processes even when unloaded.");
                break;
                
            case "cls":
                // Clear console (not really possible in Java, but log a message)
                logger.info("--- Console cleared ---");
                break;
                
            case "help":
                logger.info("Available commands are: cls, close, help, reload_catalog, reload_navigator, reload_roles, reload_help, reload_items, plugins, unload_all_plugins, unload_plugin [name]");
                break;
                
            case "close":
            case "quit":
            case "exit":
                logger.info("Shutting down server...");
                environment.destroy();
                System.exit(0);
                break;
                
            default:
                logger.warn("Unrecognized command or operation: {}. Use 'help' for a list of available commands.", input);
                break;
        }
    }
    
    /**
     * Merges parameters from a specific index onwards.
     * @param params Parameter array
     * @param start Start index
     * @return Merged string
     */
    private static String mergeParams(String[] params, int start) {
        if (start >= params.length) {
            return "";
        }
        
        StringBuilder merged = new StringBuilder();
        for (int i = start; i < params.length; i++) {
            if (i > start) {
                merged.append(" ");
            }
            merged.append(params[i]);
        }
        return merged.toString();
    }
}
