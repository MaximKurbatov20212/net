package nsu.maxwell.dns;

import java.nio.channels.SelectionKey;

public record Info(SelectionKey key, int port) {}
