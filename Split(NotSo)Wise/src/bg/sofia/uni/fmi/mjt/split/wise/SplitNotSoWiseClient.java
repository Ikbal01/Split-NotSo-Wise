package bg.sofia.uni.fmi.mjt.split.wise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SplitNotSoWiseClient {

    private static final int PORT = 8080;

    private Scanner scanner = new Scanner(System.in);

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private boolean loggedIn;
    private boolean connected;

    public SplitNotSoWiseClient() {
        loggedIn = false;
        connected = false;
    }

    public static void main(String[] args) {
        SplitNotSoWiseClient client = new SplitNotSoWiseClient();
        client.run();
    }

    public void run() {
        connect();
        String input;

        while (connected) {
            System.out.println("Enter \"register\", \"login\" or \"quit\" please");

            input = scanner.nextLine();

            switch (input) {
                case "register":
                    register();
                    break;
                case "login":
                    login();
                    break;
                case "quit":
                    disconnect();
                    break;
                default:
                    System.out.println("wrong command!");
                    break;
            }

            if (loggedIn) {
                new Thread(new ServerResponsePrinter(socket)).start();
                sendCommands();
            }
        }
    }

    private void sendCommands() {
        String command = scanner.nextLine();

        while (!command.equals("logout")) {
            writer.println(command);
            command = scanner.nextLine();
        }
        writer.println(command);
        loggedIn = false;
        disconnect();
        connect();
    }

    private void register() {
        System.out.print("name: ");
        String name = scanner.nextLine();
        System.out.print("username: ");
        String username = scanner.nextLine();
        System.out.print("password: ");
        String password = scanner.nextLine();

        writer.printf("register\n%s\n%s\n%s\n", name, username, password);

        try {
            if (reader.readLine().equals("true")) {
                loggedIn = true;
                System.out.println("You have registered successfully");
            } else {
                System.out.println("This username is already taken !");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void login() {
        System.out.print("username: ");
        String username = scanner.nextLine();
        System.out.print("password: ");
        String password = scanner.nextLine();

        writer.printf("login\n%s\n%s\n", username, password);

        try {
            if (reader.readLine().equals("true")) {
                loggedIn = true;
                System.out.println("You have been successfully logged in");
            } else {
                System.out.println("Wrong username or password !");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void disconnect() {
        try {
            socket.close();
            connected = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void connect() {
        try {
            socket = new Socket("localhost", PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("successfully connected to the server");
            connected = true;

        } catch (IOException e) {
            System.out.println("=> cannot connect to server try again later !");
        }
    }
}
