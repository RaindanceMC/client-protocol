package de.raindancer.client.protocol;

/** Takes the panel away. */
public record HudPanelClear() implements ClientboundPacket {

    @Override
    public ClientFeature feature() {
        return ClientFeature.HUD;
    }
}
