package nsu.maxwell.Server;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientsManager {

    static class ClientInfo {
        Long nBytes;
        boolean isDeleted;
        public ClientInfo(long bytes, boolean b) {
            nBytes = bytes;
            isDeleted = b;
        }
    }

    static final Map<Socket, ClientInfo> clients = new HashMap<>();
    static void put(Socket client, long bytes) {
        synchronized (clients) {
            clients.put(client, new ClientInfo(bytes, false));
        }
    }

    static ClientInfo get(Socket client) {
        synchronized (clients) {
            return clients.get(client);
        }
    }

    public static void shadowDelete(Socket client) {
        synchronized (clients) {
            ClientInfo clientInfo = clients.get(client);
            if (clientInfo == null) return;
            clientInfo.isDeleted = true;
        }
    }

    public static void update(Socket client, long newBytes) {
        synchronized (clients) {
            Long oldValue = clients.get(client).nBytes;
            if (oldValue == null) return;
            clients.put(client, new ClientInfo(newBytes + oldValue, false));
        }
    }
}
