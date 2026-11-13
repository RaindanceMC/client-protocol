package de.raindancer.client.protocol;

/**
 * The plugin channel and its limits.
 *
 * <p>A plugin message on {@link #CHANNEL} is one frame: {@code varint feature | varint type | body}. A side
 * that does not know a feature or a type drops the frame; a side that knows more than the other appends to
 * the end of a body and nowhere else, which is why a reader never minds bytes it did not ask for.
 */
public final class Wire {

    public static final String CHANNEL = "rains:client";

    /** The framing and the hello itself. Everything else is versioned per feature. */
    public static final int PROTOCOL = 1;

    /** What the game carries in one custom payload towards a client, and towards a server. */
    public static final int MAX_CLIENTBOUND = 1_048_576;
    public static final int MAX_SERVERBOUND = 32_767;

    private Wire() {
    }
}
