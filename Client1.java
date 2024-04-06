import java.io.BufferedReader; // Importing BufferedReader class for reading character-based input streams
import java.io.IOException; // Importing IOException class for handling input/output (I/O) errors
import java.io.InputStreamReader; // Importing InputStreamReader class to read bytes and decode them into characters
import java.io.PrintWriter; // Importing PrintWriter class for formatted output
import java.net.Socket; // Importing Socket class for client sockets
import java.util.Scanner; // Importing Scanner class for user input

public class Client1 { // Declaration of the Client class

    public static void main(String[] args) { // Declaration of the main method

        System.out.println("The IP Address is: localhost"); // Printing IP Address
        System.out.println("The Server Port Number is: 1"); // Printing Server Port Number
        System.out.println("Do you want to connect? (yes/no)"); // Prompting user to connect
        Scanner scanner = new Scanner(System.in); // Creating Scanner object for user input
        String connectionResponse = scanner.nextLine().trim(); // Reading user input for connection response
        String serverAddress = "localhost"; // Initialising server address
        int serverPort = 1; // Initialising server port number
        BufferedReader input = null; // Initialising BufferedReader for input
        PrintWriter output = null; // Initialising PrintWriter for output

        if (connectionResponse.equalsIgnoreCase("yes")) { // Checking if user wants to connect
            Socket socket = null; // Initialising Socket object
            try {
                socket = new Socket(serverAddress, serverPort); // Creating socket connection to server

                System.out.println("Your connection information: " + socket); // Printing connection information
                System.out.println("To disconnect, enter 'QUIT' at any time"); // Instructions for disconnecting
                System.out.println("To broadcast a message to the whole server, type your message and press enter."); // Instructions for broadcasting message
                System.out.println("To send a private message, use the format 'MSG <recipientID> <message>', and press enter."); // Instructions for sending private message
                System.out.println("To request a list of connected clients, type 'CLIENT LIST'."); // Instructions for requesting client list

                output = new PrintWriter(socket.getOutputStream(), true); // Creating PrintWriter for output
                input = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Creating BufferedReader for input

                String id = input.readLine(); // Reading ID from server
                System.out.println("Received ID from server: " + id); // Printing received ID

                BufferedReader finalInput = input; // Creating BufferedReader object for final input
                PrintWriter finalOutput = output; // Creating PrintWriter object for final output
                Socket finalSocket = socket; // Creating Socket object for final socket
                new Thread(() -> { // Creating a new thread for handling server messages
                    try {
                        String message; // Declaring message variable
                        while ((message = finalInput.readLine()) != null) { // Loop for reading messages from server
                            if (message.equals("Request Denied")) { // Checking if message is "Request Denied"
                                System.out.println("Request for client list denied by the coordinator."); // Printing denial message
                            } else {
                                System.out.println(message); // Printing received message
                            }
                        }
                    } catch (IOException e) {
                        // Ignore IOException when server disconnects
                    } finally {
                        try {
                            if (finalInput != null) finalInput.close(); // Closing final input
                            if (finalOutput != null) finalOutput.close(); // Closing final output
                            if (finalSocket != null) finalSocket.close(); // Closing final socket
                        } catch (IOException e) {
                            // Ignore IOException when closing resources
                        }
                    }
                }).start(); // Starting the thread for handling server messages

                // Listen for user input and send messages to the server
                while (true) { // Loop for user input
                    String message = scanner.nextLine().trim(); // Reading user input
                    if (message.equalsIgnoreCase("QUIT")) { // Checking if user wants to quit
                        output.println("QUIT"); // Sending quit message to server
                        scanner.close(); // Closing scanner
                        System.out.println("Quitting the client..."); // Printing quitting message
                        break; // Exiting loop
                    } else if (message.equalsIgnoreCase("CLIENT LIST")) { // Checking if user wants client list
                        output.println("CLIENT LIST"); // Sending client list request to server
                    } else {
                        output.println(message); // Sending user message to server
                    }
                }

            } catch (IOException error) { // Handling IO Exception
                System.out.println("Error: " + error.getMessage()); // Printing error message
            } finally {
                try {
                    if (input != null) input.close(); // Closing input
                    if (output != null) output.close(); // Closing output
                    if (socket != null) socket.close(); // Closing socket
                } catch (IOException e) {
                    // Ignore IOException when closing resources
                }
            }
        } else {
            System.out.println("No problem. Have a good day."); // Printing farewell message
        }
    }
}
