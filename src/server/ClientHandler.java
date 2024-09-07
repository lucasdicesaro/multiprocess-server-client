package server;

import java.net.Socket;

// Handler for clients
public class ClientHandler extends common.ClientHandler {

    public ClientHandler(int clientId, Socket socket) {
        super(clientId, socket);
    }

    private void forwardToAllClients(String message) {
        // System.out.println("Enviando a todos [" + message + "]");
        for (common.Client client : clients) {
            if (client.getId() != common.Client.KEYBOARD_ID && client.getId() != common.Client.CONSOLE_ID) {
                // System.out.println("Enviando a [" + client.getId() + "] [" + message + "]");
                client.getWriter().println(message);
            }
        }
    }

    @Override
    protected void processMessage(String message) {
        System.out.println("Received message: " + message);
        if (message.startsWith("K:/clients ")) {
            // Forward the message to all connected clients
            System.out.println("Received from Keyboard: " + message);
            message = message.replace("K:/clients ", "S:");
            System.out.println("Sending to all client: " + message);
            forwardToAllClients(message);
        } else if (message.startsWith("C:")) {
            System.out.println("Received from Client: " + message);
            message = message.replace("C:", "S:");
            System.out.println("Sending to all client: " + message);
            forwardToAllClients(message);
        } else {
            System.out.println("Received from unknown Client: " + message);
        }
    }
}