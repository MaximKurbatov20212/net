package nsu.maxwell.handlers;

import nsu.maxwell.attachment.CompleteAttachment;
import nsu.maxwell.attachment.KeyState;
import nsu.maxwell.protocol.MessageBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectResponseHandler implements Handler {

    @Override
    public void handle(SelectionKey key) {
        boolean hasError = writeConnectResponse(key);

        if (hasError) {
//            System.err.println("Couldn't write connect response");
            return;
        }

//        System.err.println("Connect response was successfully write");
    }

    private boolean writeConnectResponse(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        CompleteAttachment completeAttachment = (CompleteAttachment) key.attachment();

        System.err.println("buildConnectResponseMessage : " + key);

        ByteBuffer buffer = (ByteBuffer) completeAttachment.getIn();

        if (!completeAttachment.isRespWroteToBuffer) {
            buffer = MessageBuilder.buildConnectResponseMessage(completeAttachment);
            completeAttachment.isRespWroteToBuffer = true;
        }

        try {
            clientChannel.write(buffer);
        } catch (IOException e) {
            key.interestOps(0);
            key.cancel();
            closeChannel(clientChannel);
            return true;
        }

        if (completeAttachment.keyState == KeyState.CONNECT_RESPONSE_SUCCESS) {
            SelectableChannel remoteChannel = ((CompleteAttachment) key.attachment()).getRemoteChannel();

            SelectionKey remoteKey = remoteChannel.keyFor(key.selector());

            ((CompleteAttachment) remoteKey.attachment()).keyState = KeyState.PROXYING;
            completeAttachment.keyState = KeyState.PROXYING;

            remoteKey.interestOps(SelectionKey.OP_READ);
            key.interestOps(SelectionKey.OP_READ);

            completeAttachment.getOut().clear();
            completeAttachment.getIn().clear();
        }
        else {
            key.cancel();
            closeChannel((SocketChannel) key.channel());
        }

        return false;
    }
}