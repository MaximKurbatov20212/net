package nsu.maxwell.Server;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class ClientHandler implements Runnable {
    Socket client;

    ClientHandler(Socket client) {
        this.client = client;
    }

    FileOutputStream getFileOutputStream(String path) throws FileNotFoundException {
        path = path.replace('/', '\\');

        if (path.contains("\\")) {
            path = path.substring(path.lastIndexOf("\\"));
        }

        path = "uploads/" + path;

        File file = new File(path);
        return new FileOutputStream(file);
    }
    private void sendAnswerToClient(OutputStream clientOutputStream, Long fileSize) throws IOException {
        if (Objects.equals(fileSize, ClientsManager.get(client).nBytes)) {
            clientOutputStream.write(1);
        } else {
            clientOutputStream.write(0);
        }
    }

    private void recvMessage(FileOutputStream fout, DataInputStream in, long fileSize) throws IOException {
        byte[] buf = new byte[100];

        long remainBytes = fileSize;
        while (!client.isClosed() && remainBytes > 0) {
            int nBytes = in.read(buf, 0, 100);
            if (nBytes == -1) break;
            remainBytes -= nBytes;
            ClientsManager.update(client, nBytes);
            fout.write(buf);
        }
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(client.getInputStream());
             OutputStream clientOutputStream = client.getOutputStream()) {

            final String fileName = in.readUTF();
            final long fileSize = in.readLong();

            try (FileOutputStream fout = getFileOutputStream(fileName)) {

                ClientsManager.put(client, 0);

                recvMessage(fout, in, fileSize);

                sendAnswerToClient(clientOutputStream, fileSize);

            }
        } catch (IOException e) {
            System.err.println("Unexpected error during receive message");
        } finally {
            ClientsManager.shadowDelete(client);
        }
    }
}
