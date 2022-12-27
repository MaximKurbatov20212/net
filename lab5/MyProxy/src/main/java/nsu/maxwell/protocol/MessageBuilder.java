package nsu.maxwell.protocol;

import nsu.maxwell.attachment.CompleteAttachment;
import java.nio.ByteBuffer;
import java.rmi.ServerError;
import java.util.Arrays;

import static nsu.maxwell.protocol.Protocol.*;

//            +----+-----+-------+------+----------+----------+
//            |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
//            +----+-----+-------+------+----------+----------+
//            | 1  |  1  | X'00' |  1   | Variable |    2     |
//            +----+-----+-------+------+----------+----------+
//
//            Where:
//
//            o  VER    protocol version: X'05'
//            o  REP    Reply field:
//              o  X'00' succeeded
//              o  X'01' general SOCKS server failure
//              o  X'02' connection not allowed by ruleset
//              o  X'03' Network unreachable
//              o  X'04' Host unreachable
//              o  X'05' Connection refused
//              o  X'06' TTL expired
//              o  X'07' Command not supported
//              o  X'08' Address type not supported
//              o  X'09' to X'FF' unassigned
//            o  RSV    RESERVED
//            o  ATYP   address type of following address
//               o  IP V4 address: X'01'
//               o  DOMAINNAME: X'03'
//               o  IP V6 address: X'04'
//            o  BND.ADDR       server bound address
//            o  BND.PORT       server bound port in network octet order

public class MessageBuilder {

    private static int BUFF_SIZE = 1024;
    public static ByteBuffer buildConnectResponseMessage(CompleteAttachment completeAttachment) {
        switch (completeAttachment.keyState) {

            case CONNECT_RESPONSE_SUCCESS -> {
                return getConnectResponseMessage((ByteBuffer) completeAttachment.getOut(), (byte) 0x00);
            }

            case CONNECT_RESPONSE_FAILED -> {
                return getConnectResponseMessage((ByteBuffer) completeAttachment.getOut(), (byte) 0x01);
            }

            case CONNECT_RESPONSE_UNAVAILABLE -> {
                return getConnectResponseMessage((ByteBuffer) completeAttachment.getOut(), (byte) 0x08);
            }

            default -> throw new RuntimeException();
        }
    }

    static ByteBuffer getConnectResponseMessage(ByteBuffer buffer, byte reply) {

//        System.err.println("RESPONSE: " + buffer + " " + Arrays.toString(buffer.array()));

        ByteBuffer buf = ByteBuffer.allocate(BUFF_SIZE);

        buf.put(VERSION);

        buf.put(reply);

        buf.put(RESERVED);

        buf.put(buffer.get(3));

        byte addressType = buffer.get(3);

        switch (addressType) {
            case IPv4 -> buf.put(Arrays.copyOfRange(buffer.array(), 4, 4 + 4 + 2));

            case IPv6 -> buf.put(Arrays.copyOfRange(buffer.array(), 4, 4 + 16 + 2));

            case DOMAIN -> {
                int len = buffer.get(4);
                buf.put(Arrays.copyOfRange(buffer.array(), 4, 4 + len + 1 + 2));
            }

            default -> {
                System.err.println(Arrays.toString(buffer.array()));
                throw new RuntimeException(String.valueOf(addressType));
            }
        }

        buf.flip();
        return buf;
    }

    public static InitResponseMessage buildInitResponseMessage(CompleteAttachment completeAttachment) {
        return getInitResponseMessage((ByteBuffer) completeAttachment.getOut());
    }

    public static InitResponseMessage getInitResponseMessage(ByteBuffer buf) {
        try {
            if (buf.get(0) != VERSION) {
                return null;
            }

            int nmethods = buf.get(1);

            for (int i = 2; i <= 2 + nmethods; i++) {
                if (buf.get(i) == NO_AUTHENTICATION_REQUIRED) {
                    return new InitResponseMessage(VERSION, NO_AUTHENTICATION_REQUIRED);
                }
            }

            return new InitResponseMessage(VERSION, NO_ACCEPTABLE_METHODS);
        }

        catch (IndexOutOfBoundsException e) {  // норм ???
            return new InitResponseMessage(VERSION, NO_AUTHENTICATION_REQUIRED);
        }
    }
}
