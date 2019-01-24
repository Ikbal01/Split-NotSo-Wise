package bg.sofia.uni.fmi.mjt.split.wise;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SplitNotSoWiseServer {

    private static final int SERVER_PORT = 8080;

    private Map<String, User> users;

    public static void main(String[] args) {
        SplitNotSoWiseServer server = new SplitNotSoWiseServer();
        server.run();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                if (reader.readLine().equals("login")) {
                    String username = reader.readLine();
                    String password = reader.readLine();

                    loginUser(username, password);

                } else if (reader.readLine().equals("register")) {
                    String name = reader.readLine();
                    String username = reader.readLine();
                    String password = reader.readLine();

                    registerUser(name, username, password);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean registerUser(String name, String username, String password) {
        if (!users.containsKey(username)) {
            User user = new User(name, username, password);
            users.put(username, user);

            return true;
        }
        return false;
    }

    private boolean loginUser(String username, String password) {
        if (users.containsKey(username)) {
            if (users.get(username).getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    private void readRegisteredUsers() {
        users = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("resources/username_pass.txt"))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(" ");

            }
        } catch (IOException e) {

        }
    }
}
