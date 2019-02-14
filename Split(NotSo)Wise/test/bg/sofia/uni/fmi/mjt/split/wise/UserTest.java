package bg.sofia.uni.fmi.mjt.split.wise;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class UserTest {
    private static final int SERVER_PORT = 8080;

    private static ServerSocket serverSocket;

    private Socket clientSocketOne;
    private PrintWriter clientWriterOne;
    private BufferedReader clientReaderOne;

    private User firstUser;
    private Socket serverSocketOne;
    private PrintWriter serverWriterOne;

    private Socket clientSocketTwo;
    private PrintWriter clientWriterTwo;
    private BufferedReader clientReaderTwo;

    private User secondUser;
    private Socket serverSocketTwo;
    private PrintWriter serverWriterTwo;


    @BeforeClass
    public static void setup() throws IOException {
        serverSocket = new ServerSocket(SERVER_PORT);

    }

    @Before
    public void beforeSetup() throws IOException {

        clientSocketOne = new Socket("localhost", SERVER_PORT);
        clientReaderOne = new BufferedReader(new InputStreamReader(clientSocketOne.getInputStream()));
        clientWriterOne = new PrintWriter(clientSocketOne.getOutputStream(), true);

        serverSocketOne = serverSocket.accept();
        serverWriterOne = new PrintWriter(serverSocketOne.getOutputStream(), true);

        firstUser = new User("Ivan", "ivan12", "IvanPass123");

        firstUser.setSocket(serverSocketOne);
        firstUser.setWriter(serverWriterOne);

        clientSocketTwo = new Socket("localhost", SERVER_PORT);
        clientReaderTwo = new BufferedReader(new InputStreamReader(clientSocketTwo.getInputStream()));
        clientWriterTwo = new PrintWriter(clientSocketTwo.getOutputStream(), true);

        serverSocketTwo = serverSocket.accept();
        serverWriterTwo = new PrintWriter(serverSocketTwo.getOutputStream(), true);

        secondUser = new User("Todor", "todor123", "TodorPass456");

        secondUser.setSocket(serverSocketTwo);
        secondUser.setWriter(serverWriterTwo);
    }

    @Test
    public void testAddFriend() throws IOException {
        User testUser = new User("Pesho", "pesho1234", "pass12334");
        firstUser.addFriend(testUser);

        assertEquals("pesho1234 is successfully added to friend list!", clientReaderOne.readLine());
    }

    @Test
    public void testToAddAlreadyAddedFriend() throws IOException {
        firstUser.addFriend(secondUser);

        assertEquals("todor123 is successfully added to friend list!", clientReaderOne.readLine());

        firstUser.addFriend(secondUser);

        assertEquals("todor123 is already added to friend list!", clientReaderOne.readLine());
    }

    @Test
    public void testToAddNonExistentEser() throws IOException {
        firstUser.addFriend(null);

        assertEquals("No such user !", clientReaderOne.readLine());
    }

    @Test
    public void testCreateGroup() throws IOException {
        ArrayList<User> members = new ArrayList<>();
        members.add(secondUser);

        String groupName = "My Test Group";

        firstUser.createGroup(groupName, members);

        assertEquals("You successfully created a new group.", clientReaderOne.readLine());

        assertEquals("Ivan added you in group \"My Test Group\"", clientReaderTwo.readLine());
    }

    @Test
    public void testSplitWithUnregisteredUser() throws IOException {
        firstUser.split(null, 12.20, "Birthday");

        assertEquals("No such registered user!", clientReaderOne.readLine());
    }

    @Test
    public void testSplitWithNoFriend() throws IOException {
        User notFriend = new User("NotFriend", "notFriendUserName", "pass23242");

        firstUser.split(notFriend, 21.45, "Present");

        assertEquals("You do not have such a friend!", clientReaderOne.readLine());
    }

    @Test
    public void testSplitWithFriend() throws IOException {
        firstUser.addFriend(secondUser);
        clientReaderOne.readLine();

        firstUser.split(secondUser, 25, "Present for Pesho");

        assertEquals("You split 25.00 lv between you and Todor [Present for Pesho].",
                clientReaderOne.readLine());
        assertEquals("Current status: Todor owes you 12.50 lv.",
                clientReaderOne.readLine());

        assertEquals("Ivan split 25.00 lv between you and him/her [Present for Pesho].",
                clientReaderTwo.readLine());
        assertEquals("Current status: You owe 12.50 lv.",
                clientReaderTwo.readLine());
    }

    @Test
    public void testSplitWithAGroupYouAreNotAMemberOf() throws IOException {
        new Group("GroupName", new ArrayList<>(), secondUser);

        firstUser.splitGroup("GroupName", 20, "Velik den");

        assertEquals("You are not a member of this group!", clientReaderOne.readLine());
    }

    @Test
    public void testSplitGroup() throws IOException {
        ArrayList<User> members = new ArrayList<>();
        members.add(secondUser);

        new Group("Ludite", members, firstUser);
        assertEquals("You successfully created a new group.", clientReaderOne.readLine());

        firstUser.splitGroup("Ludite", 15, "14.02");
        assertEquals("You split 15.0 lv between the members of the group.", clientReaderOne.readLine());

        assertEquals("Ivan added you in group \"Ludite\"", clientReaderTwo.readLine());
        assertEquals("Ivan(ivan12) split 15.00 lv in group \"Ludite\"", clientReaderTwo.readLine());
    }

    @Test
    public void testFriendPayed() throws IOException {
        firstUser.addFriend(secondUser);
        firstUser.split(secondUser, 30, "Present");

        assertEquals("todor123 is successfully added to friend list!", clientReaderOne.readLine());
        assertEquals("You split 30.00 lv between you and Todor [Present].", clientReaderOne.readLine());
        assertEquals("Current status: Todor owes you 15.00 lv.", clientReaderOne.readLine());

        firstUser.friendPayed(10.50, secondUser);
        assertEquals("Todor (todor123) payed you 10.50 lv.", clientReaderOne.readLine());

        assertEquals("Ivan split 30.00 lv between you and him/her [Present].", clientReaderTwo.readLine());
    }

    @Test
    public void testNoFriednPayed() throws IOException {
        firstUser.friendPayed(45, secondUser);

        assertEquals("You do not have such a friend!", clientReaderOne.readLine());
    }

    @Test
    public void testNotExistingGroupsFriendPayed() throws IOException {
        firstUser.groupFriendPayed(23.1, secondUser, "NotExistingGroup");

        assertEquals("There is not such a group!", clientReaderOne.readLine());
    }

    @Test
    public void testGroupFriendPayedIfFriendIsNotRegistered() throws IOException {
        firstUser.createGroup("The Group", new ArrayList<>());

        assertEquals("You successfully created a new group.", clientReaderOne.readLine());

        firstUser.groupFriendPayed(12, secondUser, "The Group");
        assertEquals("There is no such user in this group!", clientReaderOne.readLine());
    }

    @Test
    public void testFriendPayedInNegativeAmount() throws IOException {
        firstUser.addFriend(secondUser);
        firstUser.split(secondUser, 20, "Present");
        firstUser.friendPayed(-20, secondUser);

        assertEquals("todor123 is successfully added to friend list!", clientReaderOne.readLine());
        assertEquals("You split 20.00 lv between you and Todor [Present].", clientReaderOne.readLine());
        assertEquals("Current status: Todor owes you 10.00 lv.", clientReaderOne.readLine());

        assertEquals("You can not pay a negative amount of money!", clientReaderOne.readLine());
    }

    @Test
    public void testSendMessageToFriend() throws IOException {
        firstUser.addFriend(secondUser);
        firstUser.sendFriend(secondUser, "How are you?");

        assertEquals("Ivan (ivan12): How are you?", clientReaderTwo.readLine());
    }

    @Test
    public void testSendMessageToNonExistentUser() throws IOException {
        firstUser.sendFriend(null, "Hello");

        assertEquals("No such user!", clientReaderOne.readLine());
    }

    @Test
    public void testSendMessageToNoFriend() throws IOException {
        firstUser.sendFriend(secondUser, "Hello");

        assertEquals("You have no friend named Todor!", clientReaderOne.readLine());
    }
}
