import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerClientTest {

    @Test
    public void testClientListRequestAndDenial() {
        try (ServerSocket serverSocket = new ServerSocket(8080);
             Socket clientSocket = new Socket("localhost", 8080);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Simulate the server denying the client list request
            new Thread(() -> {
                try {
                    Socket serverConnection = serverSocket.accept();
                    BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
                    PrintWriter serverOut = new PrintWriter(serverConnection.getOutputStream(), true);
                    assertEquals("CLIENT LIST REQUEST", serverIn.readLine());
                    serverOut.println("Request Denied");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Send a message to the server requesting client list
            out.println("CLIENT LIST REQUEST");

            // Simulate server's response
            assertEquals("Request Denied", in.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testClientConnectionAndDisconnection() {
        try (ServerSocket serverSocket = new ServerSocket(8080);
             Socket clientSocket = new Socket("localhost", 8080)) {

            // Simulate client connecting and disconnecting from the server
            assertTrue(clientSocket.isConnected());

            clientSocket.close();

            // Check if client is disconnected
            assertTrue(clientSocket.isClosed());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}