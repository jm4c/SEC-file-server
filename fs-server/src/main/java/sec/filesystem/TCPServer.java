package sec.filesystem;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient =
                        new DataOutputStream(clientSocket.getOutputStream());

                System.out.println("STARTS THREAD");
                TCPServerThread st = new TCPServerThread(clientSocket);
                st.run();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
