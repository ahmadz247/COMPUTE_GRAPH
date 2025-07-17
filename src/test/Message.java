package test;

import java.util.Date;
import java.nio.charset.StandardCharsets;

public final class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    public Message(byte[] data) {
        this(data, new String(data, StandardCharsets.UTF_8));
    }

    public Message(String text) {
        this(text.getBytes(StandardCharsets.UTF_8), text);
    }

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
