package nsu.maxwell.handlers;

import nsu.maxwell.attachment.CompleteAttachment;
import nsu.maxwell.attachment.KeyState;
import nsu.maxwell.dns.DnsResolver;
import nsu.maxwell.dns.Info;
import nsu.maxwell.protocol.ConnectionRequestMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static nsu.maxwell.attachment.KeyState.FINISH_CONNECT;
import static nsu.maxwell.protocol.Protocol.*;
import static nsu.maxwell.protocol.Protocol.RESERVED;

public class ConnectRequestHandler implements Handler {
    @Override
    public void handle(SelectionKey key) {
        boolean hasError = readConnectRequest(key);

        if (hasError) {
            System.err.println("connect request: error" + key);
            return;
        }

        System.err.println("connect request success + " + key);
    }

    private boolean readConnectRequest(SelectionKey key) {
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        SocketChannel clientChannel = (SocketChannel) key.channel();

        attachment.keyState = KeyState.CONNECT_RESPONSE_SUCCESS;
        ByteBuffer buffer = (ByteBuffer) attachment.getOut();

        if (attachment.remoteAddress != null) {
            try {
                connectToServer(key, attachment);
                System.err.println("connect" + key);
                return false;
            }

            catch (Exception e) {
                key.interestOps(SelectionKey.OP_WRITE);
                attachment.keyState = KeyState.CONNECT_RESPONSE_FAILED;
                return true;
            }
        }

        buffer.clear();

        try {
            int readResult = clientChannel.read(buffer);

            if (readResult == -1) {
                attachment.keyState = KeyState.CONNECT_RESPONSE_FAILED;
                key.interestOps(SelectionKey.OP_WRITE);
                System.err.println("readResult == -1 : " + key);
                return true;
            }

            if (!isFullMessage(buffer)) {
                System.err.println("Not full message " + key);
                return false;
            }

            System.err.println("READ: " + buffer + " " + key);

            buffer.flip();
        }

        catch (IOException e) {
            System.err.println("IOEXECTION" + key);
            closeChannel((SocketChannel) key.channel());
            return true;
        }

            ConnectionRequestMessage connectionRequestMessage = parseConnectRequest(buffer);

            key.interestOps(SelectionKey.OP_WRITE);

            if (connectionRequestMessage == null)  {
                attachment.keyState = KeyState.CONNECT_RESPONSE_FAILED;
                System.err.println("connectionRequestMessage == null" + key);
                return true;
            }

            System.err.println("Connected: address = " + new String(connectionRequestMessage.address()) + " port = " + connectionRequestMessage.getPortAsInt());

            switch (connectionRequestMessage.addressType()) {
                case IPv4 -> {
                    try {
                        attachment.remoteAddress =
                                new InetSocketAddress(InetAddress.getByAddress(connectionRequestMessage.address()),
                                                    connectionRequestMessage.getPortAsInt());
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        connectToServer(key, attachment);
                    }

                    catch (Exception e) {
                        System.err.println("CONNECT ERROR" + key);
                        attachment.keyState = KeyState.CONNECT_RESPONSE_FAILED;
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                }
                case IPv6 -> attachment.keyState = KeyState.CONNECT_RESPONSE_UNAVAILABLE;

                case DOMAIN -> {
                    key.interestOps(0);

                    DnsResolver resolver = DnsResolver.getInstance();

                    resolver.resolveDomain(new String(connectionRequestMessage.address(), StandardCharsets.ISO_8859_1),
                            new Info(key, connectionRequestMessage.getPortAsInt()));

                    attachment.keyState = KeyState.DNS_RESPONSE;
                }

                default -> throw new RuntimeException();
            }
            return false;

    }

    public void connectToServer(SelectionKey key, CompleteAttachment attachment) throws IOException {
        SocketChannel remoteChannel;
        remoteChannel = SocketChannel.open();
        remoteChannel.configureBlocking(false);

        remoteChannel.connect(attachment.remoteAddress);

        CompleteAttachment remoteChannelAttachment = new CompleteAttachment(FINISH_CONNECT, false);

        remoteChannelAttachment.joinChannels(key);

        remoteChannelAttachment.setRemoteChannel((SocketChannel) key.channel());

        attachment.setRemoteChannel(remoteChannel);

        key.interestOps(0);

        remoteChannel.register(key.selector(), SelectionKey.OP_CONNECT, remoteChannelAttachment);
    }

    private ConnectionRequestMessage parseConnectRequest(ByteBuffer buf) {
        if (buf.get(0) != VERSION) return null;

        if (buf.get(1) != TCP_ESTABLISH_CONNECTION) return null;

        if (buf.get(2) != RESERVED) return null;

        byte addressType = buf.get(3);

        switch (addressType) {
            case IPv4 -> {
                System.err.println("IPV4");
                if (buf.limit() != 10) return null;
                return new ConnectionRequestMessage(buf.get(0),
                        buf.get(1),
                        buf.get(2),
                        buf.get(3),
                        Arrays.copyOfRange(buf.array(), 4, 8),
                        Arrays.copyOfRange(buf.array(), 8, 10));
            }

            case DOMAIN -> {
                System.err.println("Domain");
                int len = buf.get(4);
                if (buf.limit() != 5 + len + 2) return null;

                return new ConnectionRequestMessage(buf.get(0),
                        buf.get(1),
                        buf.get(2),
                        buf.get(3),
                        Arrays.copyOfRange(buf.array(), 5, 5 + len),
                        Arrays.copyOfRange(buf.array(), 5 + len, 5 + len + 2));
            }

            case IPv6 -> {
                if (buf.limit() != 18) return null;
                return new ConnectionRequestMessage(buf.get(0),
                        buf.get(1),
                        buf.get(2),
                        buf.get(3),
                        Arrays.copyOfRange(buf.array(), 4, 20),
                        Arrays.copyOfRange(buf.array(), 20, 22));
            }

            default -> throw new RuntimeException();
        }
    }

    boolean isFullMessage(ByteBuffer buf) {
        if (buf.position() < 4) return false;

        byte addressType = buf.get(3);

        switch (addressType) {
            case IPv4 -> {
                return buf.position() == 10;
            }

            case DOMAIN -> {
                int len = buf.get(4);
                return  buf.position() != 5 + len + 1 + 2;
            }

            case IPv6 -> {
                return buf.position() != 18;
            }
        }

        throw new RuntimeException();
    }
}
