package de.raindancer.client.protocol;

/** What a client says. All of it is a claim. */
public sealed interface ServerboundPacket permits Hello {

    ClientFeature feature();
}
