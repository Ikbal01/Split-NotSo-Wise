package bg.sofia.uni.fmi.mjt.split.wise;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private String name;
    private String username;
    private String password;
    private Socket socket;
    private PrintWriter writer;

    private Map<User, Double> friends;
    private Map<String, Group> groups;

    private StringBuffer friendsNotifications;
    private StringBuffer groupsNotifications;
    private StringBuffer messages;

    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;

        friends = new ConcurrentHashMap<>();
        groups = new ConcurrentHashMap<>();
        friendsNotifications = new StringBuffer();
        groupsNotifications = new StringBuffer();
        messages = new StringBuffer();
    }

    public String getPassword() {
        return password;
    }

    public void addFriend(User friend) {
        String message;

        if (friend == null) {
            message = "No such user !";
        } else if (friends.containsKey(friend)) {
            message = String.format("%s is already added to friend list!", friend.getUsername());
        } else {
            friends.put(friend, 0.0);

            message = String.format("%s is successfully added to friend list!", friend.getUsername());
        }

        sendActivityMessage(message);
    }

    public void addToGroup(Group group) {
        groups.put(group.getName(), group);
    }

    public void createGroup(String groupName, ArrayList<User> members) {
        new Group(groupName, members, this);
    }

    public void split(User friend, double amount, String reasonForPayment) {
        if (friend == null) {
            sendActivityMessage("No such registered user!");
        } else if (friends.containsKey(friend)) {
            double newAmount = friends.get(friend) + (amount / 2);
            StringBuilder thisMessage = new StringBuilder();
            StringBuilder friendMessage = new StringBuilder();

            friends.put(friend, newAmount);
            thisMessage.append(String.format("You split %.2f lv between you and %s [%s].\n",
                    amount, friend.getName(), reasonForPayment));

            friend.friends.put(this, -newAmount);
            friendMessage.append(String.format("%s split %.2f lv between you and him/her [%s].\n",
                    this.name, amount, reasonForPayment));

            String youOweMessage = String.format("Current status: You owe %.2f lv.", newAmount);
            if (newAmount > 0) {
                thisMessage.append(String.format("Current status: %s owes you %.2f lv.", friend.getName(), newAmount));
                friendMessage.append(youOweMessage);
            } else {
                thisMessage.append(youOweMessage);
                friendMessage.append(String.format("Current status: %s owes you %.2f lv.", this.name, newAmount));
            }
            sendActivityMessage(thisMessage.toString());
            friend.sendActivityMessage(friendMessage.toString());
        } else {
            sendActivityMessage("You do not have such a friend!");
        }
    }

    public void splitGroup(String groupName, double amount, String reasonForPayment) {
        if (groups.containsKey(groupName)) {
            groups.get(groupName).split(this, amount, reasonForPayment);
        } else {
            sendActivityMessage("You are not a member of this group!");
        }
    }

    public void status() {
        StringBuilder status = new StringBuilder();

        status.append("Friends:\n");

        String friendsStatus = friendsStatus();
        if (friendsStatus != null) {
            status.append(friendsStatus);
        } else {
            status.append("You have no obligations.\n");
        }

        status.append("Groups:\n");

        String groupsStatus = groupsStatus();
        if (groupsStatus != null) {
            status.append(groupsStatus);
        } else {
            status.append("You have no obligations.\n");
        }
        status.append("Messages:\n");
        if (messages.length() > 0) {
            status.append(messages.toString());
            messages.setLength(0);
        } else {
            status.append("You have no unread messages\n");
        }
        sendActivityMessage(status.toString());
    }

    private String friendsStatus() {
        StringBuilder status = new StringBuilder();

        for (Map.Entry<User, Double> friend : friends.entrySet()) {
            if (friend.getValue() > 0) {
                status.append(String.format("* %s (%s): Owes you %.2f lv.\n",
                        friend.getKey().getName(), friend.getKey().getUsername(), friend.getValue()));
            } else if (friend.getValue() < 0) {
                status.append(String.format("* %s (%s): You owe %.2f lv.\n",
                        friend.getKey().getName(), friend.getKey().getUsername(), -friend.getValue()));
            }
        }

        return status.length() > 0 ? status.toString() : null;
    }

    private String groupsStatus() {
        StringBuilder status = new StringBuilder();

        for (Group group : groups.values()) {
            status.append(group.getStatus(this));
        }

        return status.length() > 0 ? status.toString() : null;
    }

    public void friendPayed(double amount, User friend) {
        if (amount < 0) {
            sendActivityMessage("You can not pay a negative amount of money!");
        } else if (friend == null) {
            sendActivityMessage("No such user !");
        } else if (friends.containsKey(friend)) {
            double newAmount = friends.get(friend) - amount;

            friends.put(friend, newAmount);
            sendActivityMessage(String.format("%s (%s) payed you %.2f lv.",
                    friend.getName(), friend.getUsername(), amount));

            friend.friends.put(this, -newAmount);
            friend.sendActivityMessage(String.format("%s (%s) approved your payment %.2f lv",
                    name, username, amount));
        } else {
            sendActivityMessage("You do not have such a friend!");
        }
    }

    public void groupFriendPayed(double amount, User user, String groupName) {
        if (groups.containsKey(groupName)) {
            if (user != null) {
                groups.get(groupName).pay(user, this, amount);
            } else {
                sendActivityMessage("No such user !\n");
            }
        } else {
            sendActivityMessage("There is not such a group!\n");
        }
    }

    public void sendFriend(User friend, String message) {
        if (friend != null) {
            if (friends.containsKey(friend)) {
                friend.sendMessage(this, message);
            } else {
                sendActivityMessage(String.format("You have no friend named %s!\n", friend.getName()));
            }
        } else {
            sendActivityMessage("No such user!\n");
        }
    }

    public void sendAllFriends(String message) {
        for (User friend : friends.keySet()) {
            friend.sendMessage(this, message);
        }
    }

    public void sendGroup(String groupName, String message) {
        if (groups.containsKey(groupName)) {
            for (User member : groups.get(groupName).getMemebers()) {
                member.sendMessage(this, message);
            }
        } else {
            sendActivityMessage("You are not a member of this group!\n");
        }
    }

    public synchronized void sendFriendsNotifications(String message) {
        if (socket == null) {
            friendsNotifications.append(message);
        } else {
            writer.println(message);
        }
    }

    public synchronized void sendGroupsNotifications(String message) {
        if (socket == null) {
            groupsNotifications.append(message);
        } else {
            writer.println(message);
        }
    }

    public synchronized void sendActivityMessage(String message) {
        try {
            writer.println(message);
        } catch (Exception e) {
            System.out.println("An error occurred while sending the activity message!");
        }
    }

    public synchronized void sendMessage(User from, String message) {
        String resultMessage = String.format("%s (%s): %s\n",
                from.getName(), from.getUsername(), message);

        if (socket == null) {
            messages.append(resultMessage);
        } else {
            writer.println(resultMessage);
        }
    }

    public void readNotifications() {
        StringBuilder notifiaction = new StringBuilder();

        notifiaction.append("*** Notifications ***\n");
        notifiaction.append("Friends:\n");
        notifiaction.append(readFriendsNotifications());
        notifiaction.append("Groups:\n");
        notifiaction.append(readGroupsNotifications());
        notifiaction.append("Messages:\n");
        notifiaction.append(readMessages());

        writer.println(notifiaction.toString());
    }

    private String readFriendsNotifications() {
        String notifications = friendsNotifications.toString();
        friendsNotifications.setLength(0);
        return notifications;
    }

    private String readGroupsNotifications() {
        String notifications = groupsNotifications.toString();
        groupsNotifications.setLength(0);
        return notifications;
    }

    private String readMessages() {
        String messages = this.messages.toString();
        this.messages.setLength(0);
        return messages;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getWriter() {
        return writer;
    }
}
