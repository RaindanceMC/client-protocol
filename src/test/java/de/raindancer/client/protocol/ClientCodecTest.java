package de.raindancer.client.protocol;

import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The bytes here are written out, not computed: they are what a mod that is already installed sends, so a
 * change that makes one of these fail is a change that breaks every player who has not updated yet.
 */
class ClientCodecTest {

    private static final HexFormat HEX = HexFormat.of();

    private static final Hello HELLO = new Hello(1, "0.1.0", "26.3",
            List.of(new FeatureRange(ClientFeature.SESSION.id(), 1, 1)), 3, "de_de");
    private static final String HELLO_BYTES = "00000105302e312e300432362e3301000101030564655f6465";

    private static final Welcome WELCOME = new Welcome(1,
            List.of(new FeatureVersion(ClientFeature.SESSION.id(), 1)), "Hyades", Theme.HARBOR_SUNSET);
    private static final String WELCOME_BYTES = "000001010001064879616465730d486172626f722053756e736574"
            + "ff070a1cff0f1e3dff1f4e8cff6ec1eafff6b83dfff2843affeef3faffa9b9cf0b6879616465733a6c6f676f";

    @Test
    void helloIsTheseBytes() {
        assertThat(HEX.formatHex(ClientCodec.encode(HELLO))).isEqualTo(HELLO_BYTES);
        assertThat(ClientCodec.decodeServerbound(HEX.parseHex(HELLO_BYTES))).contains(HELLO);
    }

    @Test
    void welcomeIsTheseBytes() {
        assertThat(HEX.formatHex(ClientCodec.encode(WELCOME))).isEqualTo(WELCOME_BYTES);
        assertThat(ClientCodec.decodeClientbound(HEX.parseHex(WELCOME_BYTES))).contains(WELCOME);
    }

    @Test
    void whatANewerSideAppendedIsIgnored() {
        byte[] longer = HEX.parseHex(HELLO_BYTES + "07ffffff");
        assertThat(ClientCodec.decodeServerbound(longer)).contains(HELLO);
    }

    @Test
    void anUnknownFeatureOrTypeIsNothingRatherThanAnError() {
        assertThat(ClientCodec.decodeServerbound(HEX.parseHex("6300"))).isEmpty();
        assertThat(ClientCodec.decodeServerbound(HEX.parseHex("0063"))).isEmpty();
        assertThat(ClientCodec.decodeClientbound(HEX.parseHex("6300ff"))).isEmpty();
    }

    @Test
    void aFeatureTheOtherSideInventedLaterSurvivesTheHello() {
        Hello fromTheFuture = new Hello(1, "9.0.0", "27.1", List.of(new FeatureRange(4711, 2, 5)), 2, "en_us");
        assertThat(ClientCodec.decodeServerbound(ClientCodec.encode(fromTheFuture))).contains(fromTheFuture);
        assertThat(ClientFeature.byId(4711)).isEmpty();
    }

    @Test
    void aCutOffPacketIsMalformed() {
        byte[] half = HEX.parseHex(HELLO_BYTES.substring(0, 20));
        assertThatThrownBy(() -> ClientCodec.decodeServerbound(half)).isInstanceOf(MalformedPacketException.class);
        assertThatThrownBy(() -> ClientCodec.decodeServerbound(new byte[0])).isInstanceOf(MalformedPacketException.class);
    }

    @Test
    void aFrameLargerThanTheGameCarriesIsNeverBuilt() {
        Hello tooLong = new Hello(1, "x".repeat(65), "26.3", List.of(), 1, "en_us");
        assertThatThrownBy(() -> ClientCodec.encode(tooLong)).isInstanceOf(IllegalArgumentException.class);
        assertThat(Wire.MAX_SERVERBOUND).isEqualTo(32767);
        assertThat(Wire.MAX_CLIENTBOUND).isEqualTo(1048576);
    }

    @Test
    void randomBytesNeverThrowAnythingButMalformed() {
        Random random = new Random(20261113);
        for (int run = 0; run < 200_000; run++) {
            byte[] noise = new byte[random.nextInt(96)];
            random.nextBytes(noise);
            if (noise.length > 1 && random.nextBoolean()) {
                noise[0] = 0;
                noise[1] = 0;
            }
            for (boolean serverbound : new boolean[]{true, false}) {
                try {
                    if (serverbound) {
                        ClientCodec.decodeServerbound(noise);
                    } else {
                        ClientCodec.decodeClientbound(noise);
                    }
                } catch (MalformedPacketException expected) {
                    // the one allowed outcome besides a packet or nothing
                }
            }
        }
    }

    @Test
    void aPacketKnowsWhichFeatureItBelongsTo() {
        assertThat(HELLO.feature()).isEqualTo(ClientFeature.SESSION);
        assertThat(WELCOME.feature()).isEqualTo(ClientFeature.SESSION);
        assertThat(FeatureRange.of(ClientFeature.SESSION)).isEqualTo(new FeatureRange(0, 1, 1));
    }

    @Test
    void theChannelIsOneBukkitAndFabricBothAccept() {
        assertThat(Wire.CHANNEL).isEqualTo("rains:client").matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }
}
