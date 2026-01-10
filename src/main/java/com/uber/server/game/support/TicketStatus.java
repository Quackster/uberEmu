package com.uber.server.game.support;

/**
 * Support ticket status enumeration.
 * Ported from HabboHotel/Support/SupportTicket.cs TicketStatus enum
 */
public enum TicketStatus {
    OPEN,
    PICKED,
    RESOLVED,
    ABUSIVE,
    INVALID,
    DELETED
}
