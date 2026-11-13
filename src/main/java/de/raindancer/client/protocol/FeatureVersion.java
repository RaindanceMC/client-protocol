package de.raindancer.client.protocol;

/** One feature at the one version both sides settled on. */
public record FeatureVersion(int feature, int version) {
}
