import java.io.BufferedReader; // Import BufferedReader class to read text from a character-input stream
import java.io.IOException; // Import IOException class to handle input/output exceptions
import java.io.InputStreamReader; // Import InputStreamReader class to read bytes and decode them into characters
import java.io.PrintWriter; // Import PrintWriter class to write formatted representations of objects to a text-output stream
import java.net.ServerSocket; // Import ServerSocket class to implement server sockets
import java.net.Socket; // Import Socket class to implement client sockets
import java.util.ArrayList; // Import ArrayList class to create dynamic arrays
import java.util.List; // Import List interface to create lists that maintain an ordered collection of elements

public class Server0 {
    // Lists to hold information about connected clients
    private List<PrintWriter> clientWriters = new ArrayList<>(); // Stores PrintWriter objects for each client
    private List<Integer> clientIds = new ArrayList<>(); // Stores IDs of connected clients
    private List<Socket> clientSockets = new ArrayList<>(); // Stores Socket objects for each client
    private int nextClientId = 1; // ID to assign to the next connected client
    private int coordinatorClientId = -1; // ID of the coordinator client, initially set to -1 (no coordinator)
    private int clientWhoRequestedList = -1; // Track the ID of the client who requested the client list

    public static void main(String[] args) {
        new Server0().startServer(); // Create a new Server0 instance and start the server
    }

    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(1); // Create a server socket listening on port 1
            System.out.println("Server started. Waiting for clients..."); // Print a message indicating server startup

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept incoming client connection
                System.out.println("A client connected: " + clientSocket); // Print a message indicating a client connection

                int clientId = nextClientId++; // Assign a client ID and increment for the next client
                clientIds.add(clientId); // Add client ID to the list
                clientSockets.add(clientSocket); // Add client socket to the list

                // Create a new thread to handle client communication
                HandleClient clientHandler = new HandleClient(clientSocket, clientId);
                clientHandler.start(); // Start the thread

                // Create a PrintWriter for this client and add it to the list
                PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(clientWriter);
                }

                // If this is the first client, make it the coordinator and inform the coordinator of their position
                if (coordinatorClientId == -1) {
                    coordinatorClientId = clientId;
                    sendCoordinatorMessage("Client " + clientId + " has become the coordinator.");
                }

                // Inform the new client about who the coordinator is
                clientWriter.println("[COORDINATOR] The coordinator is Client " + coordinatorClientId);

                // Notify the coordinator about clients who have joined the server)
                if (coordinatorClientId != clientId) {
                    sendCoordinatorMessage("Client " + clientId + " has joined the server.");
                }
            }
        } catch (IOException error) {
            System.out.println("Error: " + error.getMessage()); // Print error message if server setup fails
        }
    }

    // Method to send a message to the coordinator client
    private void sendCoordinatorMessage(String message) {
        synchronized (clientWriters) {
            // Iterate over client writers to find the coordinator client and send the message
            for (PrintWriter writer : clientWriters) {
                if (clientIds.get(clientWriters.indexOf(writer)) == coordinatorClientId) {
                    writer.println("[COORDINATOR] " + message);
                    break; // Exit the loop after sending the message
                }
            }
        }
    }

    // Method to remove a client from the server
    private void removeClient(PrintWriter clientWriter, int clientId) {
        synchronized (clientWriters) {
            // Remove client's PrintWriter, ID, and socket from respective lists
            clientWriters.remove(clientWriter);
            clientIds.remove((Integer) clientId);
            int index = clientIds.indexOf(clientId);
            if (index != -1) {
                clientSockets.remove(index);
            }

            // If the removed client was the coordinator, assign a new coordinator if available
            if (clientId == coordinatorClientId) {
                if (!clientIds.isEmpty()) {
                    coordinatorClientId = clientIds.get(0); // Assign the next client as coordinator
                    sendCoordinatorMessage("Client " + coordinatorClientId + " has become the new coordinator.");
                } else {
                    coordinatorClientId = -1; // No more clients, reset coordinator
                }
            }
            sendCoordinatorMessage("Client " + clientId + " has left the server."); // Inform all clients about the departure
        }
    }

    // Method to send a private message to a specific client
    private void sendPrivateMessage(String message, int recipientID) {
        synchronized (clientWriters) {
            // Iterate over client writers to find the recipient and send the message
            for (PrintWriter writer : clientWriters) {
                int index = clientWriters.indexOf(writer);
                if (clientIds.get(index) == recipientID) {
                    writer.println(message);
                    break; // Exit the loop after sending the message to the recipient
                }
            }
        }
    }

    // Method to broadcast a message to all clients except the sender
    private void broadcastMessage(String message, int senderID) {
        synchronized (clientWriters) {
            // Iterate over client writers and send the message to all clients except the sender
            for (PrintWriter writer : clientWriters) {
                int index = clientWriters.indexOf(writer);
                if (clientIds.get(index) != senderID) {
                    writer.println(message);
                }
            }
        }
    }

    // Inner class representing a thread to handle communication with a single client
    class HandleClient extends Thread {
        private Socket clientSocket;
        private PrintWriter clientWriter;
        private BufferedReader clientReader;
        private int clientId;

        // Constructor to initialise client socket and ID
        public HandleClient(Socket clientSocket, int clientId) {
            this.clientSocket = clientSocket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Initialise BufferedReader to read from client
                clientWriter = new PrintWriter(clientSocket.getOutputStream(), true); // Initialise PrintWriter to write to client

                clientWriter.println("Your ID is: " + clientId); // Send the client its ID

                String message;
                while ((message = clientReader.readLine()) != null) { // Continuously read messages from the client
                    if (message.equalsIgnoreCase("QUIT")) { // If client sends "QUIT", remove it from the server
                        removeClient(clientWriter, clientId);
                        break; // Exit the loop after removing the client
                    } else if (message.equalsIgnoreCase("CLIENT LIST")) {
                        handleClientListRequest(clientId); // Handle client's request for the client list
                    } else if (clientId == coordinatorClientId && (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("no"))) {
                        handleCoordinatorDecision(message); // Handle coordinator's decision for requests
                    } else {
                        handleClientMessage(message, clientId); // Handle regular client message
                    }
                }

                // Properly close resources and stop the client's console thread
                clientReader.close(); // Close the input stream
                clientWriter.close(); // Close the output stream
                clientSocket.close(); // Close the socket
                interrupt(); // Stop the client's console thread
            } catch (IOException e) {
                e.printStackTrace(); // Print the stack trace if an IOException occurs
            }
        }

        // Method to handle different types of client messages
        private void handleClientMessage(String message, int senderID) {
            if (message.startsWith("MSG ")) { // If message starts with "MSG ", it's a private message
                String[] parts = message.split(" ", 3);
                if (parts.length == 3) {
                    try {
                        int recipientID = Integer.parseInt(parts[1]); // Extract recipient ID from the message
                        String privateMessage = "[Private from " + senderID + "] " + parts[2]; // Construct private message
                        sendPrivateMessage(privateMessage, recipientID); // Send the private message to the recipient
                    } catch (NumberFormatException e) {
                        sendCoordinatorMessage("Invalid recipient ID."); // Inform coordinator about invalid recipient ID
                    }
                } else {
                    sendCoordinatorMessage("Invalid private message format."); // Inform coordinator about invalid private message format
                }
            } else {
                broadcastMessage("[Broadcast from " + senderID + "] " + message, senderID); // Broadcast regular message to all clients
            }
        }

        // Method to handle client's request for the client list
        private void handleClientListRequest(int clientId) {
            if (clientId == coordinatorClientId) { // If the client is coordinator, send the client list directly
                sendClientList(clientId);
            } else {
                sendCoordinatorMessage("Client " + clientId + " requests the client list. Do you approve? (yes/no)"); // Prompt coordinator for approval
                // Store the client ID who requested the list
                clientWhoRequestedList = clientId;
            }
        }

        // Method to handle coordinator's decision for client list requests
        private void handleCoordinatorDecision(String decision) {
            if (decision.equalsIgnoreCase("yes")) {
                if (clientWhoRequestedList != -1) {
                    sendClientList(clientWhoRequestedList); // Send the client list to the client who requested
                    clientWhoRequestedList = -1; // Reset the value after sending the list
                }
            } else {
                sendCoordinatorMessage("Request Denied"); // Inform coordinator about denial of request
                if (clientWhoRequestedList != -1) {
                    sendPrivateMessage("Request Denied", clientWhoRequestedList); // Inform client about denial of request
                    clientWhoRequestedList = -1; // Reset the value upon denial
                }
            }
        }

        // Method to send the client list to a specific client
        private void sendClientList(int clientId) {
            StringBuilder clientList = new StringBuilder("[CLIENT LIST]\n");
            for (int i = 0; i < clientIds.size(); i++) {
                clientList.append("Client ID: ").append(clientIds.get(i)); // Append client ID
                clientList.append(", IP Address: ").append(clientSockets.get(i).getInetAddress().getHostAddress()); // Append client IP address
                clientList.append(", Port Number: ").append(clientSockets.get(i).getPort()).append("\n"); // Append client port number
            }
            sendPrivateMessage(clientList.toString(), clientId); // Send the client list to the specified client
        }
    }
}
