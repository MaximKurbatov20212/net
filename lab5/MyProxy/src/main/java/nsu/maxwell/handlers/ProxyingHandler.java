package nsu.maxwell.handlers;

import nsu.maxwell.attachment.CompleteAttachment;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ProxyingHandler implements Handler {
    @Override
    public void handle(SelectionKey key) {
        SelectionKey remoteKey = ((CompleteAttachment) key.attachment()).getRemoteChannel().keyFor(key.selector());

        if (key.isReadable()) {
            read(key, remoteKey);
        }

        else if (key.isWritable()) {
            write(key, remoteKey);
        }
        else {
            throw new RuntimeException();
        }
    }

    private void write(SelectionKey key, SelectionKey remoteKey) {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        int nbytes = 0;
        ByteBuffer buffer = (ByteBuffer) ((CompleteAttachment) key.attachment()).getIn();

        try {
            nbytes = clientChannel.write(buffer);
        }

        catch (IOException e) {
            closeAll(key, remoteKey);
            key.cancel();
            remoteKey.cancel();
            return;
        }
        if (buffer.remaining() != 0) return;

        if (((CompleteAttachment) remoteKey.attachment()).isDisconnected) {
            shutdownChannel(clientChannel);
            remoteKey.interestOps(remoteKey.interestOps() ^ SelectionKey.OP_READ);

            if (((CompleteAttachment) key.attachment()).isDisconnected) {
                closeAll(key, remoteKey);
                key.cancel();
                remoteKey.cancel();
                return;
            }
        }

        else {
            remoteKey.interestOps(remoteKey.interestOps() | SelectionKey.OP_READ);
        }

        buffer.flip();
        key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
    }

    private void read(SelectionKey key, SelectionKey remoteKey) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        CompleteAttachment completeAttachment = (CompleteAttachment) key.attachment();

        ByteBuffer buffer = (ByteBuffer) ((CompleteAttachment) key.attachment()).getOut();
        int nbytes = 0;

        try {
            nbytes = clientChannel.read(buffer);
        }

        catch (IOException e) {
            key.cancel();
            remoteKey.cancel();
            closeAll(key, remoteKey);
            return;
        }

        if (nbytes == -1) {
            completeAttachment.isDisconnected = true;
            remoteKey.interestOps(remoteKey.interestOps() | SelectionKey.OP_WRITE);
            return;
        }

        buffer.flip();
        key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
        remoteKey.interestOps(remoteKey.interestOps() | SelectionKey.OP_WRITE);
    }

    private void closeAll(SelectionKey key, SelectionKey remoteKey) {
        closeChannel((SocketChannel) key.channel());
        closeChannel((SocketChannel) remoteKey.channel());
        key.cancel();
        remoteKey.cancel();
    }

    private void shutdownChannel(SocketChannel channel) {
        try {
            channel.shutdownOutput();
        } catch (IOException ignore) {}
    }
}
