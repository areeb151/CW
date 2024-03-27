import java.io.*;
import java.net.*;
import java.util.*;

public class Client2 {
    public static void main(String[] args) {
        try {
            System.out.println("Client started");
            System.out.println("IP Address Of The Server: localhost");
            System.out.println("The Port Number Of The Server: 49152");

            System.out.println("Enter the IP Address of the server to connect:");
            Scanner scanner = new Scanner(System.in);
            String ipAddress = scanner.nextLine();

            System.out.println("Enter the Port Number of the server to connect:");
            int port = scanner.nextInt();

            try (Socket soc = new Socket(ipAddress, port)) {
                BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                PrintWriter out = new PrintWriter(soc.getOutputStream(), true);

                String clientId = in.readLine();
                System.out.println("Your client ID from server: " + clientId);

                // Create a thread to handle incoming messages from the server
                Thread serverListener = new Thread(() -> {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                serverListener.start();

                // Read and send user messages to the server
                while (true) {
                    System.out.println("Enter a message to broadcast:");
                    scanner.nextLine(); // Consume the newline character
                    String str = scanner.nextLine();
                    out.println(str);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}