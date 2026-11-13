package de.raindancer.client.protocol;

import java.util.Optional;

/** Packets to frames and back. The one place that knows which number means what. */
public final class ClientCodec {

    private static final int SESSION = ClientFeature.SESSION.id();
    private static final int HELLO = 0;
    private static final int WELCOME = 0;

    private static final int MAX_FEATURES = 64;

    private ClientCodec() {
    }

    public static byte[] encode(ServerboundPacket packet) {
        PacketWriter out = new PacketWriter();
        switch (packet) {
            case Hello hello -> {
                out.varInt(SESSION).varInt(HELLO).varInt(hello.protocol())
                        .string(hello.modVersion(), 64).string(hello.minecraft(), 32);
                if (hello.features().size() > MAX_FEATURES) {
                    throw new IllegalArgumentException(hello.features().size() + " features where " + MAX_FEATURES + " fit");
                }
                out.list(hello.features(), (writer, each) ->
                        writer.varInt(each.feature()).varInt(each.oldest()).varInt(each.newest()));
                out.varInt(hello.guiScale()).string(hello.locale(), 16);
            }
        }
        return fitting(out, Wire.MAX_SERVERBOUND);
    }

    public static byte[] encode(ClientboundPacket packet) {
        PacketWriter out = new PacketWriter();
        switch (packet) {
            case Welcome welcome -> {
                out.varInt(SESSION).varInt(WELCOME).varInt(welcome.protocol());
                features(out, welcome.agreed());
                out.string(welcome.serverName(), 64);
                Theme theme = welcome.theme();
                out.string(theme.name(), 32)
                        .int32(theme.night()).int32(theme.navy()).int32(theme.harbor()).int32(theme.sky())
                        .int32(theme.gold()).int32(theme.ember()).int32(theme.text()).int32(theme.muted())
                        .string(theme.logo(), 128);
            }
        }
        return fitting(out, Wire.MAX_CLIENTBOUND);
    }

    /** Empty for a feature or type this build does not know. */
    public static Optional<ServerboundPacket> decodeServerbound(byte[] frame) {
        PacketReader in = new PacketReader(frame);
        int feature = in.varInt();
        int type = in.varInt();
        if (feature == SESSION && type == HELLO) {
            return Optional.of(new Hello(in.varInt(), in.string(64), in.string(32),
                    in.list(MAX_FEATURES, reader -> new FeatureRange(reader.varInt(), reader.varInt(), reader.varInt())),
                    in.varInt(), in.string(16)));
        }
        return Optional.empty();
    }

    /** Empty for a feature or type this build does not know. */
    public static Optional<ClientboundPacket> decodeClientbound(byte[] frame) {
        PacketReader in = new PacketReader(frame);
        int feature = in.varInt();
        int type = in.varInt();
        if (feature == SESSION && type == WELCOME) {
            return Optional.of(new Welcome(in.varInt(), features(in), in.string(64),
                    new Theme(in.string(32), in.int32(), in.int32(), in.int32(), in.int32(), in.int32(), in.int32(),
                            in.int32(), in.int32(), in.string(128))));
        }
        return Optional.empty();
    }

    private static void features(PacketWriter out, java.util.List<FeatureVersion> features) {
        if (features.size() > MAX_FEATURES) {
            throw new IllegalArgumentException(features.size() + " features where " + MAX_FEATURES + " fit");
        }
        out.list(features, (writer, each) -> writer.varInt(each.feature()).varInt(each.version()));
    }

    private static java.util.List<FeatureVersion> features(PacketReader in) {
        return in.list(MAX_FEATURES, reader -> new FeatureVersion(reader.varInt(), reader.varInt()));
    }

    private static byte[] fitting(PacketWriter out, int max) {
        byte[] frame = out.toBytes();
        if (frame.length > max) {
            throw new IllegalArgumentException("a frame of " + frame.length + " bytes where " + max + " fit");
        }
        return frame;
    }
}
