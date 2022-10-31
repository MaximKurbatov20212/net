package nsu.maxwell.Client;

import java.io.IOException;
import java.net.InetAddress;

public class Main {
    static final int maxLen = 4096;
    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.err.println("Invalid count of args. Usage: java -jar <file_path> <ip> <server_port>");
            return;
        }

        String path = args[0];

        if (path.getBytes().length> maxLen) {
            System.err.println("Too long path.");
            return;
        }

        int port = Integer.parseInt(args[2]);
        InetAddress inetAddress = InetAddress.getByName(args[1]);

        Client client = new Client(port, path, inetAddress);
        client.send();
    }
}