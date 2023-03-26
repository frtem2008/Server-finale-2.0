import java.io.*;
import java.net.*;
import java.util.Objects;

/**
 * Connection representation class
 * Simplifies online communication (comfortable interface)
 * Implements closable, so can be used in try-catch with resources
 *
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 * @see Closeable
 */
public class Connection implements Closeable {
    /**
     * A socket to communicate through
     */
    private final Socket socket;

    /**
     * Data reader
     */
    private final DataInputStream reader;

    /**
     * Data writer
     */
    private final DataOutputStream writer;


    /**
     * Is the connection closed
     */
    public boolean closed;


    /**
     * Client constructor
     *
     * @param ip   Server ip to connect to
     * @param port Server port
     * @see Socket
     * @see Connection#createReader()
     * @see Connection#createWriter()
     */
    public Connection(String ip, int port) {
        try {
            this.socket = new Socket(ip, port);
            this.reader = createReader();
            this.writer = createWriter();
            this.closed = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Server constructor
     *
     * @param server ServerSocket to wait the connection on
     * @see ServerSocket
     * @see Connection#createReader()
     * @see Connection#createWriter()
     */
    public Connection(ServerSocket server) {
        try {
            this.socket = server.accept();
            this.reader = createReader();
            this.writer = createWriter();
            this.closed = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reader creation function
     *
     * @return Created reader
     * @throws IOException Exception during reader creation
     * @see Connection#socket
     * @see BufferedReader
     */
    private DataInputStream createReader() throws IOException {
        return new DataInputStream(socket.getInputStream());
    }


    /**
     * Writer creation function
     *
     * @return Created writer
     * @throws IOException Exception during writer creation
     * @see Connection#socket
     * @see BufferedWriter
     */
    private DataOutputStream createWriter() throws IOException {
        return new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Getting ip address in local network
     *
     * @return Ip address in local network
     * @see InetAddress
     */
    public String getIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "Unable to get ip";
    }


    /**
     * Message sending function
     *
     * @param msg A message to send
     * @throws IOException exception during online communication
     * @see Connection#closed
     * @see Connection#writer
     */
    public void writeLine(String msg) throws IOException {
        if (!closed) {
            writer.writeUTF(msg);
            writer.flush();
        } else
            throw new SocketException("Write failed: connection closed");
    }

    public void writeLong(Long l) throws IOException {
        if (!closed) {
            writer.writeLong(l);
            writer.flush();
        } else
            throw new SocketException("Write failed: connection closed");
    }

    public void writeBytes(byte[] bytes, int offset, int len) throws IOException {
        if (!closed) {
            writer.write(bytes, offset, len);
            writer.flush();
        } else
            throw new SocketException("Write failed: connection closed");
    }

    /**
     * Message receiving
     *
     * @return Received message
     * @throws IOException exception during online communication
     * @see Connection#closed
     * @see Connection#reader
     */
    public String readLine() throws IOException {
        if (!closed)
            return reader.readUTF();
        throw new SocketException("Read failed: connection closed");
    }

    public Long readLong() throws IOException {
        if (!closed)
            return reader.readLong();
        throw new SocketException("Read failed: connection closed");
    }

    public int readBytes(byte[] buf, int offset, int len) throws IOException {
        if (!closed)
            return reader.read(buf, offset, len);
        throw new SocketException("Read failed: connection closed");
    }

    /**
     * More comfortable string representation of a connection
     *
     * @return String representation of a connection
     * @see Object#toString()
     * @see Connection#getIp()
     */
    @Override
    public String toString() {
        return "Test.Connection{" +
                "ip=" + getIp() +
                '}';
    }


    /**
     * For try-catch with resources
     *
     * @throws IOException exception during closing (connection is lost, etc.)
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            writer.close();
            reader.close();
            socket.close();
        }
    }


    /**
     * Hash code for proper sets work
     *
     * @return object's hash code
     * @see Object#hashCode()
     * @see Objects#hash(Object...)
     */
    @Override
    public int hashCode() {
        return Objects.hash(socket, reader, writer, closed);
    }

    /**
     * Equals for proper sets work
     *
     * @param x An object to check equality with
     * @return Are this and x equal objects
     * @see Object#equals(Object)
     */
    public boolean equals(Object x) {
        if (x == null || x.getClass() != this.getClass())
            return false;
        if (x == this)
            return true;
        Connection cur = (Connection) x;
        return cur.socket == this.socket &&
                cur.getIp().equals(this.getIp());
    }
}
