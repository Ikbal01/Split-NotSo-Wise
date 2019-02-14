package bg.sofia.uni.fmi.mjt.split.wise;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class SplitNotSoWiseServer {

    private static final int SERVER_PORT = 8080;

    private Map<String, User> users;

    public static void main(String[] args) {
        SplitNotSoWiseServer server = new SplitNotSoWiseServer();
        server.run();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

            loadFromPersistentStorage();
            System.out.println("Server started");

            while (true) {
                Socket socket = serverSocket.accept();

                new Thread(new ClientRequestHandler(this, socket)).start();
            }
        } catch (IOException e) {
            saveToPersistentStorage();
            System.out.println("The server stopped!");
        }
    }

    public void saveToPersistentStorage() {
        Gson gson = new Gson();
        String usersSerialised = gson.toJson(users);

        try {
            OutputStream os = new FileOutputStream("resources\\users.txt");
            os.write(usersSerialised.getBytes());
            os.flush();
            os.close();
            System.out.println("Data saved!\n");

        } catch (IOException e) {
            //TODO: logging
        }
    }

    public void loadFromPersistentStorage() {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(
                    "resources\\users.txt")), StandardCharsets.UTF_8);
            if (jsonString.length() > 0) {
                Gson gson = new Gson();

                users = new Gson().fromJson(
                        jsonString, new TypeToken<ConcurrentHashMap<String, Object>>() {}.getType());
            } else {
                users = new ConcurrentHashMap<>();
            }
        } catch (IOException e) {
            //TODO: ...
        }
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public User getUser(String username) {
        return users.get(username);
    }
}
