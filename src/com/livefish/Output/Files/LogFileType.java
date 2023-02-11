package com.livefish.Output.Files;

/**
 * Log file type enum
 * Represents every needed log file type
 *
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 * @see FileLogger
 */
public enum LogFileType {
    /**
     * A file with finished request data (ids, success, etc)
     */
    FINISHED_REQUESTS,
    /**
     * A file with last done command id
     */
    COMMAND_IDS,
    /**
     * A file with all client connections and disconnections data
     */
    CONNECTIONS,
    /**
     * A file with all server on and off switches
     */
    ON_OFF,
    /**
     * A file with all ids, which were registered on the server
     */
    SAVED_IDS,
}
