import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import common.ConsoleClient;
import client.ClientHandler;
import client.KeyboardClient;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String LOOPBACK_ADDRESS = "localhost";

    public static void main(String[] args) {
        ExecutorService clientHandlerExecutor = Executors.newFixedThreadPool(3);
        ExecutorService consoleInputHandlerExecutor = Executors.newFixedThreadPool(2);

        Socket serverConnectionSocket = null;
        ServerSocket miniServerSocket = null;
        try {
            int miniServerPort = nextFreePort();
            miniServerSocket = new ServerSocket(miniServerPort);

            System.out.println("Client started.");

            // Start the input handling thread as a client
            consoleInputHandlerExecutor.submit(new KeyboardClient(LOOPBACK_ADDRESS, miniServerPort));
            Socket inputSocket = miniServerSocket.accept();
            clientHandlerExecutor.submit(new ClientHandler(common.Client.KEYBOARD_ID, inputSocket));

            // Start the console output thread as a client
            consoleInputHandlerExecutor.submit(new ConsoleClient(LOOPBACK_ADDRESS, miniServerPort));
            Socket consoleSocket = miniServerSocket.accept();
            clientHandlerExecutor.submit(new ClientHandler(common.Client.CONSOLE_ID, consoleSocket));

            // Start the conection to server thread as a client
            serverConnectionSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            clientHandlerExecutor.submit(new ClientHandler(common.Client.MAIN_CONNECTION_ID, serverConnectionSocket));

            while (true) {
                // No more client will try to connect.
                miniServerSocket.accept();
            }

        } catch (IOException e) {
            System.err.println("Main: " + e.getMessage());
        } finally {
            if (serverConnectionSocket != null && !serverConnectionSocket.isClosed()) {
                System.out.println("El cliente se ha desconectado. Saliendo...");
                try {
                    serverConnectionSocket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } else if (serverConnectionSocket != null) {
                System.out.println("Socket cerrado - Proceso interrumpido por el servidor. Saliendo...");
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
