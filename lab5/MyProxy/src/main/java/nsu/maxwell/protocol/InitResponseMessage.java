package nsu.maxwell.protocol;

import java.nio.ByteBuffer;

public record InitResponseMessage(byte version, byte method) {
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put(version);
        buffer.put(method);
        buffer.flip();
        return buffer;
    }
}
