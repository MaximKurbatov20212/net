package nsu.maxwell.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    static final int MAX_CLIENTS = 10;
    ThreadPoolExecutor executor = new ThreadPoolExecutor(MAX_CLIENTS / 2,
                                                        MAX_CLIENTS,
                                                        10,
                                                        TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<>(MAX_CLIENTS));

    int port;

    Server(int port) {
        Thread speedHandlerTread = new Thread(new SpeedHandler());
        speedHandlerTread.start();
        this.port = port;
    }

    void listen() {
        if (!createUploadsDir()) {
            logger.error("Couldn't create uploads dir");
            return;
        }

        try (ServerSocket socket = new ServerSocket(port)) {
           logger.info("Server init on " + port + " port");

            while (true) {
                Socket client = socket.accept();
                logger.info("New client: " + client.toString() + "\n");
                executor.execute(new ClientHandler(client));
            }
        } catch (IOException e) {
            logger.error("Couldn't create server");
        }
    }

    private boolean createUploadsDir() {
        File file = new File("uploads");

        if (file.exists()) return true;

        try {
            Files.createDirectories(Paths.get("uploads"));
        } catch (IOException ignored) {}

        return false;
    }
}