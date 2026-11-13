package de.raindancer.client.protocol;

import java.util.Optional;

/**
 * What the mod can do for a server, each with versions of its own. On the wire a feature is its number, so
 * a side meeting one it has never heard of keeps the number and ignores it.
 *
 * <p>Inside one version a body only ever grows at its end. Anything else is the next version, and
 * {@code oldest} moves up only when a version is really dropped.
 */
public enum ClientFeature {

    /** The handshake and the theme. Every mod has it. */
    SESSION(0, 1, 1),

    /** A panel where the sidebar would be and a line where the action bar would be. */
    HUD(1, 1, 1);

    private final int id;
    private final int oldest;
    private final int newest;

    ClientFeature(int id, int oldest, int newest) {
        this.id = id;
        this.oldest = oldest;
        this.newest = newest;
    }

    public int id() {
        return id;
    }

    public int oldest() {
        return oldest;
    }

    public int newest() {
        return newest;
    }

    public static Optional<ClientFeature> byId(int id) {
        for (ClientFeature feature : values()) {
            if (feature.id == id) {
                return Optional.of(feature);
            }
        }
        return Optional.empty();
    }
}
