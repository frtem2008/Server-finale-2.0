import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileClient {
    public static void main(String[] args) {
        try (Connection server = new Connection("127.0.0.1", 26781)) {
            System.out.println(server.readLine());
            server.writeLine("Hi server omegalul!");
            sendFile("Z:/TASK.zip", server);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // sendFile function define here
    private static void sendFile(String path, Connection server) throws IOException {
        int bytes;
        // Open the File where he located in your pc
        File file = new File(path);
        FileInputStream fileInputStream
                = new FileInputStream(file);

        // Here we send the File to Server
        server.writeLong(file.length());
        // Here we  break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            // Send the file to Server Socket
            server.writeBytes(buffer, 0, bytes);
        }
        // close the file here
        fileInputStream.close();
    }
}
