package de.raindancer.client.protocol;

/** Bytes that are not a packet. The only thing a decoder throws, whatever it was handed. */
public final class MalformedPacketException extends RuntimeException {

    public MalformedPacketException(String message) {
        super(message);
    }
}
