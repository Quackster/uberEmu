package com.uber.server.game.handlers.registration;

import com.uber.server.game.Game;
import com.uber.server.messages.PacketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers help and moderation-related packet handlers.
 */
public class HelpHandlerRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(HelpHandlerRegistrar.class);
    
    private final PacketHandlerRegistry registry;
    private final Game game;
    
    public HelpHandlerRegistrar(PacketHandlerRegistry registry, Game game) {
        this.registry = registry;
        this.game = game;
    }
    
    /**
     * Registers all help handlers.
     */
    public void register() {
        // InitHelpTool handler (ID 416)
        registry.register(416, new com.uber.server.messages.incoming.help.GetClientFaqsMessageComposerHandler(game)); // GetClientFaqsMessageComposer (ID 416)
        
        // GetHelpCategories handler (ID 417)
        registry.register(417, new com.uber.server.messages.incoming.help.GetFaqCategoriesMessageComposerHandler(game)); // GetFaqCategoriesMessageComposer (ID 417)
        
        // ViewHelpTopic handler (ID 418)
        registry.register(418, new com.uber.server.messages.incoming.help.GetFaqTextMessageComposerHandler(game)); // GetFaqTextMessageComposer (ID 418)
        
        // SearchHelpTopics handler (ID 419)
        registry.register(419, new com.uber.server.messages.incoming.help.SearchFaqsMessageComposerHandler(game)); // SearchFaqsMessageComposer (ID 419)
        
        // GetTopicsInCategory handler (ID 420)
        registry.register(420, new com.uber.server.messages.incoming.help.GetFaqCategoryMessageComposerHandler(game)); // GetFaqCategoryMessageComposer (ID 420)
        
        // Help ticket handlers
        registry.register(453, new com.uber.server.messages.incoming.help.CallForHelpMessageComposerHandler(game)); // CallForHelpMessageComposer (ID 453)
        registry.register(238, new com.uber.server.messages.incoming.help.DeletePendingCallsForHelpMessageComposerHandler(game)); // DeletePendingCallsForHelpMessageComposer (ID 238)
        
        // CallGuideBot handler (ID 440)
        registry.register(440, new com.uber.server.messages.incoming.help.CallGuideBotMessageComposerHandler(game)); // CallGuideBotMessageComposer (ID 440)
        
        // Moderation handlers (require fuse permissions)
        registry.register(200, new com.uber.server.messages.incoming.help.ModSendRoomAlertMessageComposerHandler(game)); // ModSendRoomAlertMessageComposer (ID 200)
        registry.register(450, new com.uber.server.handlers.help.ModPickTicketHandler(game)); // ModPickTicket
        registry.register(451, new com.uber.server.handlers.help.ModReleaseTicketHandler(game)); // ModReleaseTicket
        registry.register(452, new com.uber.server.handlers.help.ModCloseTicketHandler(game)); // ModCloseTicket
        registry.register(454, new com.uber.server.handlers.help.ModGetUserInfoHandler(game)); // ModGetUserInfo
        registry.register(455, new com.uber.server.handlers.help.ModGetUserChatlogHandler(game)); // ModGetUserChatlog
        registry.register(456, new com.uber.server.handlers.help.ModGetRoomChatlogHandler(game)); // ModGetRoomChatlog
        registry.register(457, new com.uber.server.handlers.help.ModGetTicketChatlogHandler(game)); // ModGetTicketChatlog
        registry.register(458, new com.uber.server.handlers.help.ModGetRoomVisitsHandler(game)); // ModGetRoomVisits
        registry.register(459, new com.uber.server.handlers.help.ModGetRoomToolHandler(game)); // ModGetRoomTool
        registry.register(460, new com.uber.server.handlers.help.ModPerformRoomActionHandler(game)); // ModPerformRoomAction
        registry.register(461, new com.uber.server.handlers.help.ModSendUserCautionHandler(game)); // ModSendUserCaution
        registry.register(462, new com.uber.server.handlers.help.ModSendUserMessageHandler(game)); // ModSendUserMessage
        registry.register(463, new com.uber.server.handlers.help.ModKickUserHandler(game)); // ModKickUser
        registry.register(464, new com.uber.server.handlers.help.ModBanUserHandler(game)); // ModBanUser
        
        logger.debug("Registered help handlers");
    }
}
