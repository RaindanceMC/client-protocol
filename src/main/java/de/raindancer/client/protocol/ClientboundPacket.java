package de.raindancer.client.protocol;

public sealed interface ClientboundPacket permits Welcome, HudPanel, HudPanelClear, HudStatus {

    /** A server sends a packet only to a client it agreed this feature with. */
    ClientFeature feature();
}
