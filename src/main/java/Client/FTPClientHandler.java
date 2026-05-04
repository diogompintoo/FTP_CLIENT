package Client;

import java.io.*;
import java.net.Socket;

public class FTPClientHandler {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final DataInputStream dataIn;
    private final DataOutputStream dataOut;

    public FTPClientHandler(Socket socket) throws IOException {
        this.socket = socket;

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.dataIn = new DataInputStream(socket.getInputStream());
        this.dataOut = new DataOutputStream(socket.getOutputStream());

    }

    public void start() throws IOException {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        System.out.println(in.readLine());
        System.out.println(in.readLine());

        String input;
        while ((input = console.readLine()) != null) {

            System.out.println("> ");

            if (input == null || input.trim().isEmpty()) {
                continue;
            }

            String[] parts = input.split(" ", 2);
            String cmd = parts[0].trim().toUpperCase();
            String arg = parts.length > 1 ? parts[1].trim() : null;

            switch (cmd) {
                case "GET":
                    handleGet(arg);
                    break;

                case "PUT":
                    handlePut(arg);
                    break;

                case "DELETE":
                    handleDelete(arg);
                    break;

                case "QUIT":
                case "BYE":
                case "DISCONNECT":
                    System.out.println(in.readLine());
                    socket.close();
                    return;

                    default:
                    String response;
                    for (int i = 0; i < parts.length; i++) {
                        response = in.readLine();
                        System.out.println(response);
                    }
            }
        }
    }
    private void handleGet(String fileName) throws IOException {
        String response = in.readLine();

        if (!response.equals("OK")) {
            System.out.println(response);
            return;
        }

        long size = dataIn.readLong();

        FileOutputStream fos = new FileOutputStream("clientRoot/" + fileName);

        byte[] buffer = new byte[1024];
        int bytesRead;
        long total = 0;

        while (total < size) {
            bytesRead = dataIn.read(buffer);
            if (bytesRead == -1) break;

            fos.write(buffer, 0, bytesRead);
            total += bytesRead;
        }
        fos.close();
        System.out.println("Downloaded file:" + fileName);

    }
    private void handlePut(String fileName) throws IOException {

        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("No file name provided");
            return;
        }
        File file = new File("clientRoot/" + fileName);

        if (!file.exists()) {
            System.out.println("File does not exist");
            return;
        }
        out.println("Uploading:" + fileName);
        out.flush();

        dataOut.writeLong(file.length());
        dataOut.flush();

        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {
            dataOut.write(buffer, 0, bytesRead);
        }
        dataOut.flush();
        fis.close();

        String response = in.readLine();
        System.out.println(response);

    }
    private void handleDelete(String arg) throws IOException {

    }

}











