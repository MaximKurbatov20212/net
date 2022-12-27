package nsu.maxwell.handlers;

import nsu.maxwell.attachment.DomainAttachment;
import nsu.maxwell.dns.DnsResolver;
import org.xbill.DNS.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class DnsResolverHandler implements Handler {
    DnsResolver dnsResolver = DnsResolver.getInstance();

    @Override
    public void handle(SelectionKey key) {
        DomainAttachment attachment = ((DomainAttachment) (key.attachment()));
        ByteBuffer buffer = attachment.buffer;
        DatagramChannel datagramChannel = (DatagramChannel) key.channel();
        buffer.clear();

        try {
            datagramChannel.receive(buffer);
        }

        catch (IOException e) {
//            System.err.println("Resolve error");
            throw new RuntimeException();
        }

        buffer.flip();
        Message dnsResponse = dnsResolver.getDnsResponse(buffer);

        if (dnsResponse == null) {
//            System.err.println("Dns response error");
            throw new RuntimeException();
        }

        dnsResolver.activateChannels(dnsResponse);
    }
}