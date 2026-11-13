package de.raindancer.client.protocol;

import java.util.List;

/**
 * The first thing said on the channel, and by the client: a server never asks. Sent once per connection to
 * a backend, only to a server that announced {@link Wire#CHANNEL}.
 *
 * @param protocol the newest {@link Wire#PROTOCOL} this mod speaks
 * @param features what it offers, each with the versions it can speak
 */
public record Hello(int protocol, String modVersion, String minecraft, List<FeatureRange> features, int guiScale,
                    String locale) implements ServerboundPacket {

    public Hello {
        features = List.copyOf(features);
    }

    @Override
    public ClientFeature feature() {
        return ClientFeature.SESSION;
    }
}
