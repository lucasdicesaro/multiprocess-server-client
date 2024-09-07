package client;

import java.net.Socket;

// Handler for clients
public class ClientHandler extends common.ClientHandler {

    public ClientHandler(int clientId, Socket socket) {
        super(clientId, socket);
    }

    private void forwardToServer(String message) {
        // System.out.println("Enviando a server [" + message + "]");
        for (common.Client client : clients) {
            if (client.getId() == common.Client.MAIN_CONNECTION_ID) {
                // System.out.println("Enviando a [" + client.getId() + "] [" + message + "]");
                client.getWriter().println(message);
            }
        }
    }

    @Override
    protected void processMessage(String message) {
        if (message.startsWith("K:/server ")) {
            // Forward the message to the server
            System.out.println("Received from Keyboard: " + message);
            message = message.replace("K:/server ", "C:");
            System.out.println("Sending to server: " + message);
            forwardToServer(message);
        } else if (message.startsWith("S:")) {
            System.out.println("Received from Server: " + message);
            message = message.replace("S:", "");
            System.out.println("Sending to console: " + message);
            forwardToConsole(message);
        }
    }
}