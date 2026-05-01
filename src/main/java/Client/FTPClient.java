package Client;

import java.io.IOException;
import java.net.*;

public class FTPClient {
    public static void main(String[] args) {

        try {
            Socket socket = new Socket ("localhost", 9999);

            FTPClientHandler handler = new FTPClientHandler(socket);
            handler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
