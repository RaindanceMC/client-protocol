package de.raindancer.client.protocol;

/**
 * A server's colours, as ARGB, and the sprite of its mark. Cold for what a thing is ({@code harbor},
 * {@code sky}), warm for where to look ({@code gold}, {@code ember}).
 */
public record Theme(String name, int night, int navy, int harbor, int sky, int gold, int ember, int text, int muted,
                    String logo) {

    /** hyadesmc.net's palette, and what the mod wears until a server says otherwise. */
    public static final Theme HARBOR_SUNSET = new Theme("Harbor Sunset",
            0xFF070A1C, 0xFF0F1E3D, 0xFF1F4E8C, 0xFF6EC1EA, 0xFFF6B83D, 0xFFF2843A, 0xFFEEF3FA, 0xFFA9B9CF,
            "hyades:logo");
}
