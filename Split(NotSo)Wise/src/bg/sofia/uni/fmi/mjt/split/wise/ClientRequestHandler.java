package bg.sofia.uni.fmi.mjt.split.wise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientRequestHandler implements Runnable {

    private User user;
    private Socket socket;
    private SplitNotSoWiseServer server;

    public ClientRequestHandler(SplitNotSoWiseServer server, User user, Socket socket) {
        this.server = server;
        this.user = user;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            String command;
            while (true) {
                command = reader.readLine();
                String[] tokens = command.split(" ", 2);

                switch (tokens[0]) {
                    case "add-friend":
                        addFriend(command);
                        break;
                    case "create-group":
                        createGroup(command);
                        break;
                    case "split":
                        split(command);
                        break;
                    case "split-group":
                        splitGroup(command);
                        break;
                    case "get-status":
                        getStatus();
                        break;
                    case "payed":
                        payed(command);
                        break;
                    case "send":
                        send(command);
                        break;
                    case "send-friends":
                        sendFriends(command);
                        break;
                    case "send-group":
                        sendGroup(command);
                        break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFriend(String command) {

    }

    private void createGroup(String command) {

    }

    private void split(String command) {

    }

    private void splitGroup(String command) {

    }

    private void getStatus() {

    }

    private void payed(String command) {

    }

    private void send(String command) {

    }

    private void sendFriends(String command) {

    }

    private void sendGroup(String command) {

    }
}
