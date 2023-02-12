package com.livefish.Online;//модуль для облегчения работы с сокетами

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Connection representation class
 * Simplifies online communication (comfortable interface)
 * Implements closable, so can be used in try-catch with resources
 *
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 * @see java.io.Closeable
 */
public class Connection implements Closeable {
    /**
     * A socket to communicate through
     */
    private final Socket socket;

    /**
     * Data reader
     */
    private final BufferedReader reader;

    /**
     * Data writer
     */
    private final BufferedWriter writer;


    /**
     * Is the connection closed
     */
    public boolean closed = false;


    /**
     * Client constructor
     *
     * @param ip   Server ip to connect to
     * @param port Server port
     *
     * @see Socket
     * @see Connection#createReader()
     * @see Connection#createWriter()
     */
    public Connection(String ip, int port) {
        try {
            this.socket = new Socket(ip, port);
            this.reader = createReader();
            this.writer = createWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Server constructor
     *
     * @param server ServerSocket to wait the connection on
     *
     * @see ServerSocket
     * @see Connection#createReader()
     * @see Connection#createWriter()
     */
    public Connection(ServerSocket server) {
        try {
            this.socket = server.accept();
            this.reader = createReader();
            this.writer = createWriter();
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
    private BufferedReader createReader() throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }


    /**
     * Writer creation function
     *
     * @return Created writer
     * @throws IOException Exception during writer creation
     * @see Connection#socket
     * @see BufferedWriter
     */
    private BufferedWriter createWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
            writer.write(msg);
            writer.newLine();
            writer.flush();
        }
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
            return reader.readLine();
        return null;
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
        return "Online.Phone{" +
                "ip=" + getIp() +
                '}';
    }


    /**
     * For try-catch with resources
     *
     * @throws IOException exception during closing (connection is lost, etc)
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
