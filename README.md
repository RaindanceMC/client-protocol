# RaindanceMC/client-protocol

What [RainsCore](https://github.com/RaindanceMC/RainsCore) and the [client mod](https://github.com/RaindanceMC/client-mod)
say to each other on the plugin channel `rains:client`: the packets and their bytes. One artifact both sides
compile against, so they cannot disagree about a byte.

Plain Java, no dependencies, compiled for Java 21 (the mod also runs in a 1.21.x client).

## The wire

A plugin message is one frame: `varint feature | varint type | body`.

- The client speaks first. A server that announced the channel gets a `Hello` (what the mod is, which versions
  of which feature it speaks); it answers with a `Welcome` (what was agreed, the server's name and theme).
- Every feature has versions of its own. Inside one version a body only grows at its end, so a reader
  ignores bytes it did not ask for. Anything else is the next version.
- A side that does not know a feature or a type drops the frame.
- A decoder throws `MalformedPacketException` and nothing else, whatever it is handed, and checks every length
  against the bytes that are really there before allocating.

`ClientCodecTest` holds the bytes of each packet written out. A change that makes one of those fail breaks
every player who has not updated their mod yet.

## Build

```
mvn install
```

CI publishes the pom's version to `packages.tstieh.de` on a push to master, once.
