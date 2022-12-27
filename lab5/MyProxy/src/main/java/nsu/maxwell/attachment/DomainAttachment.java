package nsu.maxwell.attachment;

import java.nio.ByteBuffer;

public class DomainAttachment extends Attachment {
    public ByteBuffer buffer;
    private final int BUFF_SUZE = 1024;
    public DomainAttachment(KeyState dnsResponse) {
        super(dnsResponse);
        buffer = ByteBuffer.allocate(BUFF_SUZE);
    }
}
