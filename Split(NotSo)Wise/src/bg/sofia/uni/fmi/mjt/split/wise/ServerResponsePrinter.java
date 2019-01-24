package bg.sofia.uni.fmi.mjt.split.wise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerResponsePrinter implements Runnable {
    private Socket socket;

    public ServerResponsePrinter(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line = reader.readLine();

            while (!line.equals("logout")) {
                System.out.println(line);
                line = reader.readLine();
            }

            System.out.println("You are logged out");
        } catch (IOException e) {
            System.out.println("connection is interrupted");
        }
    }
}
