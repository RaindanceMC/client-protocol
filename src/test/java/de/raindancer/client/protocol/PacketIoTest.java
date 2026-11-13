package de.raindancer.client.protocol;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PacketIoTest {

    @Test
    void everyTypeComesBackAsWritten() {
        UUID id = UUID.fromString("0b8a4c3e-1f2d-4e5a-9b6c-7d8e9f0a1b2c");
        byte[] bytes = new PacketWriter()
                .varInt(0).varInt(127).varInt(128).varInt(300).varInt(Integer.MAX_VALUE)
                .bool(true).bool(false)
                .int32(0xFF6EC1EA).float32(1.5f).int64(Long.MIN_VALUE).uuid(id)
                .string("Drachma ₯", 32)
                .list(List.of(3, 5), PacketWriter::varInt)
                .toBytes();

        PacketReader in = new PacketReader(bytes);
        assertThat(List.of(in.varInt(), in.varInt(), in.varInt(), in.varInt(), in.varInt()))
                .containsExactly(0, 127, 128, 300, Integer.MAX_VALUE);
        assertThat(in.bool()).isTrue();
        assertThat(in.bool()).isFalse();
        assertThat(in.int32()).isEqualTo(0xFF6EC1EA);
        assertThat(in.float32()).isEqualTo(1.5f);
        assertThat(in.int64()).isEqualTo(Long.MIN_VALUE);
        assertThat(in.uuid()).isEqualTo(id);
        assertThat(in.string(32)).isEqualTo("Drachma ₯");
        assertThat(in.list(8, PacketReader::varInt)).containsExactly(3, 5);
        assertThat(in.remaining()).isZero();
    }

    @Test
    void varIntIsMinecraftsOwn() {
        assertThat(new PacketWriter().varInt(300).toBytes()).containsExactly(0xAC, 0x02);
        assertThat(new PacketWriter().varInt(-1).toBytes()).hasSize(5);
    }

    @Test
    void aVarIntLongerThanFiveBytesIsRefused() {
        byte[] endless = {(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x01};
        assertThatThrownBy(() -> new PacketReader(endless).varInt()).isInstanceOf(MalformedPacketException.class);
    }

    @Test
    void readingPastTheEndIsMalformedNotAnArrayError() {
        assertThatThrownBy(() -> new PacketReader(new byte[]{1, 2}).int32()).isInstanceOf(MalformedPacketException.class);
        assertThatThrownBy(() -> new PacketReader(new byte[0]).varInt()).isInstanceOf(MalformedPacketException.class);
    }

    @Test
    void aStringLongerThanItsCapIsRefusedOnBothSides() {
        assertThatThrownBy(() -> new PacketWriter().string("abcdef", 5)).isInstanceOf(IllegalArgumentException.class);
        byte[] six = new PacketWriter().string("abcdef", 6).toBytes();
        assertThatThrownBy(() -> new PacketReader(six).string(5)).isInstanceOf(MalformedPacketException.class);
    }

    @Test
    void aLengthIsCheckedAgainstTheBytesThatAreThereBeforeAnythingIsAllocated() {
        byte[] claimsTwoBillion = new PacketWriter().varInt(Integer.MAX_VALUE).toBytes();
        assertThatThrownBy(() -> new PacketReader(claimsTwoBillion).string(Integer.MAX_VALUE))
                .isInstanceOf(MalformedPacketException.class);
        assertThatThrownBy(() -> new PacketReader(claimsTwoBillion).list(Integer.MAX_VALUE, PacketReader::varInt))
                .isInstanceOf(MalformedPacketException.class);
    }

    @Test
    void aNegativeLengthIsRefused() {
        byte[] minusOne = new PacketWriter().varInt(-1).toBytes();
        assertThatThrownBy(() -> new PacketReader(minusOne).string(64)).isInstanceOf(MalformedPacketException.class);
        assertThatThrownBy(() -> new PacketReader(minusOne).list(64, PacketReader::varInt))
                .isInstanceOf(MalformedPacketException.class);
    }

    @Test
    void bytesThatAreNotUtf8AreRefused() {
        byte[] broken = {2, (byte) 0xC3, 0x28};
        assertThatThrownBy(() -> new PacketReader(broken).string(64)).isInstanceOf(MalformedPacketException.class);
    }
}
