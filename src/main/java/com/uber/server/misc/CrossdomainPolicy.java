package com.uber.server.misc;

/**
 * Generates cross-domain policy XML for Flash clients.
 */
public class CrossdomainPolicy {
    /**
     * Gets the XML cross-domain policy string.
     * @return XML policy string
     */
    public static String getXmlPolicy() {
        return """
            <?xml version="1.0"?>\r
            <!DOCTYPE cross-domain-policy SYSTEM "/xml/dtds/cross-domain-policy.dtd">\r
            <cross-domain-policy>\r
            <allow-access-from domain="*" to-ports="1-31111" />\r
            </cross-domain-policy>\0""";
    }
}
