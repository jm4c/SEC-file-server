package sec.filesystem;

import interfaces.InterfaceBlockServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private static final int PORT = 1099;

    public static void main(String argv[]) {
        for (int i = 0; i < InterfaceBlockServer.REPLICAS; i++) {
            runServer(PORT + i);
        }
    }

    private static void runServer(int port) {
        System.out.println("Starting Server-" + (port - PORT) + " using port " + port);
        new Thread(() -> {
            ImplementationBlockServer server = null;
            ServerSocket serverSocket = null;
            try {
                server = new ImplementationBlockServer(port - PORT);
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    assert serverSocket != null;
                    Socket clientSocket = serverSocket.accept();
                    TCPServerThread st = new TCPServerThread(server, clientSocket);
                    st.run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
