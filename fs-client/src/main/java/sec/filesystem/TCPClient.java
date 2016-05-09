package sec.filesystem;

import java.io.*;
import java.net.Socket;

public class TCPClient {

    private static final int PORT = 1099;

    public static void main(String argv[]){
        BufferedReader inFromUser =
                new BufferedReader( new InputStreamReader(System.in));
        try {
            Socket clientSocket = new Socket("localhost",PORT);
            DataOutputStream outToServer =
                    new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String sentence = "hello world!";

            System.out.println("SENDS \"" + sentence + "\" TO SERVER");
            outToServer.writeBytes(sentence + "\n");

            System.out.println("RECEIVES FROM SERVER:" +inFromServer.readLine());

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
