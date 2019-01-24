package bg.sofia.uni.fmi.mjt.split.wise;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String name;
    private String username;
    private String password;

    private Map<String, Double> friends;
    private Map<String, Map<String, Double>> groups;

    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;

        friends = new HashMap<>();
        groups = new HashMap<>();
    }

    public String getPassword() {
        return password;
    }

    public void addFriend() {

    }
}
