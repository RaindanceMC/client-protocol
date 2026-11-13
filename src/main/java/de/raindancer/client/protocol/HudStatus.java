package de.raindancer.client.protocol;

/**
 * What the action bar would have shown. Replaces the line that was there; empty text takes it away.
 *
 * @param staysMillis how long it stays if nothing follows — a server repeats a line it wants kept
 */
public record HudStatus(Text text, int staysMillis) implements ClientboundPacket {

    @Override
    public ClientFeature feature() {
        return ClientFeature.HUD;
    }
}
