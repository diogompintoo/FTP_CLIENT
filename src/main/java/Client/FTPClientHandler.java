package Client;

import Utility.Constants;

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

    public void start() throws IOException, InterruptedException {

        System.out.println(in.readLine());
        System.out.println(in.readLine());

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String input;

        while (true) {

            System.out.println("> ");
            input = console.readLine();

            if (input == null || input.trim().isEmpty()) {
                continue;
            }

            String[] parts = input.split(" ", 2);
            String cmd = parts[0].trim().toUpperCase();
            String arg = parts.length > 1 ? parts[1].trim() : null;

            out.println(input);
            out.flush();

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
                        readServerResponse();
                        break;
            }
        }
    }
    private void readServerResponse() throws IOException {

        String line;

        while ((line = in.readLine()) != null) {
            if (line.equals(".") || line.contains("Upload OK") ||
                    line.startsWith("Directory") || line.startsWith("Deleted")) {
                System.out.println(line);
                break;
            }
            System.out.println(line);

            if (line.startsWith("No file found") ||
                    line.startsWith("Unknown command") ||
                    line.startsWith("Directory") ||
                    line.startsWith("Failed to delete")) {
                break;
            }
        }
    }

    private void handleGet(String fileName) throws IOException {

        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("Invalid file name");
            return;
        }

        String response = in.readLine();
        System.out.println(response);

        if (!response.equals("OK")) {
            System.out.println(response);
            return;
        }

        long size = dataIn.readLong();

        File file = new File(Constants.CLIENT_ROOT + fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {

        byte[] buffer = new byte[1024];
        int bytesRead;
        long total = 0;

        while (total < size && (bytesRead = dataIn.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
            total += bytesRead;
        }
        }
        System.out.println(fileName + "Downloaded");

    }

    private void handlePut(String fileName) throws IOException {

        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("No file name provided");
            return;
        }
        File file = new File(Constants.CLIENT_ROOT + fileName);

        if (!file.exists() || file.isDirectory()) {
            System.out.println("File does not exist");
            return;
        }
        dataOut.writeLong(file.length());
        dataOut.flush();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }
            dataOut.flush();
        }
        String response = in.readLine();
        System.out.println(response);

    }
    private void handleDelete(String arg) throws IOException {

    }

}











