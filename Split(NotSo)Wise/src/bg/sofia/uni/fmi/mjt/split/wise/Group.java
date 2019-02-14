package bg.sofia.uni.fmi.mjt.split.wise;

import java.util.ArrayList;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Group {
    private String name;
    private ConcurrentHashMap<User, ConcurrentHashMap<User, Double>> members;

    public Group(String name, ArrayList<User> members, User creator) {
        this.name = name;
        initMembers(members, creator);
    }

    public String getStatus(User user) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<User, Double> currUser : members.get(user).entrySet()) {
            if (currUser.getValue() > 0) {
                builder.append(String.format("* %s (%s): Owes you %.2f lv.\n",
                        currUser.getKey().getName(), currUser.getKey().getUsername(), currUser.getValue()));
            } else {
                builder.append(String.format("* %s (%s): You owe %.2f lv.\n",
                        currUser.getKey().getName(), currUser.getKey().getUsername(), -currUser.getValue()));
            }
        }

        return builder.toString();
    }

    public void split(User splitter, double amount, String reasonForPayment) {
        for (Map.Entry<User, ConcurrentHashMap<User, Double>> member : members.entrySet()) {

            if (member.getKey() != splitter) {

                double splittedAmount = -(amount / members.size());

                if (member.getValue().containsKey(splitter)) {
                    splittedAmount -= member.getValue().get(splitter);
                }
                member.getValue().put(splitter, splittedAmount);
                members.get(splitter).put(member.getKey(), splittedAmount);

                member.getKey().sendGroupsNotifications(String.format("%s(%s) split %.2f lv in group \"%s\"",
                        splitter.getName(), splitter.getUsername(), amount, name));
                if (splittedAmount < 0) {
                    member.getKey().sendGroupsNotifications(String.format("Current status: You owe %s %.2f lv",
                            splitter.getName(), -splittedAmount));
                } else {
                    member.getKey().sendGroupsNotifications(String.format("Current status: %s owes you %.2f lv",
                            splitter.getName(), splittedAmount));
                }
            }
        }
        splitter.sendActivityMessage(String.format("You split %s lv between the members of the group.", amount));
    }

    public void pay(User from, User to, double amount) {
        if (amount > 0) {
            if (members.containsKey(from)) {
                if (members.get(to).containsKey(from)) {

                    double newAmount = members.get(to).get(from) - amount;

                    members.get(to).put(from, newAmount);
                    to.sendActivityMessage(String.format("%s (%s) payed you %.2f lv.",
                            from.getName(), from.getUsername(), amount));

                    members.get(from).put(to, -newAmount);
                    from.sendGroupsNotifications(String.format("%s (%s) verified your payment of %.2f lv.",
                            to.getName(), to.getUsername(), amount));

                } else {
                    to.sendActivityMessage("This user does not owe you money!");
                }
            } else {
                to.sendActivityMessage("There is no such user in this group!");
            }

        } else {
            to.sendActivityMessage("You can not pay a negative amount of money!");
        }
    }

    private void initMembers(ArrayList<User> members, User creator) {
        this.members = new ConcurrentHashMap<>();

        for (User member : members) {
            this.members.put(member, new ConcurrentHashMap<>());
            member.addToGroup(this);
            member.sendGroupsNotifications(String.format("%s added you in group \"%s\"", creator.getName(), name));
        }

        this.members.put(creator, new ConcurrentHashMap<>());
        creator.addToGroup(this);
        creator.sendActivityMessage("You successfully created a new group.");
    }

    public String getName() {
        return name;
    }

    public Set<User> getMemebers() {
        return members.keySet();
    }
}
