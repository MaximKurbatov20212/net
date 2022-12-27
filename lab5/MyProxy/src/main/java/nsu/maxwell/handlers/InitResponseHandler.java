package nsu.maxwell.handlers;

import nsu.maxwell.attachment.CompleteAttachment;
import nsu.maxwell.attachment.KeyState;
import nsu.maxwell.protocol.InitResponseMessage;
import nsu.maxwell.protocol.MessageBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class InitResponseHandler implements Handler {
    @Override
    public void handle(SelectionKey key) {
        boolean hasError = writeInitResponse(key);

        if (hasError) {
//            System.err.println("Couldn't write init response from client");
            return;
        }

//        System.err.println("Init response was successfully write");
    }

    private boolean writeInitResponse(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        CompleteAttachment completeAttachment = (CompleteAttachment) key.attachment();

        InitResponseMessage msg = MessageBuilder.buildInitResponseMessage(completeAttachment);

        try {
            clientChannel.write(msg.toBytes());
        } catch (IOException e) {
            key.cancel();
            closeChannel(clientChannel);
            return true;
        }

        completeAttachment.keyState = KeyState.CONNECT_REQUEST;
        key.interestOps(SelectionKey.OP_READ);

        completeAttachment.getOut().clear();
        completeAttachment.getIn().clear();
        return false;
    }
}
