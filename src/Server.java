import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import common.ConsoleClient;
import server.KeyboardClient;
import server.ClientHandler;

public class Server {

    private static final int SERVER_PORT = 12345;
    private static final String LOOPBACK_ADDRESS = "localhost";
    private static final AtomicInteger clientIdCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        ExecutorService clientHandlerExecutor = Executors.newFixedThreadPool(10);
        ExecutorService consoleInputHandlerExecutor = Executors.newFixedThreadPool(2);

        ServerSocket serverSocket = null;
        ServerSocket miniServerSocket = null;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            int miniServerPort = nextFreePort();
            miniServerSocket = new ServerSocket(miniServerPort);
            serverSocket.setReuseAddress(true); // Enable reuse of address

            System.out.println("Server started.");

            // Start the input handling thread as a client
            consoleInputHandlerExecutor.submit(new KeyboardClient(LOOPBACK_ADDRESS, miniServerPort));
            Socket inputSocket = miniServerSocket.accept();
            clientHandlerExecutor.submit(new ClientHandler(common.Client.KEYBOARD_ID, inputSocket));

            // Start the console output thread as a client
            consoleInputHandlerExecutor.submit(new ConsoleClient(LOOPBACK_ADDRESS, miniServerPort));
            Socket consoleSocket = miniServerSocket.accept();
            clientHandlerExecutor.submit(new ClientHandler(common.Client.CONSOLE_ID, consoleSocket));

            // Accept client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Se conecto uno");
                int clientId = clientIdCounter.getAndIncrement();
                clientHandlerExecutor.submit(new ClientHandler(clientId, clientSocket));
            }

        } catch (IOException e) {
            System.err.println("Main: " + e.getMessage());
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                System.out.println("El servidor se ha desconectado. Saliendo...");
                try {
                    serverSocket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } else if (serverSocket != null) {
                System.out.println("Socket cerrado - Proceso interrumpido por el cliente. Saliendo...");
            }
            if (miniServerSocket != null && !miniServerSocket.isClosed()) {
                try {
                    miniServerSocket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            clientHandlerExecutor.shutdown();
            consoleInputHandlerExecutor.shutdown();
            System.out.println("Main: cerrando todo");
        }
    }

    public static int nextFreePort() throws IOException {
        try (ServerSocket tempSocket = new ServerSocket(0)) {
            return tempSocket.getLocalPort();
        }
    }
}
