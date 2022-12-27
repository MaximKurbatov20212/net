package nsu.maxwell.attachment;

import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class CompleteAttachment extends Attachment {
    private final int BUFF_SIZE = 1024;
    Buffer in, out;
    SocketChannel remoteChannel;
    public InetSocketAddress remoteAddress;
    public boolean isRespWroteToBuffer;
    public boolean isDisconnected;

    public CompleteAttachment(KeyState state, boolean initBuffers) {
        super(state);
        if (initBuffers) {
            in = ByteBuffer.allocate(BUFF_SIZE);
            out = ByteBuffer.allocate(BUFF_SIZE);
        }
    }

    public void joinChannels(SelectionKey otherChannelKey) {
        in = ((CompleteAttachment) otherChannelKey.attachment()).out;
        out = ((CompleteAttachment) otherChannelKey.attachment()).in;
    }

    public void setRemoteChannel(SocketChannel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    public Buffer getIn() {
        return in;
    }

    public Buffer getOut() {
        return out;
    }

    public SocketChannel getRemoteChannel() {
        return remoteChannel;
    }

    public void setRemoteAddress(InetSocketAddress dstAddress) {
        remoteAddress = dstAddress;
    }
}
