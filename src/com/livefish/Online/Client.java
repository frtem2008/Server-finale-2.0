package com.livefish.Online;

import java.io.Closeable;
import java.io.IOException;

/**
 * Connected client representation type
 */
public class Client implements Closeable {
    /**
     * Unique phone id (from registration or login)
     * For server
     */
    public final int id;
    public final ClientRoot root;
    public final Thread clientThread;
    /**
     * Connection to communicat through
     */
    private final Connection connection;

    public Client(Connection connection) {
        this.clientThread = null;
        this.connection = connection;
        this.root = ClientRoot.UNAUTHORIZED;
        this.id = -1;
    }

    public Client(Connection connection, int id, ClientRoot root, Thread clientThread) {
        this.connection = connection;
        this.id = id;
        this.root = root;
        this.clientThread = clientThread;
    }

    @Override
    public String toString() {
        if (root == ClientRoot.ADMIN)
            return "Admin{id=" + id + "}(" + getIp() + ")";
        else if (root == ClientRoot.CLIENT)
            return "Client{id=" + id + "}(" + getIp() + ")";
        else
            return "Unauthorized client(" + getIp() + ")";
    }

    public String getIp() {
        return connection.getIp();
    }

    public boolean isAdmin() {
        return root == ClientRoot.ADMIN;
    }

    public boolean isUnauthorized() {
        return root == ClientRoot.UNAUTHORIZED;
    }

    public boolean isClient() {
        return root == ClientRoot.CLIENT;
    }

    public int getId() {
        return id;
    }

    public String readLine() throws IOException {
        return connection.readLine();
    }

    public void writeLine(String msg) throws IOException {
        connection.writeLine(msg);
    }

    @Override
    public void close() throws IOException {
        if (clientThread != null)
            clientThread.interrupt();
        connection.close();
    }
}
