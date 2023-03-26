package com.livefish.Output.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * File loader class
 * Loads a file by name or from file object
 *
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 */
public class FileLoader {

    /**
     * Empty default constructor
     */
    public FileLoader() {

    }

    /**
     * Reads a file from reader to sb
     *
     * @param reader A reader connected to an opened file
     * @param sb     A string builder to read file to
     * @throws IOException exception during file reading (no access, etc)
     */
    private static void read(BufferedReader reader, StringBuilder sb) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            sb.append(line).append(System.lineSeparator());
            line = reader.readLine();
        }
    }

    /**
     * Load file by its name
     *
     * @param name File name to load
     * @return Read file data
     */
    public static String loadFile(String name) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(name))) {
            read(reader, sb);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * Load file from its file variable
     *
     * @param f File object to load from
     * @return Read file data
     */
    public static String loadFile(File f) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            read(reader, sb);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}

