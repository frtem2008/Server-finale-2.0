package com.livefish.Online;

import com.livefish.Output.Console.Logger;
import com.livefish.Output.Console.OutputColor;
import com.livefish.Output.Files.FileLoader;
import com.livefish.Output.Files.FileLogger;
import com.livefish.Output.Files.LogFileType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.ServerSocket;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;


/**
 * com.livefish.Main server class (Singleton)
 * Online communication, requests and console commands handling
 *
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 */
public class Server {
    /**
     * Singleton instance field
     */
    private static Server instance = null;
    /**
     * Scanner instance for console
     */
    private final Scanner input;
    /**
     * List of all client threads for proper thread management
     */
    private final List<Thread> clientThreads;
    /**
     * Global server run variable
     */
    private boolean run = true;
    /**
     * Logger instance for console
     */
    private Logger logger;
    /**
     * File logger instance for file operations (saving data, global file management)
     */
    private FileLogger fileLogger;
    /**
     * Set of all connectedClients in the moment
     */
    private Set<Client> connectedClients;
    /**
     * Set of all registered ids (updates from file on every server start)
     */
    private Set<Integer> allIds;
    /**
     * Set of all online ids
     */
    private Set<Integer> onlineIds;
    /**
     * Set of all online admin ids
     */
    private Set<Integer> adminIds;
    /**
     * Set of all online client ids
     */
    private Set<Integer> clientIds;
    /**
     * Set of all requests in process (before the result is known)
     */
    private Set<Request> tempRequests;

    /**
     * Configure and start a server
     *
     * @see Server#getInstance()
     * @see Server#createSets()
     * @see Server#initLogger(boolean)
     * @see Server#initFileLogger()
     * @see Server#setIdCount()
     * @see Server#fillArrays()
     * @see Server#startConsole()
     * @see Server#startServer()
     */
    private Server() {
        input = new Scanner(System.in);
        clientThreads = new ArrayList<>();
        createSets();

        initLogger(true);
        initFileLogger();
        setIdCount();
        fillArrays();
        startConsole();
        startServer();
    }


    /**
     * Starts a server only once (Singleton)
     *
     * @return Single server instance
     *
     * @see Server#instance
     * @see Server#Server()
     */
    public static Server getInstance() {
        if (instance == null)
            instance = new Server();
        return instance;
    }

    /**
     * Date formatting function for logging
     *
     * @param date Date to format
     *
     * @return Formatted date DD.MM.YYYY[HH:MM:SS]
     *
     * @see LocalDateTime
     */
    private static String formatDate(LocalDateTime date) {
        int year = date.getYear();
        String res;

        String month = date.getMonthValue() < 10 ? "0" + date.getMonthValue() : String.valueOf(date.getMonthValue());
        String day = date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : String.valueOf(date.getDayOfMonth());
        String hours = date.getHour() < 10 ? "0" + date.getHour() : String.valueOf(date.getHour());
        String min = date.getMinute() < 10 ? "0" + date.getMinute() : String.valueOf(date.getMinute());
        String sec = date.getSecond() < 10 ? "0" + date.getSecond() : String.valueOf(date.getSecond());

        res = day + "." + month + "." + year + "[" + hours + ":" + min + ":" + sec + "]";
        return res;
    }

    /**
     * Asks a console user, if they want to use colored console output
     *
     * @return True if colored text will be used in console
     *
     * @see Server#input
     * @see Logger#print(String, String)
     * @see Logger#addPrintColor(String, OutputColor)
     */
    private boolean useColoredText() {
        System.out.println("Do you want to use colored console? (true/false)");
        boolean res = input.nextBoolean();
        input.nextLine();

        return res;
    }

    /**
     * Creates all needed sets for server (SHOULD NOT BE USED TWICE)
     *
     * @see Server#connectedClients
     * @see Server#allIds
     * @see Server#onlineIds
     * @see Server#adminIds
     * @see Server#clientIds
     * @see Server#tempRequests
     */
    private void createSets() {
        connectedClients = new HashSet<>();
        allIds = new HashSet<>();
        onlineIds = new HashSet<>();
        adminIds = new HashSet<>();
        clientIds = new HashSet<>();
        tempRequests = new HashSet<>();
    }

    /**
     * Logger initialization and color binding function
     *
     * @param useColorText will color text be used in console output
     *
     * @see Logger
     * @see Logger#addPrintColor(String, OutputColor)
     * @see Logger#print(String, String)
     */
    private void initLogger(boolean useColorText) {
        logger = Logger.getInstance();
        if (useColorText)
            logger.enableColoredText();
        else
            logger.disableColoredText();
        logger.addPrintColor("Connection", OutputColor.GREEN);
        logger.addPrintColor("Disconnection", OutputColor.CYAN);
        logger.addPrintColor("Registration", OutputColor.YELLOW);
        logger.addPrintColor("File creation", OutputColor.BLUE);
        logger.addPrintColor("Error", OutputColor.RED);
        logger.addPrintColor("Wrong data", OutputColor.PURPLE);
        logger.addPrintColor("Server state", OutputColor.GREEN);
    }

    /**
     * File logger initialization and file binding function
     * Creates all needed folders and files, binds them to their names, prints information about every file
     *
     * @see FileLogger
     */
    private void initFileLogger() {
        logger.setOutputColor("File creation");
        logger.print("Attempting to create files:\n");
        fileLogger = new FileLogger("logFolder");
        logger.print("Log dir created in: " + fileLogger.getLogDirPath() + "\n");
        fileLogger.addLogFile("Request file", "req.dat", LogFileType.FINISHED_REQUESTS);
        fileLogger.printFileInfo(logger::print, "Request file");
        fileLogger.addLogFile("Command id file", "commandIDs.dat", LogFileType.COMMAND_IDS);
        fileLogger.printFileInfo(logger::print, "Command id file");
        fileLogger.addLogFile("Connections file", "connectedClients.dat", LogFileType.CONNECTIONS);
        fileLogger.printFileInfo(logger::print, "Connections file");
        fileLogger.addLogFile("Turning on-off file", "on-off.dat", LogFileType.ON_OFF);
        fileLogger.printFileInfo(logger::print, "Turning on-off file");
        fileLogger.addLogFile("Id file", "ids.dat", LogFileType.SAVED_IDS);
        fileLogger.printFileInfo(logger::print, "Id file");
        logger.setDefaultOutputColor();
    }

    /**
     * Reads last finished request id from file
     * It is for keeping request ids unique
     *
     * @see FileLoader#loadFile(File)
     * @see FileLogger#getLogFile(String)
     * @see Request
     * @see Request#setRequestCount(int)
     */
    private void setIdCount() {
        String ids = FileLoader.loadFile(fileLogger.getLogFile("Command id file"));

        if (ids.trim().equals(""))
            Request.setRequestCount(1);
        else {
            String[] idSplit = ids.split("\n");
            ArrayList<Integer> commandIds = new ArrayList<>();

            for (String value : idSplit)
                commandIds.add(Integer.parseInt(value.trim()));
            if (commandIds.size() > 0)
                Request.setRequestCount(commandIds.get(commandIds.size() - 1));
            else
                Request.setRequestCount(1);
        }
    }

    /**
     * Read all registered ids from file
     *
     * @see FileLoader#loadFile(File)
     * @see FileLogger#getLogFile(String)
     * @see Server#allIds
     */
    private void fillArrays() {
        String ids = FileLoader.loadFile(fileLogger.getLogFile("Id file"));
        String[] idSplit = ids.split("\n");
        if (idSplit[0].equals("")) {
            logger.print("No id input to parse\n", "Default");
            return;
        }
        logger.print("Ids read from file: ", "Default");
        for (int i = 0; i < idSplit.length; i++) {
            allIds.add(Integer.parseInt(idSplit[i].trim()));
            //красивый вывод без запятой в конце
            if (i != idSplit.length - 1) {
                System.out.print(Integer.parseInt(idSplit[i].trim()) + ", ");
            } else {
                System.out.print(Integer.parseInt(idSplit[i].trim()));
            }
        }
        logger.println("", "Default");
    }

    /**
     * Starts the server console thread
     *
     * @see Server#serverConsole()
     */
    private void startConsole() {
        new Thread(this::serverConsole, "Console").start();
    }

    /**
     * Starts the main server thread
     *
     * @see Server#server()
     */
    private void startServer() {
        new Thread(this::server, "Server").start();
    }

    /**
     * Stops the server and closes server resources
     *
     * @see Server#writeOnOff(String)
     * @see Server#disconnectIfInactive(Client, Thread)
     * @see Server#input
     */
    private void stopServer() {
        run = false;
        logger.print("Shutting down...", "Disconnection");
        writeOnOff("Off"); //запись в логи
        ArrayList<Client> clients = new ArrayList<>(connectedClients);
        for (Client client : clients) {
            try {
                client.writeLine("SYS$SHUTDOWN");
                disconnectIfInactive(client, client.clientThread);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        connectedClients.clear();
        clients.clear();
        logger.println("Press enter to stop the server", "Default");
        input.close();

        System.exit(0);
    }

    /**
     * Updates all active ids in online ids set
     *
     * @see Server#onlineIds
     * @see Server#connectedClients
     */
    private void refreshActiveIDs() {
        onlineIds.clear();
        connectedClients.forEach(connection -> onlineIds.add(connection.id));
    }

    /**
     * Function for inactive client detection and immediate disconnection (no timeout)
     *
     * @param client  A client to disconnect
     * @param current A current client working thread to interrupt after client will be lost
     *
     * @see Connection
     * @see Server#writeConnection(int, boolean)
     * @see Server#refreshActiveIDs()
     */
    private void disconnectIfInactive(Client client, Thread current) {
        if (client == null || current == null) {
            logger.print("Client to disconnect: " + client + ", current thread: " + current, "Wrong data");
            return;
        }
        try {
            if (client.isUnauthorized())
                logger.println("Unauthorized client from " + client.getIp() + " disconnected", "Disconnection");
            else if (client.isAdmin())
                logger.println("Admin with id " + client.id + " disconnected", "Disconnection");
            else
                logger.println("Client with id " + client.id + " disconnected", "Disconnection");
            writeConnection(client.id, false); //запись в логи
            connectedClients.remove(client); //удаление сокета из списка активных
            client.close();
            current.interrupt(); //остановка потока, обрабатывавшего этот сокет
            refreshActiveIDs();  //обновление базы активных id при отключении
        } catch (IOException e) {
            e.printStackTrace();
            logger.print("FAILED TO CLOSE CLIENT: " + client, "Error");
        }
    }

    /**
     * Gets global ip using http://checkip.amazonaws.com
     *
     * @return Server global ip address
     *
     * @throws IOException URL is not available
     * @see URL
     */
    private String getServerIp() throws IOException {
        URL awsHost = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                awsHost.openStream()));

        return in.readLine(); //you get the IP as a String
    }

    /**
     * com.livefish.Main server thread function
     * Starts the server, waits for clients to complete authorization
     * Handles message cycle with admins and clients
     * Request managing
     *
     * @see Server
     * @see Connection
     * @see Server#connectedClients
     * @see Server#login(Connection)
     * @see Server#refreshActiveIDs()
     * @see Server#stopServer()
     */
    private void server() {
        //порт сервера
        final int SERVER_PORT = 26780;

        try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
            logger.print("Server started with ip: " + getServerIp() + " On port: " + SERVER_PORT + "\n", "Yellow");
            writeOnOff("On");

            while (run) {
                Connection connection = new Connection(server);
                Thread clientThread = new Thread(() -> {
                    Client client = new Client(connection);
                    try {
                        //переменные для вывода и отправки информации
                        String data, root, command, args, success;
                        int adminId, clientToSendId;

                        client = login(connection);
                        connectedClients.add(client);
                        //если логин прошёл успешно
                        if (client == null || client.isUnauthorized() || client.id <= 0)
                            disconnectIfInactive(client, Thread.currentThread());
                        else {
                            onlineIds.add(client.id);
                            if (client.isAdmin()) {
                                adminIds.add(client.id);
                                logger.print("Admin connected: ip address is " + connection.getIp() + ", unique id is " + client.id, "Connection");
                            } else {
                                clientIds.add(client.id);
                                logger.print("Client connected: ip address is " + connection.getIp() + ", unique id is " + client.id, "Connection");
                            }

                            writeConnection(client.id, true);
                            //в бесконечном цикле обрабатываем данные клиента
                            while (!Thread.currentThread().isInterrupted()) {
                                data = client.readLine(); //считывание данных
                                if (data != null) {
                                    String[] split = data.split("\\$"); //чтобы каждый раз не сплитить строку

                                    //сообщение о неверной команде
                                    if (split.length == 0) {
                                        logger.print("Received invalid data from client with id " + client.id, "Wrong data");
                                        client.writeLine("INVALID$DATA$" + data);
                                        continue;
                                    }
                                    root = split[0]; //информация об отправителе(админ/клиент)

                                    if (root.trim().equals("A")) {
                                        //добавление информации об админе
                                        adminId = client.id;

                                        //запись в списки о новом/старом вернувшимся юзере
                                        adminIds.add(client.id);

                                        //получение информации
                                        //TODO: 12.02.2023 НОРМАЛЬНОЕ ПОЛНОЕ ПОЛУЧЕНИЕ ИНФОРМАЦИИ
                                        if (split[1].equals("INFO"))
                                            if (split.length == 3)
                                                getInfo(split[2], client);
                                            else
                                                client.writeLine("INFO$ERROR$INVALID_SYNTAX$" + data);
                                        else {
                                            if (!data.matches("A\\$[\\d]+\\$.+\\$.+")) { //регулярка для обработки
                                                logger.print("Received invalid data from client with id " + client.id, "Wrong data");
                                                connection.writeLine("INVALID$DATA$" + data);
                                                continue;
                                            }
                                            logger.print("___________________________________", "Default");
                                            logger.print("Admin data read: " + data, "Default");
                                            //получение информации о клиенте
                                            clientToSendId = Integer.parseInt(split[1]); //уникальный id клиента
                                            if (clientToSendId == client.id) { //проверка на отправку запроса самому себе
                                                logger.print("Attempt to send request on itself on id: " + client.id, "Wrong data");
                                                connection.writeLine("INVALID$SELF_ID$" + client.id);
                                                continue;
                                            }
                                            if (adminIds.contains(clientToSendId)) {//проверка на отправку запроса другому админу
                                                logger.print("Attempt to send request to admin with id: " + clientToSendId, "Wrong data");
                                                connection.writeLine("INVALID$ADMIN_ID$" + clientToSendId);
                                                continue;
                                            }

                                            logger.print("Id to send: " + clientToSendId, "Default");
                                            logger.print("Id who sent: " + client.id, "Default");

                                            command = split[2]; //сама команда
                                            logger.print("Command to send: " + command, "Default");

                                            args = split[3]; //аргументы команды
                                            logger.print("Args to send: " + args, "Default");

                                            //id запроса выставляется автоматически прямо в конструкторе
                                            Request thisReq = new Request(adminId, clientToSendId, command, args);

                                            //добавление запроса в список запросов и в файл
                                            tempRequests.add(thisReq);

                                            //отправка информации нужному клиенту
                                            refreshActiveIDs();
                                            Client toSend = getClientById(connectedClients, clientToSendId);
                                            if (toSend != null) {
                                                if (allIds.contains(clientToSendId))
                                                    toSend.writeLine(thisReq.id + "$" + command + "$" + args);
                                                else { //ошибка отправки данных незарегистрированному клиенту
                                                    logger.print("Invalid command: this id is free", "Wrong data");
                                                    connection.writeLine("INVALID$FREE$" + clientToSendId);
                                                }
                                            } else { //ошибка отправки оффлайн клиенту
                                                logger.print("Sending error: system didn't find an online client with id " + clientToSendId, "Error");
                                                connection.writeLine("INVALID$OFFLINE_CLIENT$" + clientToSendId);
                                            }
                                        }
                                    } else if (root.trim().equals("C")) { //добавление информации о клиенте
                                        logger.print("___________________________________", "Default");
                                        logger.print("Client data read: " + data, "Default");
                                        if (!data.matches("C\\$[\\d]+\\$[\\d]+\\$.+")) {//регулярка для проверки данных, которые прислал клиент
                                            logger.print("Received invalid data from client with id " + client.id, "Wrong data");
                                            connection.writeLine("INVALID$DATA$" + data);
                                            continue;
                                        }
                                        //получение уникального идентификатора клиента
                                        clientToSendId = Integer.parseInt(split[1]);
                                        clientIds.add(client.id);
                                        logger.print("Client id to send: " + client.id, "Default");

                                        int commandId = Integer.parseInt(split[2]); //id выполненной команды
                                        logger.print("Command id: " + commandId, "Default");

                                        //получение команды, которую выполнял клиент, по её id
                                        Request clientReq = getReqById(tempRequests, commandId);

                                        //получение id админа, отправившего команду
                                        adminId = clientReq.idA;
                                        adminIds.add(adminId);
                                        logger.print("Admin id to send: " + adminId, "Default");

                                        command = clientReq.cmd; //команда, которая была выполнена
                                        logger.print("Command to send: " + command, "Default");

                                        args = clientReq.args; //аргументы команды
                                        logger.print("Args to send: " + args, "Default");

                                        success = split[3]; //успех выполнения (success/no success)
                                        logger.print("Success to send: " + success, "Default");

                                        //формирование ответа админу
                                        String response = clientToSendId + "$" + command + "$" + args + "$" + success;

                                        //обработка команды по id
                                        Request done = getReqById(tempRequests, commandId);
                                        if (done.equals(Request.ZEROREQUEST))
                                            logger.print("Client " + clientToSendId + " wanted to write a zeroRequest", "Wrong data");
                                        else {
                                            //запись запроса в файл
                                            Request mainReq = new Request(done, success);
                                            writeRequest(mainReq);

                                            //удаление запроса из промежуточного списка
                                            tempRequests.remove(done);
                                            //отправка данных о клиенте админу с id aUniId
                                            Client toSend = getClientById(connectedClients, adminId);
                                            if (toSend != null) {
                                                if (allIds.contains(adminId))
                                                    toSend.writeLine(response);
                                                else { //ошибка отправки данных незарегистрированному администратору
                                                    logger.print("Invalid command: this id is free", "Wrong data");
                                                    connection.writeLine("INVALID$FREE$" + adminId);
                                                }
                                            } else { //ошибка отправки данных оффлайн администратору
                                                logger.print("Sending error: system didn't find an online admin with id " + adminId, "Error");
                                                connection.writeLine("INVALID$OFFLINE_ADMIN$" + adminId);
                                            }
                                        }
                                    }

                                    refreshActiveIDs(); //обновление идентификаторов
                                }
                            }
                        }
                    } catch (IOException e) {
                        disconnectIfInactive(client, Thread.currentThread());
                    }
                }, "Client: " + connection.getIp());
                clientThread.start();
                clientThreads.add(clientThread);
            }
        } catch (NullPointerException | IOException e) {
            logger.print("Failed to start a server:\n_________________________", "Error");
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    /**
     * Server console thread
     * User command handling
     *
     * @see Server#input
     * @see Server#allIds
     * @see Server#connectedClients
     */
    private void serverConsole() {
        String userInput;
        ThreadGroup parent = new ThreadGroup("Console threads");

        while (run) {
            userInput = input.nextLine();
            if (userInput.trim().isEmpty())
                continue;
            String finalAction = userInput;
            new Thread(parent, () -> {
                try {
                    switch (finalAction) {
                        case "$shutdown" -> {
                            stopServer();
                        }
                        case "$connections" -> { //вывод списка всех активных подключений
                            if (connectedClients.size() > 0) {
                                logger.print("All active connections: ", "Connection");
                                connectedClients.forEach(client -> logger.print(client.toString(), "Registration"));
                                logger.print(connectedClients.size() + " connections in total\n", "Connection");
                            } else
                                logger.print("No active connections", "Disconnection");
                        }
                        case "$idlist" -> {//вывод всех зарегистрированных id
                            if (allIds.size() == 0)
                                logger.print("No registrated IDs yet", "Disconnection");
                            else {
                                logger.print("All registrated IDs: ", "Default");
                                allIds.forEach(id -> logger.print(String.valueOf(id), "Default"));
                            }
                        }
                        case "$help" -> { //вывод справки
                            logger.print("___________________________________", OutputColor.CYAN);
                            logger.print("Help: \n", OutputColor.CYAN);
                            logger.print("""
                                    $help to show this
                                    $shutdown to shut the server down
                                    $disconnect <int id> to disconnect a client from server
                                    $connectedClients to show all active connectedClients
                                    $idlist to show all registrated ids
                                    $msg <int id> <String message> to send a message to the client
                                    ___________________________________\040
                                    """, OutputColor.CYAN);
                        }
                        default -> {
                            //обработка остальных команд при помощи регулярных выражений
                            if (finalAction.matches("\\$disconnect[ ]*\\d*[ ]*")) { // - /disconnect [int id]
                                if (finalAction.split("\\$disconnect").length > 0) {
                                    int idToDisconnect = Integer.parseInt(finalAction.split("\\$disconnect ")[1]);
                                    refreshActiveIDs();
                                    if (onlineIds.contains(idToDisconnect)) {
                                        getClientById(connectedClients, idToDisconnect).writeLine("SYS$DISCONNECT");
                                        getClientById(connectedClients, idToDisconnect).close();
                                        logger.print("Disconnected client with id " + idToDisconnect + "\n", "Disconnection");
                                        writeConnection(idToDisconnect, false);
                                        connectedClients.remove(getClientById(connectedClients, idToDisconnect));
                                    } else
                                        logger.print("Client with id " + idToDisconnect + " isn't connected", "Wrong data");
                                    if (connectedClients.size() > 0)
                                        logger.print(connectedClients.size() + " connectedClients in total\n", "Connection");
                                    else
                                        logger.print("No active connectedClients", "Disconnection");
                                } else {
                                    if (connectedClients.size() != 0) {
                                        ArrayList<Client> toDisconnect = new ArrayList<>(connectedClients);
                                        int disconnectedClientsCount = toDisconnect.size();
                                        logger.print("Disconnecting " + disconnectedClientsCount + " clients...", "Disconnection");
                                        for (Client client : toDisconnect) {
                                            client.writeLine("SYS$DISCONNECT");
                                            writeConnection(client.id, false);
                                            client.close();
                                        }
                                        clientThreads.forEach(Thread::interrupt);
                                        logger.print("Disconnected " + disconnectedClientsCount + " clients (all)", "Disconnection");
                                        connectedClients.clear();
                                    } else {
                                        logger.print("No active connectedClients", "Disconnection");
                                    }
                                }
                            } else if (finalAction.matches("\\$msg[ ]+[\\d]+[ ]+([\\w][ \\-=*$#]*)+")) { // - /msg <id> <text>
                                if (finalAction.split("\\$msg").length > 0) {
                                    int idToSend = Integer.parseInt(finalAction.split(" ")[1]);
                                    StringBuilder messageText = new StringBuilder();
                                    for (int i = 2; i < finalAction.split(" ").length; i++)
                                        messageText.append(finalAction.split(" ")[i]);

                                    refreshActiveIDs(); //обновление идентификаторов перед отправкой, иначе возможна отправка несуществующему клиенту
                                    if (onlineIds.contains(idToSend)) {
                                        getClientById(connectedClients, idToSend).writeLine("SYS$MSG$" + messageText);
                                        logger.print("Sent message " + messageText + " to client with id: " + idToSend, "Default");
                                    } else
                                        logger.print("Client with id: " + idToSend + " isn't connected", "Wrong data");
                                }
                            } else {
                                //сообщение о неправильном вводе в консоль
                                logger.print("Invalid command", "Wrong data");
                                logger.print("Type $help to show all available commands", OutputColor.CYAN);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, "Server console").start();
        }
    }

    /**
     * Getting all information about a client
     * WORK IN PROGRESS
     *
     * @param command A command to get information about
     * @param client  Client who requested information
     */
    private void getInfo(String command, Client client) {
        //TODO доделать парсер по id
        //command - сообщение после getinfo
        new Thread(() -> {
            String toSend = "INFO$ERROR";

            if (!adminIds.contains(client.id))
                toSend = "INFO$ERROR$ACCESS_DENIED";
            else
                switch (command.toUpperCase(Locale.ROOT)) {
                    case "ONLINE" -> {
                        StringBuffer sendBuffer = new StringBuffer("INFO$ONLINE$");
                        connectedClients.forEach(socket -> sendBuffer.append(socket.getIp()).append(", ").append(socket.id).append(", ").append("root: ").append(adminIds.contains(socket.id) ? "Admin" : "Client").append(";"));
                        if (sendBuffer.charAt(sendBuffer.length() - 1) == ';')
                            sendBuffer.deleteCharAt(sendBuffer.length() - 1);
                        toSend = sendBuffer.toString();
                        //logger.print("Admin with id: " + phone.id + " requested online id list:\n" + onlineIds, "Default");
                    }
                    case "REG" -> {
                        toSend = "INFO$REG$" + allIds;
                        logger.print("Admin with id: " + client.id + " requested registered id list:\n" + allIds, "Default");
                    }
                    case "ADMINS" -> {
                        toSend = "INFO$ADMINS$" + adminIds;
                        logger.print("Admin with id: " + client.id + " requested admin id list:\n" + adminIds, "Default");
                    }
                    case "CLIENTS" -> {
                        toSend = "INFO$CLIENTS$" + clientIds;
                        logger.print("Admin with id: " + client.id + " requested client id list:\n" + clientIds, "Default");
                    }
                    case "HEALTH" -> {
                        StringBuilder res = new StringBuilder();
                        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
                        res.append(String.format("Max heap memory: %.2f GB\n",
                                (double) memoryMXBean.getHeapMemoryUsage().getMax() / 1073741824));
                        res.append(String.format("Used heap memory: %.2f GB\n\n",
                                (double) memoryMXBean.getHeapMemoryUsage().getUsed() / 1073741824));
                        File cDrive = new File("E:/");
                        res.append(String.format("Total disk space: %.2f GB\n",
                                (double) cDrive.getTotalSpace() / 1073741824));
                        res.append(String.format("Free disk space: %.2f GB\n",
                                (double) cDrive.getFreeSpace() / 1073741824));
                        res.append(String.format("Usable disk space: %.2f GB\n\n",
                                (double) cDrive.getUsableSpace() / 1073741824));
                        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

                        for (Long threadID : threadMXBean.getAllThreadIds()) {
                            ThreadInfo info = threadMXBean.getThreadInfo(threadID);
                            res.append('\n').append("Thread name: ").append(info.getThreadName());
                            res.append("Thread State: ").append(info.getThreadState());
                            res.append(String.format("CPU time: %s ns", threadMXBean.getThreadCpuTime(threadID)));
                        }
                        toSend = res.toString();
                        logger.print("SERVER HEALTH: \n" + toSend, "Server state");
                    }
                    default -> {
                        if (command.matches("[\\d]+")) { //получение ip по id
                            int idToSend = Integer.parseInt(command);
                            Client cur = getClientById(connectedClients, idToSend);
                            if (cur != null)
                                toSend = "INFO$IP" + cur.getIp();
                            else
                                toSend = "INFO$ERROR$INVALID_ID$" + idToSend;
                        } else if (command.matches("[\\d]+[ ]+[\\w]+([ ]+[\\d]{2}.[\\d]{2}.[\\d]{4} [\\d]{2}.[\\d]{2}.[\\d]{4})*")) {
                            //TODO cmd information
                            //получение информации о команде
                            ArrayList<String> commands = new ArrayList<>();
                            ArrayList<String> args = new ArrayList<>();
                            ArrayList<String> successes = new ArrayList<>();

                            int id = Integer.parseInt(command.split(" ")[0]);
                            String cmd = command.split(" ")[1];
                            String allCmd = FileLoader.loadFile(fileLogger.getLogFile("Request file"));
                            String[] lines = allCmd.split("\n");
                            for (String line : lines) {
                                commands.add(line.split("\\$")[3]);
                                args.add(line.split("\\$")[4]);
                                successes.add(line.split("\\$")[5]);
                            }
                        } else //сообщение о неправильной команде
                            toSend = "INFO$ERROR$INVALID_SYNTAX$" + command;
                    }
                }

            //отправка сообщения админу
            try {
                client.writeLine(toSend);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Request getting by it's unique id function
     *
     * @param reqSet A set to get request from
     * @param id     A request id to get
     *
     * @return A request from reqSet with unique id id, Request.ZEROREQUEST if request with id id does not exist
     *
     * @see Request
     * @see Request#id
     * @see Request#ZEROREQUEST
     */
    private Request getReqById(Set<Request> reqSet, long id) {
        Request[] res = new Request[]{null};
        reqSet.stream().filter(req -> req.id == id).findFirst().ifPresent(req -> res[0] = req);
        return res[0] == null ? Request.ZEROREQUEST : res[0];
    }

    /**
     * Phone getting by its unique id function
     *
     * @param clientSet A set to get client from
     * @param id        A phone id to get
     *
     * @return A client from clientSet with uniqueId id
     *
     * @see Client
     * @see Client#id
     */
    private Client getClientById(Set<Client> clientSet, long id) {
        Client[] res = new Client[1];
        clientSet.stream().filter(connection -> connection.id == id).findFirst().ifPresent(connection -> res[0] = connection);
        return res[0];
    }

    /**
     * Writing requests to file function
     *
     * @param req A request to write to file
     *
     * @see Request
     * @see Server#formatDate(LocalDateTime)
     * @see FileLogger#logToAll(String, String)
     * @see Server#initFileLogger()
     */
    private void writeRequest(Request req) {
        String writeReq;
        LocalDateTime now = LocalDateTime.now();

        String dateToWrite = formatDate(now);
        if (req.equals(Request.ZEROREQUEST)) {
            logger.print("A try to write a zero request into file", "Wrong data");
        } else { //запись в файл с завершёнными запросами
            writeReq = dateToWrite + "$" + req.idA + "$" + req.idC + "$" + req.cmd + "$" + req.args + "$" + req.success;
            fileLogger.logToAll("Request file", writeReq);
        }
    }

    /**
     * Server powering on / turning off logging function
     *
     * @param onOff Log on if "on", Log off if "off"
     *
     * @see Server#formatDate(LocalDateTime)
     * @see FileLogger#logToAll(String, String)
     */
    private void writeOnOff(String onOff) {
        LocalDateTime now = LocalDateTime.now();
        String normalDate = formatDate(now);
        String toAppend = normalDate + "$" + onOff;
        fileLogger.logToAll(LogFileType.ON_OFF, toAppend);
    }

    /**
     * User connection / disconnection logging function
     *
     * @param clientID  A connected client id
     * @param connected True, if client connected, false if client disconnected
     *
     * @see FileLogger#logToAll(String, String)
     * @see Server#formatDate(LocalDateTime)
     */
    private void writeConnection(int clientID, boolean connected) {
        LocalDateTime now = LocalDateTime.now();
        String normalDate = formatDate(now);
        String toAppend = normalDate + "$" + clientID + "$" + (connected ? 'c' : 'd');
        fileLogger.logToAll("Connections file", toAppend);
    }

    /**
     * Saves last request id to file to save request unique ids
     *
     * @param req A last completed request, it's id will be written to file
     *
     * @see Request
     * @see FileLogger#clearAll(String)
     * @see FileLogger#logToAll(String, String)
     */
    void updateIdCommandsFile(Request req) {
        fileLogger.clearAll("Command id file");
        fileLogger.logToAll("Command id file", String.valueOf(req.id));
    }


    /**
     * Client registration / login function
     * Performs all checks for valid access
     *
     * @param connection A client to register / login
     *
     * @return Array of login results:
     * [0] - client id,
     * [1] - client root (-1 - failed, 1 - Admin, 2 - Client)
     *
     * @see Connection
     * @see Server#allIds
     * @see Server#refreshActiveIDs()
     * @see Server#disconnectIfInactive(Client, Thread)
     * @see Server#onlineIds
     * @see FileLogger#logToAll(String, String)
     */
    private Client login(Connection connection) {
        Client res = new Client(connection);
        //отправка инфы о подключении клиенту
        boolean loginFailed = true;
        ClientRoot root = null;
        int uniId = -1;
        //пароли и тд будут позже
        String dataReceived;
        //цикл входа / регистрации
        do { //пока клиент не зарегается или не войдёт
            try {
                refreshActiveIDs();
                dataReceived = res.readLine(); //чтение id клиента
                if (dataReceived == null) {
                    disconnectIfInactive(res, Thread.currentThread());
                    return res;
                }
                if (dataReceived.split("\\$").length != 2) {
                    logger.print("Received invalid data from: " + res + " data: " + dataReceived, "Wrong data");
                    res.writeLine("LOGIN$INVALID_SYNTAX$" + dataReceived);
                    disconnectIfInactive(res, Thread.currentThread());
                    return res;
                }

                root = dataReceived.split("\\$")[0].equals("A") ? ClientRoot.ADMIN :
                        dataReceived.split("\\$")[0].equals("C") ? ClientRoot.CLIENT :
                                ClientRoot.UNAUTHORIZED;
                uniId = Integer.parseInt(dataReceived.split("\\$")[1]);

                //если id отрицательный, то регистрируем пользователя
                if (uniId <= 0) {
                    if (allIds.contains(-uniId)) {
                        logger.print("The user with id " + (-uniId) + " already exists", "Wrong data");
                        res.writeLine("LOGIN$INVALID_ID$EXISTS$" + (-uniId));
                        continue;
                    }

                    String register = "Successfully registrated new user with root " + root + " and id: " + (-uniId);

                    fileLogger.logToAll("Id file", String.valueOf(-uniId)); //добавление id в список зарегистрированных
                    logger.print(register, "Registration");

                    res.writeLine("LOGIN$CONNECT$" + root + "$" + Math.abs(uniId));
                    allIds.add(Math.abs(uniId)); //добавление нашего id в список
                    break; //окончание цикла входа/регистрации
                } else { //иначе пытаемся войти
                    if (allIds.contains(uniId)) {
                        if (!onlineIds.contains(uniId)) { //id есть в списке, но нет онлайн
                            loginFailed = false;
                            res.writeLine("LOGIN$CONNECT$" + root + "$" + Math.abs(uniId));
                        } else { //id есть в списке и есть онлайн
                            logger.print("Failed to login a user with id " + uniId + ": user with this id has already logged in", "Wrong data");
                            res.writeLine("LOGIN$INVALID_ID$ONLINE$" + (uniId));
                        }
                    } else { //ошибка логина: такого логина пока нет
                        logger.print("Failed to login a user with id " + uniId + ": this id is free", "Wrong data");
                        res.writeLine("LOGIN$INVALID_ID$FREE$" + (uniId));
                        loginFailed = true;
                    }
                }
            } catch (IOException e) {
                disconnectIfInactive(res, Thread.currentThread());
            }
        } while (loginFailed);

        if (uniId != -1)
            res = new Client(
                    connection,
                    Math.abs(uniId),
                    root, Thread.currentThread());
        return res;
    }
}

/**
 * Class for unfinished request handling
 * Is used for saving and manipulating unique id requests
 *
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 */
class Request {
    /**
     * Special invalid 'zero' request
     */
    public static final Request ZEROREQUEST = new Request(0, 0, "0", "0");

    /**
     * Total request count (is used as a unique request id), is restored from file
     */
    private static long requestCount = 1; //Количество запросов

    /**
     * Request command
     */
    public String cmd;
    /**
     * Request command arguments
     */
    public String args;
    /**
     * Request command completion success
     */
    public String success;
    /**
     * Admin id who sent the command
     */
    public int idA;
    /**
     * Client id who attempted to do the command
     */
    public int idC;
    /**
     * Request unique id
     */
    public long id;


    /**
     * Request construction function
     * Is used for temporary request storage (a request is still in process)
     *
     * @param idA  Admin id who sent the command
     * @param idC  Client id who attempted to do the command
     * @param cmd  Request command
     * @param args Request command arguments
     */
    public Request(int idA, int idC, String cmd, String args) {
        this.cmd = cmd;
        this.args = args;
        this.success = "NaN";
        this.idC = idC;
        this.idA = idA;
        requestCount++;
        this.id = requestCount;
    }


    /**
     * Request construction function
     * Is used for long-time storage (a completed request)
     * Saves a completed request to a file
     *
     * @param what    A request to set success
     * @param success Command execution result
     *
     * @see Server#updateIdCommandsFile(Request)
     */
    public Request(Request what, String success) {
        this.idA = what.idA;
        this.idC = what.idC;
        this.cmd = what.cmd;
        this.args = what.args;
        this.id = what.id;

        this.success = success;
        Server.getInstance().updateIdCommandsFile(this);
    }

    /**
     * Sets a request count to some value
     *
     * @param c New request count, should be read from file and updated every time the request is done
     */
    public static void setRequestCount(int c) {
        Logger.getInstance().print("Request count set to " + c + "\n", "Default");
        requestCount = c;
    }
}