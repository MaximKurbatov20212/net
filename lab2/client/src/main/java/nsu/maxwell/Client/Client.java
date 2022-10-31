package nsu.maxwell.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    String path;
    final long maxFileSize = 1024L * 1024 * 1024 * 1024;
    byte[] buf = new byte[100];

    int serverPort;
    InetAddress inetAddress;

    Client(int port, String path, InetAddress inetAddress) {
        serverPort = port;
        this.path = path;
        this.inetAddress = inetAddress;
    }

    void send()  {
        try (Socket socket = new Socket(inetAddress, serverPort);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             FileInputStream fin = new FileInputStream(path)) {

            File file = new File(path);

            if (file.length() > maxFileSize) {
                logger.error("Too large file...");
                return;
            }

            out.writeUTF(file.getName());
            out.writeLong(file.length());

            int bytes;
            while ((bytes = fin.read(buf)) != -1) {
                out.write(buf, 0, bytes);
            }
            boolean hasConfirmation = getConfirmation(in, buf);
            if (!hasConfirmation) {
                logger.error("Server is unreachable");
            }
        }
        catch (FileNotFoundException e) {
            logger.error("No such file: " + path);
        }
        catch (IOException e) {
            logger.error("Client socket create error or permission denied to read file");
        }
    }

    private boolean getConfirmation(DataInputStream in, byte[] buf) throws IOException {
        if (in.read(buf) == -1) return false;

        if (buf[0] == 1) {
            logger.info("Successfully!");
        } else {
            logger.info("Unsuccessfully!");
        }
        return true;
    }
}