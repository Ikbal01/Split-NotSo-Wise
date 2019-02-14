package bg.sofia.uni.fmi.mjt.split.wise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientRequestHandler implements Runnable {

    private User user;
    private Socket socket;
    private SplitNotSoWiseServer server;

    private BufferedReader reader;
    private PrintWriter writer;

    private boolean loggedIn;

    public ClientRequestHandler(SplitNotSoWiseServer server, Socket socket) {
        this.server = server;
        this.socket = socket;

        loggedIn = false;
        initWriterReader();
    }

    @Override
    public void run() {
        try {
            login();
            user.readNotifications();

            String command;
            while (true) {
                command = reader.readLine();
                String[] tokens = command.split(" ", 2);

                switch (tokens[0]) {
                    case "add-friend":
                        addFriend(tokens[1]);
                        break;
                    case "create-group":
                        createGroup(tokens[1]);
                        break;
                    case "split":
                        split(tokens[1]);
                        break;
                    case "split-group":
                        splitGroup(tokens[1]);
                        break;
                    case "get-status":
                        getStatus();
                        break;
                    case "friend-payed":
                        friendPayed(tokens[1]);
                        break;
                    case "group-friend-payed":
                        groupFriendPayed(tokens[1]);
                    case "send-friend":
                        sendFriend(tokens[1]);
                        break;
                    case "send-all-friends":
                        sendAllFriends(tokens[1]);
                        break;
                    case "send-group":
                        sendGroup(tokens[1]);
                        break;
                    case "help":
                        help();
                        break;
                    case "logout":
                        return;
                    default:
                        user.sendActivityMessage("Wrong command!");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void login() {
        try {
            while (!loggedIn) { // && !socket.isClosed()
                String line = reader.readLine();

                if (line != null) {

                    if (line.equals("login")) {
                        String username = reader.readLine();
                        String password = reader.readLine();

                        loggedIn = loginUser(username, password);

                    } else if (line.equals("register")) {
                        String name = reader.readLine();
                        String username = reader.readLine();
                        String password = reader.readLine();

                        loggedIn = registerUser(name, username, password);
                    }

                    writer.println(loggedIn);
                }
            }

            user.setSocket(socket);
            user.setWriter(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean registerUser(String name, String username, String password) {
        if (!server.getUsers().containsKey(username)) {
            user = new User(name, username, password);
            server.getUsers().put(username, user);

            return true;
        }
        return false;
    }

    private boolean loginUser(String username, String password) {
        if (server.getUsers().containsKey(username)) {
            User user = server.getUsers().get(username);

            if (user.getPassword().equals(password)) {
                this.user = user;
                return true;
            }
        }
        return false;
    }

    private void help() {
        StringBuilder help = new StringBuilder();
        help.append("Commands:\n");
        help.append("add-friend \"friend's username\"\n");
        help.append("create-group \"group name\" \"username\"...\n");
        help.append("split \"amount\" \"friend's username\" \"reason for payment\"\n");
        help.append("split-group \"group name\" \"amount\" \"reason for payment\"\n");
        help.append("get-status\n");
        help.append("friend-payed \"amount\" \"friend's username\"\n");
        help.append("group-friend-payed \"amount\" \"username\" \"group name\"\n");
        help.append("send-friend \"friend's username\" \"message\"\n");
        help.append("send-all-friends \"message\"\n");
        help.append("send-group \"group name\" \"message\"\n");

        user.sendActivityMessage(help.toString());
    }

    private void addFriend(String command) {
        user.addFriend(server.getUser(command));
    }

    private void createGroup(String command) {
        try {
            String[] tokens = command.split(" ");
            String groupName = tokens[0];

            ArrayList<User> users = new ArrayList<>();
            for (int i = 1; i < tokens.length; i++) {
                User currUser = server.getUser(tokens[i]);
                if (currUser != null) {
                    users.add(currUser);
                } else {
                    user.sendActivityMessage(String.format("No user named %s!\n", tokens[i]));
                    return;
                }
            }

            user.createGroup(groupName, users);
        } catch (Exception e) {
            user.sendActivityMessage("Wrong command!");
        }
    }

    private void split(String command) {
        try {
            String[] tokens = command.split(" ", 3);

            double amount = Double.parseDouble(tokens[0]);
            String friendUsername = tokens[1];
            String reasonForPayment = tokens[2];

            user.split(server.getUser(friendUsername), amount, reasonForPayment);

        } catch (Exception e) {
            user.sendActivityMessage("Wrong command!");
        }
    }

    private void splitGroup(String command) {
        try {
            String[] tokens = command.split(" ", 3);
            String groupName = tokens[1];
            double amount = Double.parseDouble(tokens[0]);
            String reasonForPayment = tokens[2];

            user.splitGroup(groupName, amount, reasonForPayment);
        } catch (Exception e) {
            user.sendActivityMessage("Wrong command!");
        }
    }

    private void getStatus() {
        user.status();
    }

    private void friendPayed(String command) {
        try {
            String[] tokens = command.split(" ", 2);
            double amount = Double.parseDouble(tokens[0]);
            String userName = tokens[1];

            user.friendPayed(amount, server.getUser(userName));
        } catch (Exception e) {
            user.sendActivityMessage("Wrong command!");
        }
    }

    private void groupFriendPayed(String command) {
        try {
            String[] tokens = command.split(" ", 3);
            double amount = Double.parseDouble(tokens[0]);
            String username = tokens[1];
            String groupName = tokens[2];

            user.groupFriendPayed(amount, server.getUser(username), groupName);
        } catch (Exception e) {
            user.sendActivityMessage("Wrong command!");
        }
    }

    private void sendFriend(String command) {
        try {
            String[] tokens = command.split(" ", 2);
            String friendUserName = tokens[0];
            String message = tokens[1];

            user.sendFriend(server.getUser(friendUserName), message);
        } catch (Exception e) {
            user.sendActivityMessage("Wrong command!");
        }
    }

    private void sendAllFriends(String message) {
        if (message != null && message.length() > 0) {
            user.sendAllFriends(message);
        } else {
            user.sendActivityMessage("Wrong command!");
        }
    }

    private void sendGroup(String command) {
        try {
            String[] tokens = command.split(" ", 2);
            String groupName = tokens[0];
            String message = tokens[1];

            user.sendGroup(groupName, message);
        } catch (Exception e) {
            user.sendActivityMessage("Wrong command!");
        }
    }

    private void initWriterReader() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
