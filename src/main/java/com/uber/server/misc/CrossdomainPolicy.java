package com.uber.server.misc;

/**
 * Generates cross-domain policy XML for Flash clients.
 * Ported from HabboHotel/Misc/CrossdomainPolicy.cs
 */
public class CrossdomainPolicy {
    /**
     * Gets the XML cross-domain policy string.
     * @return XML policy string
     */
    public static String getXmlPolicy() {
        return "<?xml version=\"1.0\"?>\r\n" +
               "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">\r\n" +
               "<cross-domain-policy>\r\n" +
               "<allow-access-from domain=\"*\" to-ports=\"1-31111\" />\r\n" +
               "</cross-domain-policy>\u0000";
    }
}
