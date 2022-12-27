package nsu.maxwell;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
//            System.err.println("Bad format. Expected only port");
        }

        try {
            int port = Integer.parseInt(args[0]);
            Proxy proxy = Proxy.getProxy(port);
            assert proxy != null;
            proxy.start();
        }
        catch (NumberFormatException e) {
//            System.err.println("Invalid port");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Hello world!");
    }
}