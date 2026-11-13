package de.raindancer.client.protocol;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The HUD's bytes, written out for the same reason as the handshake's. */
class HudCodecTest {

    private static final HexFormat HEX = HexFormat.of();

    private static final HudPanel PANEL = new HudPanel(
            new Text(List.of(new Span("Hyades", 0xFF6EC1EA, Span.BOLD, ""))),
            List.of(new Text(List.of(new Span("Drachma ", 0, 0, ""), new Span("12", 0xFFF6B83D, 0, ""))), Text.EMPTY));
    private static final String PANEL_BYTES =
            "01000106487961646573ff6ec1ea010002020844726163686d6120000000000000023132fff6b83d000000";

    private static final HudStatus STATUS = new HudStatus(new Text(List.of(
            new Span("", 0, 0, "rainscore:hints"), new Span(" In combat", 0xFFF2843A, 0, ""))), 3000);
    private static final String STATUS_BYTES =
            "01020203ee808100000000000f7261696e73636f72653a68696e74730a20496e20636f6d626174fff2843a0000b817";

    @Test
    void aPanelIsTheseBytes() {
        assertThat(HEX.formatHex(ClientCodec.encode(PANEL))).isEqualTo(PANEL_BYTES);
        assertThat(ClientCodec.decodeClientbound(HEX.parseHex(PANEL_BYTES))).contains(PANEL);
    }

    @Test
    void takingThePanelAwayIsTheseBytes() {
        assertThat(HEX.formatHex(ClientCodec.encode(new HudPanelClear()))).isEqualTo("0101");
        assertThat(ClientCodec.decodeClientbound(HEX.parseHex("0101"))).contains(new HudPanelClear());
    }

    @Test
    void aStatusLineIsTheseBytes() {
        assertThat(HEX.formatHex(ClientCodec.encode(STATUS))).isEqualTo(STATUS_BYTES);
        assertThat(ClientCodec.decodeClientbound(HEX.parseHex(STATUS_BYTES))).contains(STATUS);
    }

    @Test
    void theyBelongToTheHud() {
        assertThat(ClientFeature.HUD.id()).isEqualTo(1);
        assertThat(PANEL.feature()).isEqualTo(ClientFeature.HUD);
        assertThat(new HudPanelClear().feature()).isEqualTo(ClientFeature.HUD);
        assertThat(STATUS.feature()).isEqualTo(ClientFeature.HUD);
    }

    @Test
    void textKnowsWhatItSaysAndWhetherItSaysAnything() {
        assertThat(PANEL.lines().get(0).plain()).isEqualTo("Drachma 12");
        assertThat(Text.EMPTY.isEmpty()).isTrue();
        assertThat(new Text(List.of(new Span("", 0, 0, ""))).isEmpty()).isTrue();
        assertThat(Text.of("plain")).isEqualTo(new Text(List.of(new Span("plain", 0, 0, ""))));
        assertThat(new Span("x", 0, Span.BOLD | Span.ITALIC, "").has(Span.ITALIC)).isTrue();
        assertThat(new Span("x", 0, Span.BOLD, "").has(Span.ITALIC)).isFalse();
    }

    @Test
    void moreThanAScreenHoldsIsNeverBuilt() {
        List<Text> tooMany = Collections.nCopies(HudPanel.MAX_LINES + 1, Text.EMPTY);
        assertThatThrownBy(() -> ClientCodec.encode(new HudPanel(Text.EMPTY, tooMany))).isInstanceOf(IllegalArgumentException.class);
        Text tooFine = new Text(Collections.nCopies(Text.MAX_SPANS + 1, new Span("x", 0, 0, "")));
        assertThatThrownBy(() -> ClientCodec.encode(new HudStatus(tooFine, 1000))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aCutOffPanelIsMalformed() {
        byte[] half = HEX.parseHex(PANEL_BYTES.substring(0, 30));
        assertThatThrownBy(() -> ClientCodec.decodeClientbound(half)).isInstanceOf(MalformedPacketException.class);
    }
}
