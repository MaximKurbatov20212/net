package nsu.maxwell.protocol;

public abstract class Protocol {
    public final static byte VERSION = 0x05;
    public final static byte NO_AUTHENTICATION_REQUIRED = 0x00;

    public final static byte NO_ACCEPTABLE_METHODS = (byte) 0xFF;
    public final static byte TCP_ESTABLISH_CONNECTION = 0x01;
    public final static byte RESERVED = 0x00;
    public final static byte IPv4 = 0x01;
    public final static byte IPv6 = 0x04;
    public final static byte DOMAIN = 0x03;
}
