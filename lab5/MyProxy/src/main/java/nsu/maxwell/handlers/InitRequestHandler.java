package nsu.maxwell.handlers;

import nsu.maxwell.attachment.CompleteAttachment;
import nsu.maxwell.attachment.KeyState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static nsu.maxwell.protocol.Protocol.NO_AUTHENTICATION_REQUIRED;
import static nsu.maxwell.protocol.Protocol.VERSION;

public class InitRequestHandler implements Handler {
    @Override
    public void handle(SelectionKey key) {
        boolean hasError = readInitRequest(key);

        if (hasError) {
            System.err.println("Init request error");
            return;
        }
        System.err.println("Init request success: " + key);
    }

    // true - has error
    // false - has no error
    private boolean readInitRequest(SelectionKey key) {
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        SocketChannel clientChannel = (SocketChannel) key.channel();

        ByteBuffer buffer = (ByteBuffer) attachment.getOut();

        try {
            int readResult = clientChannel.read(buffer);

            if (readResult == -1) {
//                attachment.keyState = KeyState.INIT_RESPONSE_FAILED;
//                key.interestOps(SelectionKey.OP_WRITE);
                closeChannel((SocketChannel) key.channel());
                return true;
            }

            else if (!isFullMessage(buffer)) {
                System.err.println("NOT FULL");
                return false;
            }

            boolean isValid = validate(buffer);

            key.interestOps(SelectionKey.OP_WRITE);

            attachment.keyState = isValid ? KeyState.INIT_RESPONSE_SUCCESS : KeyState.INIT_RESPONSE_FAILED;

            return !isValid;
        }

        catch (IOException e) {
            attachment.keyState = KeyState.INIT_RESPONSE_FAILED;
            key.interestOps(SelectionKey.OP_WRITE);
            return true;
        }
    }

    public static boolean validate(ByteBuffer buf) {
        if (buf.get(0) != VERSION) {
            return false;
        }

        int nmethods = buf.get(1);

        for (int i = 2; i < buf.limit(); i++) { // limit - 1 or limit?
            if (buf.get(i) == NO_AUTHENTICATION_REQUIRED) {
                return true;
            }
        }
        return false;
    }

    boolean isFullMessage(ByteBuffer buffer) {
        if (buffer.position() <= 1) return false;
        int nmethods = buffer.get(1);
        return nmethods + 2 == buffer.position();
    }
}