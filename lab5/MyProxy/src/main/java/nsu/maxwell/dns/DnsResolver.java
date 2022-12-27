package nsu.maxwell.dns;

import nsu.maxwell.attachment.CompleteAttachment;
import nsu.maxwell.attachment.KeyState;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.*;

import static org.xbill.DNS.Section.ANSWER;

public class DnsResolver {

    record DomainInfo(String domain, boolean wasAttempt) {}
    private final InetSocketAddress dnsServer;
    private final HashMap<String, ArrayList<Info>> unresolvedDomains;
    private final DatagramChannel resolverChannel;
    private static DnsResolver INSTANCE;
    public static DnsResolver getInstance() {
        return INSTANCE;
    }
    public static void create(DatagramChannel resolverChannel) {
        INSTANCE = new DnsResolver(resolverChannel);
    }
    private DnsResolver(DatagramChannel resolverChannel) {
        this.unresolvedDomains = new HashMap<>();
        dnsServer = ResolverConfig.getCurrentConfig().servers().get(0);
        this.resolverChannel = resolverChannel;
    }

    public void resolveDomain(String domain, Info info) {
        if (unresolvedDomains.containsKey(domain)) {
            unresolvedDomains.get(domain).add(info);
        }

        else {
            ArrayList<Info> list = new ArrayList<>();
            list.add(info);
            unresolvedDomains.put(domain, list);
            sendRequest(domain);
        }
    }

    private void sendRequest(String unresolvedDomain) {
        try {
            Message message = createDnsMessage(unresolvedDomain);
            resolverChannel.send(ByteBuffer.wrap(message.toWire()), dnsServer);
        }
        catch (TextParseException e) {
            throw new RuntimeException();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Message createDnsMessage(String name) throws TextParseException {
        Message result = new Message();
        Header header = new Header();
        header.setFlag(Flags.RD);                   // recursion available
        header.setOpcode(Opcode.QUERY);             // standard dns-query
        Record record = Record.newRecord(new Name(name + "."), Type.A, DClass.IN);  // Type - 'Address', Class - 'Internet'
        result.setHeader(header);
        result.addRecord(record, Section.QUESTION); // write record in 'Question' section of the message
        return result;
    }

    public Message getDnsResponse(ByteBuffer buffer) {
        try {
            return new Message(buffer);
        } catch (IOException e) {
            return null;
        }
    }

    public void activateChannels(Message dnsResponse) {
        String hostName = dnsResponse.getQuestion().getName().toString(); // domain
        hostName = hostName.substring(0, hostName.length() - 1);

        String address;

        Optional<Record> maybe = dnsResponse.getSection(Section.ANSWER).stream().filter(r -> r.getType() == Type.A).findAny();

        List<Record> list  = dnsResponse.getSection(Section.ANSWER);

        ArrayList<Info> channels = unresolvedDomains.get(hostName);

        if (channels == null) return;

        if (maybe.isPresent()) {

            address = maybe.get().rdataToString();

            System.err.println("Resolve " + hostName + " " + address);

            for (Info channel : channels) {

                if (!channel.key().isValid()) continue;

                channel.key().interestOps(SelectionKey.OP_WRITE);
                InetSocketAddress dstAddress = new InetSocketAddress(address, channel.port());
                ((CompleteAttachment) (channel.key().attachment())).setRemoteAddress(dstAddress);
                ((CompleteAttachment) (channel.key().attachment())).keyState = KeyState.CONNECT_REQUEST;
            }
        }

        else {
            System.err.println("Couldn't resolve " + hostName );

            for (Info channel : channels) {
                if (!channel.key().isValid()) continue;

                channel.key().interestOps(SelectionKey.OP_WRITE);
                ((CompleteAttachment) (channel.key().attachment())).keyState = KeyState.CONNECT_RESPONSE_UNAVAILABLE;
            }
        }

        unresolvedDomains.remove(hostName);
    }
}