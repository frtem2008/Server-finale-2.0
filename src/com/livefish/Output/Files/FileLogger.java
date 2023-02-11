package com.livefish.Output.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Log file record
 *
 * @param name A name in FileLogger system
 * @param file A file object to be associated with name name
 * @param type A file type (for server)
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 * @see FileLogger
 * @see FileLoader
 */
record LogFile(String name, File file, LogFileType type) {
    /**
     * Log file construction from name, file and type function
     *
     * @param name Log file name in FileLogger system
     * @param file File object associated with this name
     * @param type File type (for server)
     */
    LogFile {
        try {
            if (file != null && !file.exists())
                if (!file.createNewFile())
                    throw new RuntimeException("Failed to create log file in: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create log file in: " + file.getAbsolutePath() + "\n" + e.getLocalizedMessage());
        }
    }

    /**
     * Clears the file if it is valid
     *
     * @see LogFile#checkAccess()
     * @see FileLogger#clearAll(String)
     * @see FileLogger#clearOne(String)
     */
    public void clear() {
        checkAccess();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Name getter
     *
     * @return File name in File logger system
     */
    public String getName() {
        return name;
    }

    /**
     * Type getter
     *
     * @return File type
     */
    public LogFileType getType() {
        return type;
    }

    /**
     * File getter
     *
     * @return file object of this log file
     */
    public File getFile() {
        return file;
    }

    /**
     * Checks if a file has a name str
     *
     * @param str Another filename to compare with this log file name
     * @return Comparing result
     * @see FileLogger#logToAll(String, String)
     * @see FileLogger#logToOne(String, String)
     * @see FileLogger#clearAll(String)
     * @see FileLogger#clearOne(String)
     */
    public boolean hasName(String str) {
        return name.equals(str);
    }

    /**
     * Checks if a file has type type
     *
     * @param tp Another log file type to compare
     * @return Comparing result
     * @see FileLogger#logToAll(LogFileType, String)
     * @see FileLogger#logToOne(LogFileType, String)
     * @see FileLogger#clearAll(LogFileType)
     * @see FileLogger#clearOne(LogFileType)
     */
    public boolean hasType(LogFileType tp) {
        return type.equals(tp);
    }

    /**
     * Check if a file is valid
     *
     * @return validation result
     * @see LogFile#checkAccess()
     */
    public boolean isValid() {
        return file != null && file.exists() && file.isFile();
    }

    /**
     * Checks if a file is accessible
     *
     * @see LogFile#isValid()
     * @see LogFile#clear()
     * @see LogFile#log(String)
     */
    private void checkAccess() {
        if (!isValid()) {
            if (file == null)
                throw new IllegalStateException("Invalid log file: file is null");
            if (!file.exists())
                throw new IllegalStateException("Invalid log file: " + file.getAbsolutePath() + " does not exist");
            if (!file.isFile())
                throw new IllegalStateException("Invalid log file: " + file.getAbsolutePath() + " is not a file");
        }
    }

    /**
     * Doing something with a file if a predicate is correct function
     *
     * @param predicate A check before action is performed
     * @param action    Something we want to do with LogFile
     * @return always true (for compatibility)
     * @see FileLogger#clearAll(String)
     * @see FileLogger#clearOne(String)
     * @see FileLogger#logToAll(String, String)
     * @see FileLogger#logToOne(String, String)
     */
    public boolean doIf(Predicate<LogFile> predicate, Consumer<LogFile> action) {
        if (predicate.test(this))
            action.accept(this);
        return true;
    }

    /**
     * Doing something with result (true / false) with a file if a predicate is correct function
     *
     * @param predicate A check before action is performed
     * @param action    Something we want to do with LogFile
     * @return result of action done
     * @see LogFile#doIf(Predicate, Consumer)
     */
    public boolean doIf(Predicate<LogFile> predicate, Function<LogFile, Boolean> action) {
        if (predicate.test(this))
            return action.apply(this);
        return false;
    }

    /**
     * Log something to a file function
     * Checks if file is accessible before logging attempt
     *
     * @param str A string to log to file
     * @see FileLogger#log(LogFile, String)
     */
    public void log(String str) {
        checkAccess();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(str + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * File Logger implementation
 * Logs to files by their names in system or types
 * Can log to one or to all of the files with this type or name
 *
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 * @see LogFile
 * @see LogFileType
 */
public class FileLogger {
    /**
     * Set of files and their names in system
     */
    private final Set<LogFile> files;

    /**
     * Absolute path to logging directory
     */
    private final String logDirAbsPath;

    /**
     * FileLogger constructor
     * Creates all needed directories
     * Initializes file set
     *
     * @param logDirPath log folder, where all log files are
     * @see FileLogger#logDirAbsPath
     * @see FileLogger#files
     */
    public FileLogger(String logDirPath) {
        File dir = new File(logDirPath);
        logDirAbsPath = dir.getAbsolutePath() + "\\";
        if (!dir.exists() && !dir.mkdirs())
            throw new RuntimeException("Failed to create logging directory in: " + logDirAbsPath);
        files = new HashSet<>();
    }

    /**
     * Getter for log directory absolute path
     *
     * @return Logging directory absolute path
     * @see FileLogger#logDirAbsPath
     */
    public String getLogDirPath() {
        return logDirAbsPath;
    }

    /**
     * Adds a new LogFile to system
     *
     * @param name     A file name in the system
     * @param fileName File name on a computer
     * @param type     Log file type
     */
    public void addLogFile(String name, String fileName, LogFileType type) {
        files.add(new LogFile(name, new File(logDirAbsPath + fileName), type));
    }

    /**
     * Used privately for getting LogFile by it's name in system
     *
     * @param name LogFile's name in system
     * @return Log File with this name in system
     * @see FileLogger#getLogFile(String)
     */
    private LogFile getLogFileFile(String name) {
        for (LogFile file : files)
            if (file.hasName(name))
                return file;
        throw new RuntimeException("Log file with name: " + name + " not found in file list");
    }

    /**
     * Get a Log File by it's name in the system
     *
     * @param name LogFile's name in system
     * @return Log File with this name in system
     * @see FileLogger#getLogFileFile(String)
     */
    public File getLogFile(String name) {
        return getLogFileFile(name).getFile();
    }

    /**
     * Does something with a string of file info
     *
     * @param printer An action to be done with a string with wile info
     * @param name    A file name in system
     */
    public void printFileInfo(Consumer<String> printer, String name) {
        LogFile f = getLogFileFile(name);
        printer.accept(f.getName() + ": " + f.getFile().getAbsolutePath());
    }

    /**
     * Get log file by it's name in system function
     *
     * @param name Log File name in system
     * @return Absolute path to this file on a computer
     * @see FileLogger#getLogFileFile(String)
     */
    public String getLogFilePath(String name) {
        return getLogFileFile(name).getFile().getAbsolutePath();
    }

    /**
     * Logs a string to a file if predicate is true
     *
     * @param file      A log file ot log to
     * @param predicate A predicate to be true to log something
     * @param str       A string to log to a LogFile
     * @return Logging success
     * @see LogFile#doIf(Predicate, Function)
     */
    private boolean logIf(LogFile file, Predicate<LogFile> predicate, String str) {
        return file.doIf(predicate, (Consumer<LogFile>) logFile -> log(logFile, str));
    }

    /**
     * Does something to all files in files
     *
     * @param action An action to do with every file
     * @see FileLogger#files
     * @see FileLogger#logToAll(String, String)
     * @see FileLogger#logToAll(LogFileType, String)
     * @see FileLogger#clearAll(String)
     * @see FileLogger#clearAll(LogFileType)
     */
    private void forAllFiles(Consumer<LogFile> action) {
        files.forEach(action);
    }

    /**
     * Does something to only one file in files (to the first apply)
     *
     * @param action An action to do with every file until it succeeds
     * @see FileLogger#files
     * @see FileLogger#logToOne(String, String)
     * @see FileLogger#logToOne(LogFileType, String)
     * @see FileLogger#clearOne(String)
     * @see FileLogger#clearOne(LogFileType)
     */
    private void forOneFile(Function<LogFile, Boolean> action) {
        for (LogFile file : files)
            if (action.apply(file))
                return;
    }

    /**
     * Clears all the files of LogFileType type
     *
     * @param type A type of all the files to clear
     * @see FileLogger#forAllFiles(Consumer)
     * @see LogFile#hasType(LogFileType)
     * @see LogFile#doIf(Predicate, Consumer)
     * @see LogFile#clear()
     */
    public void clearAll(LogFileType type) {
        forAllFiles(file -> file.doIf(logFile -> logFile.hasType(type), LogFile::clear));
    }

    /**
     * Clears all the files with name name
     *
     * @param name A name of all the files to clear
     * @see FileLogger#forAllFiles(Consumer)
     * @see LogFile#hasName(String)
     * @see LogFile#doIf(Predicate, Consumer)
     * @see LogFile#clear()
     */
    public void clearAll(String name) {
        forAllFiles(file -> file.doIf(logFile -> logFile.hasName(name), LogFile::clear));
    }

    /**
     * Clears the first file with type type
     *
     * @param type A type of a file to clear
     * @see FileLogger#forOneFile(Function)
     * @see LogFile#hasType(LogFileType)
     * @see LogFile#doIf(Predicate, Consumer)
     * @see LogFile#clear()
     */
    public void clearOne(LogFileType type) {
        forOneFile(file -> file.doIf(logFile -> logFile.hasType(type), LogFile::clear));
    }

    /**
     * Clears the first file with name name
     *
     * @param name A name of a file to clear
     * @see FileLogger#forOneFile(Function)
     * @see LogFile#hasName(String)
     * @see LogFile#doIf(Predicate, Consumer)
     * @see LogFile#clear()
     */
    public void clearOne(String name) {
        forOneFile(file -> file.doIf(logFile -> logFile.hasName(name), LogFile::clear));
    }

    /**
     * Logs to all the files with type type
     *
     * @param type A type of all the files to log to
     * @param str  A string to log to all the files with type type
     * @see LogFile#hasType(LogFileType)
     * @see FileLogger#forAllFiles(Consumer)
     * @see FileLogger#logIf(LogFile, Predicate, String)
     */
    public void logToAll(LogFileType type, String str) {
        forAllFiles(file -> logIf(file, logFile -> logFile.hasType(type), str));
    }

    /**
     * Logs to all the files with name name
     *
     * @param name A name of all the files to log to
     * @param str  A string to log to all the files with type type
     * @see LogFile#hasName(String)
     * @see FileLogger#forAllFiles(Consumer)
     * @see FileLogger#logIf(LogFile, Predicate, String)
     */
    public void logToAll(String name, String str) {
        forAllFiles(file -> logIf(file, logFile -> logFile.hasName(name), str));
    }


    /**
     * Logs to the first of the files with type type
     *
     * @param type A type of the first of the files with type type
     * @param str  A string to log to the first of the files with type type
     * @see LogFile#hasType(LogFileType)
     * @see FileLogger#forOneFile(Function)
     * @see FileLogger#logIf(LogFile, Predicate, String)
     */
    public void logToOne(LogFileType type, String str) {
        forOneFile(file -> logIf(file, logFile -> logFile.hasType(type), str));
    }

    /**
     * Logs to the first of the files with name name
     *
     * @param name A name of the first of the files with name name
     * @param str  A string to log to the first of the files with name name
     * @see LogFile#hasName(String)
     * @see FileLogger#forOneFile(Function)
     * @see FileLogger#logIf(LogFile, Predicate, String)
     */
    public void logToOne(String name, String str) {
        forOneFile(file -> logIf(file, logFile -> logFile.hasName(name), str));
    }

    /**
     * Logs a string to {@link LogFile}
     *
     * @param logFile A {@link LogFile} to log to
     * @param str     A string to log
     * @see LogFile#log(String)
     * @see FileLogger#logToAll(LogFileType, String)
     * @see FileLogger#logToAll(String, String)
     * @see FileLogger#logToOne(LogFileType, String)
     * @see FileLogger#logToOne(String, String)
     */
    public void log(LogFile logFile, String str) {
        logFile.log(str);
    }
}
