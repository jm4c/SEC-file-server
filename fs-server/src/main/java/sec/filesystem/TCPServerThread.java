package sec.filesystem;


import blocks.PublicKeyBlock;
import exceptions.IDMismatchException;
import exceptions.InvalidSignatureException;
import exceptions.WrongHeaderSequenceException;
import types.*;
import utils.CryptoUtils;

import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.List;

import static types.Message.*;
import static types.Message.MessageType.*;

class TCPServerThread {
    private Socket socket;
    private ImplementationBlockServer server;
    TCPServerThread(ImplementationBlockServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }


    void run() {
        try {
            ObjectOutputStream outToClient =
                    new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inFromClient=
                    new ObjectInputStream(socket.getInputStream());

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
                        messageToClient = new MessageBuilder(ACK)
                                .id(id)
                                .createMessage();
                        break;
                    case PUT_K:
                        data = messageFromClient.getData();
                        signature = messageFromClient.getSignature();
                        publicKey = messageFromClient.getPublicKey();
                        id = server.put_k(data, signature, publicKey);
                        messageToClient = new MessageBuilder(NC_ACK)
                                .id(id)
                                .timestamp(getTimestampFromFile(id))
                                .createMessage();
                        break;
                    case GET:
                        id = messageFromClient.getID();
                        PublicKeyBlock pkb = getPublicKeyBlock(id);
                        if (pkb != null){
                            messageToClient = new MessageBuilder(VALUE)
                                    .data(pkb.getData())
                                    .signature(pkb.getSig())
                                    .publicKey(pkb.getPKey())
                                    .createMessage();
                        }else {
                            data = server.get(id);
                            messageToClient = new MessageBuilder(ACK)
                                    .data(data)
                                    .createMessage();
                        }
                        break;
                    case GET_ID:
                        publicKey = messageFromClient.getPublicKey();
                        id = server.getID(publicKey);
                        messageToClient = new MessageBuilder(ACK)
                                .id(id)
                                .createMessage();
                        break;
                    case STORE_PK:
                        publicKey = messageFromClient.getPublicKey();
                        server.storePubKey(publicKey);
                        messageToClient = new MessageBuilder(ACK)
                                .createMessage();
                        break;
                    case LIST_PK:
                        List publicKeyList = server.readPubKeys();
                        messageToClient = new MessageBuilder(ACK)
                                .list(publicKeyList)
                                .createMessage();
                        break;
                    default:
                        System.out.println(messageFromClient.getMessageType().toString() + "is not a valid message type.");

                }
            } catch (NoSuchAlgorithmException | IDMismatchException | InvalidKeyException
                    | WrongHeaderSequenceException | InvalidSignatureException | SignatureException
                    | ClassNotFoundException | IOException e) {
                e.printStackTrace();
                messageToClient = new MessageBuilder(ERROR)
                        .error(e)
                        .createMessage();
            }finally {
                //sends message with return to client
                outToClient.writeObject(messageToClient);
                inFromClient.close();
                outToClient.close();
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Timestamp getTimestampFromFile(Id_t id) throws IOException, ClassNotFoundException, InvalidKeyException, InvalidSignatureException, NoSuchAlgorithmException, SignatureException, IDMismatchException {
        return ((Header_t) CryptoUtils.deserialize(server.get(id).getValue())).getTimestamp();
    }

    private PublicKeyBlock getPublicKeyBlock(Id_t id) throws IOException, ClassNotFoundException {
        String s = id.getValue();
        FileInputStream fin;
        fin = new FileInputStream("./files/server" + server.getServerID() + "/" + s + ".dat");
        ObjectInputStream ois = new ObjectInputStream(fin);
        Object obj = ois.readObject();
        if (obj instanceof PublicKeyBlock) {
            return (PublicKeyBlock) obj;

        }
        return null;
    }
}


