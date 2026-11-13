package de.raindancer.client.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Reads what {@link PacketWriter} wrote — or what somebody who is not the mod sent instead. Every length is
 * checked against the bytes that are really there before anything is allocated for it.
 */
public final class PacketReader {

    private final byte[] bytes;
    private int at;

    public PacketReader(byte[] bytes) {
        this.bytes = bytes;
    }

    public int remaining() {
        return bytes.length - at;
    }

    public int varInt() {
        int value = 0;
        for (int shift = 0; shift < 35; shift += 7) {
            byte next = one();
            value |= (next & 0x7F) << shift;
            if ((next & 0x80) == 0) {
                return value;
            }
        }
        throw new MalformedPacketException("a number longer than five bytes");
    }

    public boolean bool() {
        return one() != 0;
    }

    public int int32() {
        need(4);
        int value = (bytes[at] & 0xFF) << 24 | (bytes[at + 1] & 0xFF) << 16 | (bytes[at + 2] & 0xFF) << 8 | (bytes[at + 3] & 0xFF);
        at += 4;
        return value;
    }

    public long int64() {
        return ((long) int32() << 32) | (int32() & 0xFFFFFFFFL);
    }

    public float float32() {
        return Float.intBitsToFloat(int32());
    }

    public UUID uuid() {
        return new UUID(int64(), int64());
    }

    public String string(int maxBytes) {
        int length = length(maxBytes);
        try {
            String text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes, at, length)).toString();
            at += length;
            return text;
        } catch (CharacterCodingException notText) {
            throw new MalformedPacketException("text that is not UTF-8");
        }
    }

    public <T> List<T> list(int maxCount, Function<PacketReader, T> each) {
        // Every element is at least one byte, so a count beyond what is left is a lie whatever the type.
        int count = length(maxCount);
        List<T> values = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            values.add(each.apply(this));
        }
        return values;
    }

    private int length(int max) {
        int length = varInt();
        if (length < 0 || length > max || length > remaining()) {
            throw new MalformedPacketException("a length of " + length + " with " + remaining() + " bytes left, at most " + max);
        }
        return length;
    }

    private byte one() {
        need(1);
        return bytes[at++];
    }

    private void need(int count) {
        if (remaining() < count) {
            throw new MalformedPacketException("ends " + (count - remaining()) + " bytes early");
        }
    }
}
