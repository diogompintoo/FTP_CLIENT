package Client;

import java.io.*;
import java.net.Socket;

public class FTPClientHandler {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public FTPClientHandler(Socket socket) throws IOException {
        this.socket = socket;

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);


    }

    public void start() throws IOException {

    }

}
