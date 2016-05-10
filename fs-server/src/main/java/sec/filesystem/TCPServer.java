package sec.filesystem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private static final int PORT = 1099;

    public static void main(String argv[]) {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                assert serverSocket != null;
                Socket clientSocket = serverSocket.accept();
                System.out.println("ACCEPTS CLIENT");

                System.out.println("STARTS THREAD");
                TCPServerThread st = new TCPServerThread(clientSocket);
                st.run();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
