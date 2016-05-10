package sec.filesystem;

import types.Message;

import java.io.*;
import java.net.Socket;

public class TCPClient {

    private static final int PORT = 1099;


    public void sendMessageToServer(Message message){
        BufferedReader inFromUser =
                new BufferedReader( new InputStreamReader(System.in));
        try {
            Socket clientSocket = new Socket("localhost",PORT);
            ObjectInputStream inFromServer=
                    new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream outToServer =
                    new ObjectOutputStream(clientSocket.getOutputStream());

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]){
        Message message = new Message.MessageBuilder(Message.MessageType.RE_GET).data(null).createMessage();
        System.out.println(message.getMessageType());
    }
}
