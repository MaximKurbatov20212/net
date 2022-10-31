package nsu.maxwell.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.text.DecimalFormat;

public class SpeedHandler implements Runnable {
    int DELAY = 3000;
    private static final Logger logger = LoggerFactory.getLogger(SpeedHandler.class);

    @Override
    public void run()  {

        try {
            while (true) {
                long startTime = System.currentTimeMillis();

                synchronized (this) {
                    this.wait(DELAY);
                }

                synchronized (ClientsManager.clients) {
                    for (Socket client : ClientsManager.clients.keySet()) {
                        long prevBytes = 0;
                        double allDataRecv = (double) ClientsManager.get(client).nBytes / 1024D;  // Kb
                        double localDataRevc = (double) (ClientsManager.get(client).nBytes - prevBytes) / 1024D;

                        DecimalFormat df = new DecimalFormat("#####.###########");
                        logger.info("Client" + client.toString() + " : \n" +
                                " ".repeat(4) + "total speed: " + (df.format(allDataRecv / (System.currentTimeMillis() - startTime) * 1000)) + " Kb/sec \n" +
                                " ".repeat(4) + "current speed: " + df.format(localDataRevc / (double) (DELAY / 1000)) + " Kb/sec\n");

                        prevBytes = ClientsManager.get(client).nBytes;

                    }
                    ClientsManager.clients.values().removeIf(value -> value.isDeleted);
                }
            }
        }

        catch (InterruptedException e) {
            logger.error("Error during receiving file!");
            e.printStackTrace();
        }
    }
}
