package nsu.maxwell.handlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface Handler {
    void handle(SelectionKey key);

    default boolean closeChannel(SocketChannel clientChannel) {
        try {
            clientChannel.close();
        }
        catch (IOException e) {
            return true;
        }
        return false;
    }
}
