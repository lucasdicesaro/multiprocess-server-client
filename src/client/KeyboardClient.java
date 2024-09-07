package client;

// Client handler for keyboard input
public class KeyboardClient extends common.KeyboardClient {

    public KeyboardClient(String serverAddress, int port) {
        super(serverAddress, port);
    }

    protected void showUsage() {
        System.out.print("Opciones:\n" +
                "/console <message> Imprime el mensaje del teclado en consola.\n" +
                "/server <message> Envia el mensaje al servidor (Solo para clientes).\n" +
                "> ");
    }
}