package graph;

import java.util.Date;
import java.nio.charset.StandardCharsets;

/**
 * Immutable message class for data transfer between topics and agents.
 * Provides automatic conversion between byte[], String, and double formats.
 * 
 * @author Advanced Programming Course
 */
public final class Message {
    /** Raw message data as bytes */
    public final byte[] data;
    /** Text representation of the message */
    public final String asText;
    /** Numeric representation (NaN if not convertible) */
    public final double asDouble;
    /** Timestamp when message was created */
    public final Date date;

    /**
     * Creates a message from byte array data.
     * @param data The raw bytes of the message
     */
    public Message(byte[] data) {
        this(data, new String(data, StandardCharsets.UTF_8));
    }

    /**
     * Creates a message from a text string.
     * @param text The text content of the message
     */
    public Message(String text) {
        this(text.getBytes(StandardCharsets.UTF_8), text);
    }

    /**
     * Creates a message from a numeric value.
     * @param value The numeric value of the message
     */
    public Message(double value) {
        this(Double.toString(value).getBytes(StandardCharsets.UTF_8), Double.toString(value));
    }

    private Message(byte[] data, String asText) {
        this.data = data;
        this.asText = asText;
        double d;
        try {
            d = Double.parseDouble(asText);
        } catch (Exception e) {
            d = Double.NaN;
        }
        this.asDouble = d;
        this.date = new Date();
    }
}
