package de.raindancer.client.protocol;

import java.util.List;

/**
 * The server's answer to {@link Hello}. From here on the server may use every feature in {@code agreed}, at
 * exactly that version, and nothing else.
 */
public record Welcome(int protocol, List<FeatureVersion> agreed, String serverName, Theme theme)
        implements ClientboundPacket {

    public Welcome {
        agreed = List.copyOf(agreed);
    }

    @Override
    public ClientFeature feature() {
        return ClientFeature.SESSION;
    }
}
