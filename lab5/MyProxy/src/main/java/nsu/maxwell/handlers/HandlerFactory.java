package nsu.maxwell.handlers;

import nsu.maxwell.attachment.Attachment;

import java.nio.channels.SelectionKey;

public class HandlerFactory {
    static AcceptHandler acceptHandler = new AcceptHandler();
    static InitRequestHandler initRequestHandler = new InitRequestHandler();
    static InitResponseHandler initResponseHandler = new InitResponseHandler();
    static FinishConnectHandler finishConnectHandler = new FinishConnectHandler();
    static ProxyingHandler proxyingHandler = new ProxyingHandler();
    static ConnectRequestHandler connectRequestHandler = new ConnectRequestHandler();
    static ConnectResponseHandler connectResponseHandler = new ConnectResponseHandler();
    static DnsResolverHandler dnsResolverHandler = new DnsResolverHandler();
    public static Handler getHandler(SelectionKey key) {
        Attachment attachment = (Attachment) key.attachment();
//        System.err.println(attachment.keyState);

        switch (attachment.keyState) {
            case ACCEPT -> {
                return acceptHandler;
            }
            case INIT_REQUEST -> {
                return initRequestHandler;
            }

            case INIT_RESPONSE_SUCCESS, INIT_RESPONSE_FAILED -> {
                return initResponseHandler;
            }
            case CONNECT_REQUEST -> {
                return connectRequestHandler;
            }

            case CONNECT_RESPONSE_FAILED, CONNECT_RESPONSE_UNAVAILABLE, CONNECT_RESPONSE_SUCCESS -> {
                return connectResponseHandler;
            }

            case FINISH_CONNECT -> {
                return finishConnectHandler;
            }

            case PROXYING -> {
                return proxyingHandler;
            }

            case DNS_RESPONSE -> {
                return dnsResolverHandler;
            }
        }
        throw new RuntimeException("No such handler");
    }
}
