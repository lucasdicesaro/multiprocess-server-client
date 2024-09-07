package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

// Handler for clients
public abstract class ClientHandler implements Runnable {

    protected static Set<common.Client> clients = new HashSet<>();

    protected Socket socket;
    protected int clientId;

    public ClientHandler(int clientId, Socket socket) {
        this.clientId = clientId;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);) {

            synchronized (clients) {
                clients.add(new common.Client(clientId, "", socket, -1, out));
            }

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("RECIBIDO: " + message);

                if (message.startsWith("K:/console ")) {
                    System.out.println("Received from Keyboard: " + message);
                    message = message.replace("K:/console ", "");
                    System.out.println("Sending to console: " + message);
                    forwardToConsole(message);
                } else {
                    processMessage(message);
                }
            }

        } catch (IOException e) {
            System.err.println("ClientHandler: " + e.getMessage());
        } finally {
            System.out.println("ClientHandler: cerrando ");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (clients) {
                clients.removeIf(c -> c.getId() == clientId);
                // Se informa al resto de los clientes que clientId se ha desconectado
                // TODO
            }
        }
    }

    protected abstract void processMessage(String message);

    protected void forwardToConsole(String message) {
        // System.out.println("Enviando a console [" + message + "]");
        for (common.Client client : clients) {
            if (client.getId() == common.Client.CONSOLE_ID) {
                // System.out.println("Enviando a [" + client.getId() + "] [" + message + "]");
                client.getWriter().println(message);
                return;
            }
        }
    }
}