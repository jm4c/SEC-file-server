package sec.filesystem;


import exceptions.IDMismatchException;
import exceptions.InvalidSignatureException;
import exceptions.WrongHeaderSequenceException;
import types.*;

import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

class TCPServerThread {
    private Socket socket;
    TCPServerThread(Socket socket) {
        this.socket = socket;
    }


    void run() {
        try {
            ImplementationBlockServer server = new ImplementationBlockServer();
            ObjectInputStream inFromClient=
                    new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outToClient =
                    new ObjectOutputStream(socket.getOutputStream());
            Message messageToClient = null;
            try {
                Message messageFromClient = (Message) inFromClient.readObject();

                Data_t data = null;
                Sig_t signature = null;
                Pk_t publicKey = null;
                Id_t id = null;

                //TODO
                switch (messageFromClient.getMessageType()) {
                    case PUT_H:
                        data = messageFromClient.getData();
                        server.put_h(data);
                        break;
                    case PUT_K:
                        data = messageFromClient.getData();
                        signature = messageFromClient.getSignature();
                        publicKey = messageFromClient.getPublicKey();
                        server.put_k(data, signature, publicKey);
                        break;
                    case GET:
                        id = messageFromClient.getID();
                        server.get(id);
                        break;
                    default:

                }
            } catch (NoSuchAlgorithmException | IDMismatchException | InvalidKeyException |
                    WrongHeaderSequenceException | InvalidSignatureException | SignatureException |
                    ClassNotFoundException e) {
                e.printStackTrace();
                messageToClient = new Message.MessageBuilder(Message.MessageType.ERROR).error(e.toString()).createMessage();
            }

            //sends message with return to client
            outToClient.writeObject(messageToClient);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


