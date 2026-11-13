package de.raindancer.client.protocol;

import java.util.List;

/**
 * Styled text as it crosses the wire: already flattened by the server, so no game version has to parse
 * another's idea of a chat component.
 */
public record Text(List<Span> spans) {

    public static final int MAX_SPANS = 128;
    public static final Text EMPTY = new Text(List.of());

    public Text {
        spans = List.copyOf(spans);
    }

    public static Text of(String plain) {
        return new Text(List.of(new Span(plain, 0, 0, "")));
    }

    public String plain() {
        StringBuilder all = new StringBuilder();
        spans.forEach(span -> all.append(span.text()));
        return all.toString();
    }

    public boolean isEmpty() {
        return spans.stream().allMatch(span -> span.text().isEmpty());
    }
}
