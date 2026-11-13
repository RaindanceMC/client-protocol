package de.raindancer.client.protocol;

/**
 * A run of text in one style.
 *
 * @param argb  its colour, or 0 for whatever the place it is drawn in uses
 * @param style {@link #BOLD} and friends, or-ed together
 * @param font  a font of the server's resource pack by its key (its icons live in fonts), or empty for the mod's own
 */
public record Span(String text, int argb, int style, String font) {

    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINED = 4;
    public static final int STRIKETHROUGH = 8;
    public static final int OBFUSCATED = 16;

    public boolean has(int flag) {
        return (style & flag) != 0;
    }
}
