package com.uber.server.messages;

import com.uber.server.encoding.base64.Base64Encoding;
import com.uber.server.encoding.server.ServerMessageEncoder;
import com.uber.server.encoding.wire.WireEncoding;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Server-to-client message builder and formatter.
 * Formats messages as: [2 bytes: Base64 message ID][body][1 byte: terminator 0x01]
 */
public class ServerMessage {
    private long messageId;
    private final List<Byte> body;
    private static final Charset DEFAULT_ENCODING = Charset.defaultCharset();
    private ServerMessageEncoder encoder;
    
    public ServerMessage() {
        this.body = new ArrayList<>();
        this.messageId = 0;
    }
    
    public ServerMessage(long messageId) {
        this();
        init(messageId);
    }
    
    public long getId() {
        return messageId;
    }
    
    public String getHeader() {
        byte[] headerBytes = Base64Encoding.encodeUInt32(messageId, 2);
        return new String(headerBytes, DEFAULT_ENCODING);
    }
    
    /**
     * Gets a body byte at the specified index.
     * @param index Index in body
     * @return Byte value
     */
    public byte getBodyByte(int index) {
        if (index >= 0 && index < body.size()) {
            return body.get(index);
        }
        return 0;
    }
    
    public int getLength() {
        return body.size();
    }
    
    public void init(long messageId) {
        this.messageId = messageId;
        this.body.clear();
    }
    
    public void clear() {
        this.body.clear();
    }
    
    public void appendByte(byte b) {
        body.add(b);
    }
    
    public void appendBytes(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        for (byte b : data) {
            body.add(b);
        }
    }
    
    public void appendString(String s, Charset encoding) {
        if (s == null || s.isEmpty()) {
            return;
        }
        appendBytes(s.getBytes(encoding));
    }
    
    public void appendString(String s) {
        appendString(s, DEFAULT_ENCODING);
    }
    
    public void appendStringWithBreak(String s) {
        appendStringWithBreak(s, (byte) 2);
    }
    
    public void appendStringWithBreak(String s, byte breakChar) {
        appendString(s);
        appendByte(breakChar);
    }
    
    public void appendInt32(int i) {
        appendBytes(WireEncoding.encodeInt32(i));
    }
    
    public void appendRawInt32(int i) {
        appendString(String.valueOf(i), Charset.forName("ASCII"));
    }
    
    public void appendUInt(long i) {
        appendInt32((int) i);
    }
    
    public void appendRawUInt(long i) {
        appendRawInt32((int) i);
    }
    
    public void appendBoolean(boolean bool) {
        if (bool) {
            body.add(WireEncoding.POSITIVE);
        } else {
            body.add(WireEncoding.NEGATIVE);
        }
    }
    
    /**
     * Converts the message to byte array format: [2 bytes: Base64 ID][body][1 byte: terminator]
     * Delegates to ServerMessageEncoder.
     * @return Byte array ready to send over TCP
     */
    public byte[] getBytes() {
        if (encoder == null) {
            encoder = new ServerMessageEncoder(this);
        }
        return encoder.encode();
    }
    
    public String toBodyString() {
        byte[] bodyArray = new byte[body.size()];
        for (int i = 0; i < body.size(); i++) {
            bodyArray[i] = body.get(i);
        }
        return new String(bodyArray, DEFAULT_ENCODING);
    }
    
    @Override
    public String toString() {
        return getHeader() + toBodyString();
    }
}
