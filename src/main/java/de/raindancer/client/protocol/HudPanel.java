package de.raindancer.client.protocol;

import java.util.List;

/** What a sidebar would have shown: a title and its lines. Replaces whatever panel was there. */
public record HudPanel(Text title, List<Text> lines) implements ClientboundPacket {

    public static final int MAX_LINES = 32;

    public HudPanel {
        lines = List.copyOf(lines);
    }

    @Override
    public ClientFeature feature() {
        return ClientFeature.HUD;
    }
}
