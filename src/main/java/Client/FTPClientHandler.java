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

            System.out.print("> ");
            input = console.readLine();

            if (input == null || input.trim().isEmpty()) {continue;}

            String[] parts = input.split(" ", 2);
            String cmd = parts[0].trim().toUpperCase();
            String arg = parts.length > 1 ? parts[1].trim() : null;


            switch (cmd) {
                case "GET":
                    out.println(input);
                    out.flush();
                    handleGet(arg);
                    break;

                case "PUT":
                    out.println(input);
                    out.flush();
                    handlePut(arg);
                    break;

                case "DELETE":
                    out.println(input);
                    out.flush();
                    handleDelete(arg);
                    break;

                case "QUIT":
                case "BYE":
                case "DISCONNECT":
                    out.println(input);
                    out.flush();
                    System.out.println(in.readLine());
                    socket.close();
                    return;

                    default:
                        out.println(input);
                        out.flush();
                        readServerResponse();
                        break;
            }
        }
    }
    private void readServerResponse() throws IOException {

        String line;
        boolean hasResponse = false;

        while ((line = in.readLine()) != null) {
            hasResponse = true;
            System.out.println(line);

            if (line.equals(".")){
                break;
            }
            if (line.startsWith("Directory") ||
                    line.startsWith("Deleted") ||
                    line.startsWith("No ") ||
                    line.contains("Failed") ||
                    line.contains("Downloaded file: ") ||
                    line.contains("Upload OK")) {
                break;
            }

        }
        if (!hasResponse) {
            System.out.println("Server response is empty");
        }
    }

    private void handleGet(String fileName) throws IOException {

        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("Invalid file name");
            return;
        }

        String response = in.readLine();
        System.out.println("Server: " + response);

        if (!"OK".equals(response)){
            System.out.println("Download failed");
            return;
        }

        long size = dataIn.readLong();
        File file = new File(Constants.CLIENT_ROOT + File.separator + fileName);

        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file)) {

        byte[] buffer = new byte[1024];
        int bytesRead;
        long total = 0;

        while (total < size && (bytesRead = dataIn.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
            total += bytesRead;
        }
        }
        System.out.println(fileName + "Downloaded: " + fileName);

    }

    private void handlePut(String fileName) throws IOException {

        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("No file name provided");
            return;
        }
        File file = new File(Constants.CLIENT_ROOT + File.separator + fileName);

        if (!file.exists() || file.isDirectory()) {
            System.out.println("File does not exist");
            return;
        }
        String response = in.readLine();

        if (!"READY".equals(response)) {
            System.out.println("Server response is empty");
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
        String finalResponse = in.readLine();
        System.out.println(finalResponse);

    }
    private void handleDelete(String arg) throws IOException {

    }

}











