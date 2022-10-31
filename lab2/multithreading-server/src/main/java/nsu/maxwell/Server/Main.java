package nsu.maxwell.Server;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("error");
            return;
        }
        int port = Integer.parseInt(args[0]);

        Server server = new Server(port);
        server.listen();
    }
}
