package nsu.maxwell.protocol;

public record ConnectionResponseMessage(byte version,
                                byte reply,
                                byte reserved,
                                byte addressType,
                                byte[] address,
                                byte[] port) {}
