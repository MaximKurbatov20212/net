package nsu.maxwell.protocol;

//   Where:
//           o  VER    protocol version: X'05'
//           o  CMD
//              o  CONNECT X'01'
//              o  BIND X'02'
//              o  UDP ASSOCIATE X'03'
//           o  RSV    RESERVED
//           o  ATYP   address type of following address
//              o  IP V4 address: X'01'
//              o  DOMAINNAME:    X'03'
//              o  IP V6 address: X'04'
//           o  DST.ADDR desired destination address
//           o  DST.PORT desired destination port in network octet order

import java.nio.ByteBuffer;

public record ConnectionRequestMessage(byte version,
                                       byte connectType,
                                       byte reserved,
                                       byte addressType,
                                       byte[] address,
                                       byte[] port) {
    public int getPortAsInt() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] {0x00, 0x00, port[0], port[1]});
        return buffer.getInt();
    }
}
