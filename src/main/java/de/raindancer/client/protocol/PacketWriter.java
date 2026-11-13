package de.raindancer.client.protocol;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class PacketWriter {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream(64);

    public PacketWriter varInt(int value) {
        while ((value & ~0x7F) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value);
        return this;
    }

    public PacketWriter bool(boolean value) {
        out.write(value ? 1 : 0);
        return this;
    }

    public PacketWriter int32(int value) {
        out.write(value >>> 24);
        out.write(value >>> 16);
        out.write(value >>> 8);
        out.write(value);
        return this;
    }

    public PacketWriter int64(long value) {
        return int32((int) (value >>> 32)).int32((int) value);
    }

    public PacketWriter float32(float value) {
        return int32(Float.floatToIntBits(value));
    }

    public PacketWriter uuid(UUID value) {
        return int64(value.getMostSignificantBits()).int64(value.getLeastSignificantBits());
    }

    /** @param maxBytes the same cap the reader on the other side applies, in UTF-8 bytes */
    public PacketWriter string(String value, int maxBytes) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > maxBytes) {
            throw new IllegalArgumentException("text of " + bytes.length + " bytes where " + maxBytes + " fit");
        }
        varInt(bytes.length);
        out.writeBytes(bytes);
        return this;
    }

    public <T> PacketWriter list(Collection<T> values, BiConsumer<PacketWriter, T> each) {
        varInt(values.size());
        for (T value : values) {
            each.accept(this, value);
        }
        return this;
    }

    public byte[] toBytes() {
        return out.toByteArray();
    }
}
