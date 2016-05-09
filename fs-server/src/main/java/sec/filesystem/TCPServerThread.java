package sec.filesystem;


import java.io.*;
import java.net.Socket;

class TCPServerThread {
    private Socket socket;

    TCPServerThread(Socket socket) {
        this.socket = socket;
    }


    void run() {
        try {
            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient =
                    new DataOutputStream(socket.getOutputStream());
            outToClient.writeBytes(inFromClient.readLine().toUpperCase()+"\n");


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


