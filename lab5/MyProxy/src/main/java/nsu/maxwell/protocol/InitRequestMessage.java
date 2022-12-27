package nsu.maxwell.protocol;

public record InitRequestMessage(byte version, byte nmethods, byte[] methods) {}