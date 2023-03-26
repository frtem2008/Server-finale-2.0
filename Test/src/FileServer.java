import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

public class FileServer {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(26781)) {
            Connection client = new Connection(server);
            client.writeLine("Hi!");
            System.out.println(client.readLine());
            receiveFile("RECEIVED.zip", client);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // receive file function is start here

    private static void receiveFile(String fileName, Connection client) throws IOException {
        int bytes;
        FileOutputStream fileOutputStream
                = new FileOutputStream(fileName);

        long size
                = client.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
                && (bytes = client.readBytes(
                buffer, 0,
                (int) Math.min(buffer.length, size)))
                != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        // Here we received file
        System.out.println("File is Received");
        fileOutputStream.close();
    }
}
