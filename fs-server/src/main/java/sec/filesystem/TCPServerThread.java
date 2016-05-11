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
import java.util.List;

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

                Data_t data;
                Sig_t signature;
                Pk_t publicKey;
                Id_t id;

                //TODO
                switch (messageFromClient.getMessageType()) {
                    case PUT_H:
                        data = messageFromClient.getData();
                        id = server.put_h(data);
                        messageToClient = new Message.MessageBuilder(Message.MessageType.RE_PUT)
                                .id(id)
                                .createMessage();
                        break;
                    case PUT_K:
                        data = messageFromClient.getData();
                        signature = messageFromClient.getSignature();
                        publicKey = messageFromClient.getPublicKey();
                        id = server.put_k(data, signature, publicKey);
                        messageToClient = new Message.MessageBuilder(Message.MessageType.RE_PUT)
                                .id(id)
                                .createMessage();
                        break;
                    case GET:
                        id = messageFromClient.getID();
                        data = server.get(id);
                        messageToClient = new Message.MessageBuilder(Message.MessageType.RE_GET)
                                .data(data)
                                .createMessage();
                        break;
                    case GET_ID:
                        publicKey = messageFromClient.getPublicKey();
                        id = server.getID(publicKey);

                        //RE_PUT since this message only contains ID like all the other responses to put_k or put_h
                        messageToClient = new Message.MessageBuilder(Message.MessageType.RE_PUT)
                                .id(id)
                                .createMessage();
                        break;
                    case STORE_PK:
                        publicKey = messageFromClient.getPublicKey();
                        if(server.storePubKey(publicKey))
                            messageToClient = new Message.MessageBuilder(Message.MessageType.ACK)
                                    .createMessage();
                        else
                            messageToClient = new Message.MessageBuilder(Message.MessageType.NACK)
                                    .createMessage();
                        break;
                    case LIST_PK:
                        List publicKeyList = server.readPubKeys();
                        messageToClient = new Message.MessageBuilder(Message.MessageType.RE_LIST_PK)
                                .list(publicKeyList)
                                .createMessage();
                        break;
                    default:

                }
            } catch (NoSuchAlgorithmException | IDMismatchException | InvalidKeyException
                    | WrongHeaderSequenceException | InvalidSignatureException | SignatureException
                    | ClassNotFoundException e) {
                e.printStackTrace();
                messageToClient = new Message.MessageBuilder(Message.MessageType.ERROR).error(e.toString()).createMessage();
            }

            //sends message with return to client
            outToClient.writeObject(messageToClient);
            inFromClient.close();
            outToClient.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


