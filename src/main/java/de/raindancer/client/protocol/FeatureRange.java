package de.raindancer.client.protocol;

/**
 * Which versions of a feature a mod can speak, oldest to newest. A feature by its number — not
 * {@link ClientFeature}, so one invented later still fits.
 */
public record FeatureRange(int feature, int oldest, int newest) {

    public static FeatureRange of(ClientFeature feature) {
        return new FeatureRange(feature.id(), feature.oldest(), feature.newest());
    }
}
