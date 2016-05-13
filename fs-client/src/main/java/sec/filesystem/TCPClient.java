package sec.filesystem;

import blocks.PublicKeyBlock;
import exceptions.IDMismatchException;
import exceptions.InvalidSignatureException;
import exceptions.MajorityQuorumTimeoutException;
import exceptions.NullContentException;
import interfaces.InterfaceBlockServer;

import types.*;
import utils.CryptoUtils;
import utils.HashUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static interfaces.InterfaceBlockServer.*;
import static types.Message.*;
import static types.Message.MessageType.*;

public class TCPClient {

    private static final int PORT = 1099;

    private PrivateKey privateKey;
    private Pk_t publicKey;
    private Id_t clientID;


    protected static InterfaceBlockServer server;
    private static final String SERVERLISTPATH = "../fs-utils/serverlist.dat";

    private List fileList;

    protected TCPClient() {

    }


    protected void setClientID(Id_t headerID) throws NoSuchAlgorithmException, IOException {
        this.clientID = headerID;
    }

    protected void setPrivateKey(KeyPair kp) {
        this.privateKey = kp.getPrivate();
    }

    protected void setPublicKey(Pk_t key) {
        this.publicKey = key;
    }

    protected void setPublicKey(KeyPair kp) {
        this.publicKey = new Pk_t(kp.getPublic());
    }

    protected Id_t getClientID() {
        return clientID;
    }

    private PrivateKey getPrivateKey() {
        return privateKey;
    }

    protected Pk_t getPublicKey() {
        return publicKey;
    }

    private void setFileList(List l) {
        fileList = l;
    }

    protected List getFileList() {
        return fileList;
    }

    private byte[][] splitContent(Buffer_t content) {

        byte[][] filesArray = new byte[(int) Math.ceil(content.getValue().length / (double) BLOCK_MAX_SIZE)][];

        int ptr = 0;

        for (int i = 0; i < filesArray.length - 1; i++) {
            filesArray[i] = Arrays.copyOfRange(content.getValue(), ptr, ptr + BLOCK_MAX_SIZE);
            ptr += BLOCK_MAX_SIZE;
        }
        filesArray[filesArray.length - 1] = Arrays.copyOfRange(content.getValue(), ptr, content.getValue().length);

        return filesArray;

    }

    private Buffer_t joinContent(byte[][] filesArray) {

        byte[] b = new byte[(filesArray.length - 1) * BLOCK_MAX_SIZE + filesArray[filesArray.length - 1].length];
        int ptr = 0;

        for (byte[] filesArray1 : filesArray) {
            System.arraycopy(filesArray1, 0, b, ptr, filesArray1.length);
            ptr += filesArray1.length;
        }

        return new Buffer_t(b);

    }

    protected Id_t fs_init() throws Exception {
        KeyPair kp = CryptoUtils.setKeyPair();
        setPrivateKey(kp);
        setPublicKey(kp);

        //current (empty) header file
        List<Id_t> emptyFileList = new ArrayList<>();
        Header_t header = new Header_t(emptyFileList);
        Data_t headerData = new Data_t(CryptoUtils.serialize(header));

        Sig_t signature;
        signature = new Sig_t(CryptoUtils.sign(headerData.getValue(), getPrivateKey()));


        //REPLICA CODE BLOCK
        //  Read the serverList file
        FileInputStream fin = new FileInputStream(SERVERLISTPATH);
        ObjectInputStream ois = new ObjectInputStream(fin);
        ArrayList<String> serverList = (ArrayList) ois.readObject();

        //TODO RMI comments to remove
        //Registry myReg = LocateRegistry.getRegistry("localhost");
        //System.out.println("Contacting fs." + serverName);
//            try {
//                server = (InterfaceBlockServer) myReg.lookup("fs." + serverName);
//                System.out.println(server.greeting() + "\n");
//                System.out.println("DATA SENT (empty header): " + header.toString() + "\n");
//
        //setClientID(server.put_k(headerData, signature, getPublicKey()));
        //TCP
//            Message response = sendMessageToServer(new MessageBuilder(PUT_K)
//                    .data(headerData).signature(signature).publicKey(getPublicKey())
//                    .createMessage(),PORT);
//            setClientID(response.getID());
        Message response = broadcastMessageToServers(new MessageBuilder(PUT_K)
                .data(headerData).signature(signature).publicKey(getPublicKey())
                .createMessage());
        setClientID(response.getID());

        //server.storePubKey(getPublicKey());
        //TCP
//            sendMessageToServer(new MessageBuilder(STORE_PK)
//                    .publicKey(getPublicKey())
//                    .createMessage(),PORT);

        broadcastMessageToServers(new MessageBuilder(STORE_PK)
                .publicKey(getPublicKey())
                .createMessage());


//            } catch (ConnectException rme) {
//                System.out.println("fs." + serverName + " is unresponsive...");
//            }
//ENDOF REPLICA CODE BLOCK


        return getClientID();
    }

    protected int fs_read(Pk_t pk, int pos, int size, Buffer_t contents) throws Exception {
        byte[] buff = new byte[0];


//            Registry myReg = LocateRegistry.getRegistry("localhost");
//            System.out.println("Contacting fs." + serverName);
//            try {
        //server = (InterfaceBlockServer) myReg.lookup("fs." + serverName);

        //Id_t id = server.getID(pk);
        //TCP
//        Message response = sendMessageToServer(new MessageBuilder(GET_ID)
//                .publicKey(pk)
//                .createMessage(),PORT);

        Message response = broadcastMessageToServers(new MessageBuilder(GET_ID)
                .publicKey(pk)
                .createMessage());
        Id_t id = response.getID();

//              Data_t data = server.get(id);
        //TCP
//        response = sendMessageToServer(new MessageBuilder(GET)
//                .id(id)
//                .createMessage(),PORT);
        response = broadcastMessageToServers(new MessageBuilder(GET)
                .id(id)
                .createMessage());

        Data_t data = response.getData();



        @SuppressWarnings("unchecked")
        List<Id_t> originalFileList = ((Header_t) CryptoUtils.deserialize(data.getValue())).getValue();

        byte[][] originalContentParts = new byte[originalFileList.size()][];
        for (int j = 0; j < originalFileList.size(); j++) {
            //originalContentParts[j] = server.get(originalFileList.get(j)).getValue();
            //TCP
//            response = sendMessageToServer(new MessageBuilder(GET)
//                    .id(originalFileList.get(j))
//                    .createMessage(),PORT);
            response = broadcastMessageToServers(new MessageBuilder(GET)
                    .id(originalFileList.get(j))
                    .createMessage());

            originalContentParts[j] = response.getData().getValue();
        }

        //all stored data
        Buffer_t src = joinContent(originalContentParts);

        if (src.getValue().length < pos + size) {
            buff = new byte[src.getValue().length - pos];
        } else {
            buff = new byte[size];
        }

        System.arraycopy(src.getValue(), pos, buff, 0, buff.length);
        contents.setValue(buff);
//            } catch (ConnectException rme) {
//                System.out.println("fs." + serverName + " is unresponsive...");
//            }
//ENDOF REPLICA CODE BLOCK

        return buff.length;
    }

    protected void fs_write(int pos, int size, Buffer_t contents) throws Exception {

        System.out.println("\nNew FS write");
        if (contents == null) {
            throw new NullContentException("Content is null");
        }

        System.out.println(this.getClientID().getValue());

//REPLICA CODE BLOCK
        //  Read the serverList file
        FileInputStream fin = new FileInputStream(SERVERLISTPATH);
        ObjectInputStream ois = new ObjectInputStream(fin);
        ArrayList<String> serverList = (ArrayList) ois.readObject();

        //Registry myReg = LocateRegistry.getRegistry("localhost");
        //System.out.println("Contacting fs." + servername);
//            try {
        //server = (InterfaceBlockServer) myReg.lookup("fs." + servername);
        //Client's ID can only be a header file
        //Data_t data = server.get(this.getClientID());
        //TCP
//        Message response = sendMessageToServer(new MessageBuilder(GET)
//                .id(this.getClientID())
//                .createMessage(),PORT);
        Message response = broadcastMessageToServers(new MessageBuilder(GET)
                .id(this.getClientID())
                .createMessage());

        Data_t data = response.getData();

        if (data == null) {
            throw new NullContentException("data is null");
        }
        //Header file's data is always a list of other files' IDs
        @SuppressWarnings("unchecked")
        List<Id_t> originalFileList = ((Header_t) CryptoUtils.deserialize(data.getValue())).getValue();

        Buffer_t base;

        if (originalFileList.isEmpty()) {
            base = new Buffer_t(new byte[pos + size]);
        } else {
            byte[][] originalContentParts = new byte[originalFileList.size()][];
            for (int i = 0; i < originalFileList.size(); i++) {
                //originalContentParts[i] = server.get(originalFileList.get(i)).getValue();
                //TCP
//                response = sendMessageToServer(new MessageBuilder(GET)
//                        .id(originalFileList.get(i))
//                        .createMessage(),PORT);
                response = broadcastMessageToServers(new MessageBuilder(GET)
                        .id(originalFileList.get(i))
                        .createMessage());
                originalContentParts[i] = response.getData().getValue();
            }
            base = joinContent(originalContentParts);

            //	puts old content into a bigger file
            if (base.getValue().length < pos + size) {
                Buffer_t auxBase = new Buffer_t(new byte[pos + size]);
                System.arraycopy(base.getValue(), 0, auxBase.value, 0, size);
                base = auxBase;
            }
        }
        System.arraycopy(contents.getValue(), 0, base.value, pos, size);

        byte[][] filesArray = splitContent(base);

        List<Id_t> newFileList = new ArrayList<>();
        for (byte[] filesArray1 : filesArray) {
            newFileList.add(new Id_t(HashUtils.hash(filesArray1, null)));
        }

        Header_t header = new Header_t(newFileList);

        Data_t headerData = new Data_t(CryptoUtils.serialize(header));
        Sig_t signature;
        signature = new Sig_t(CryptoUtils.sign(headerData.getValue(), getPrivateKey()));


        //uploads header first to check signature
        //if (!getClientID().equals(server.put_k(headerData, signature, getPublicKey()))) {
        //TCP
//        response = sendMessageToServer(new MessageBuilder(PUT_K)
//                .data(headerData).signature(signature).publicKey(getPublicKey())
//                .createMessage(),PORT);

        response = broadcastMessageToServers(new MessageBuilder(PUT_K)
                .data(headerData).signature(signature).publicKey(getPublicKey())
                .createMessage());
        if (!getClientID().equals(response.getID())){
            throw new IDMismatchException("Client's ID does not match main block ID!");
        }

        //server.storePubKey(getPublicKey());
        //TCP
//        sendMessageToServer(new MessageBuilder(STORE_PK)
//                .publicKey(getPublicKey())
//                .createMessage(),PORT);
        broadcastMessageToServers(new MessageBuilder(STORE_PK)
                .publicKey(getPublicKey())
                .createMessage());


        //uploads contents
        if (originalFileList.isEmpty()) {
            System.out.println("Original it's empty");
            for (int i = 0; i < newFileList.size(); i++) {
                System.out.println("new block! (" + i + ")");
                //System.out.println(server.put_h(new Data_t(filesArray[i])).getValue());
                //TCP
                //response = sendMessageToServer(new MessageBuilder(PUT_H)
                //        .data(new Data_t(filesArray[i]))
                //        .createMessage(),PORT);
                response = broadcastMessageToServers(new MessageBuilder(PUT_H)
                        .data(new Data_t(filesArray[i]))
                        .createMessage());
                System.out.println(response.getID().getValue());
            }
        } else {
            boolean addBlockFlag;
            for (int i = 0; i < newFileList.size(); i++) {
                addBlockFlag = true;
                System.out.println("\nNEW[" + i + "]:" + newFileList.get(i).getValue());

                for (int j = 0; j < originalFileList.size(); j++) {
                    System.out.println("OLD[" + j + "]:" + originalFileList.get(j).getValue());
                    if (originalFileList.get(j).equals(newFileList.get(i))) {
                        addBlockFlag = false;
                        break;
                    }
                }

                if (addBlockFlag) {
                    System.out.println("new block!");
                    //server.put_h(new Data_t(filesArray[i]));
                    //TCP
                    //sendMessageToServer(new MessageBuilder(PUT_H)
                    //        .data(new Data_t(filesArray[i]))
                    //        .createMessage(), PORT);

                    broadcastMessageToServers(new MessageBuilder(PUT_H)
                            .data(new Data_t(filesArray[i]))
                            .createMessage());

                }
            }
        }
        this.setFileList(newFileList);

//            } catch (ConnectException rme) {
//                System.out.println("fs." + servername + " is unresponsive...");
//            }
//ENDOF REPLICA CODE BLOCK

    }

    protected List fs_list() throws Exception {
        List keyList = new ArrayList<>();
//REPLICA CODE BLOCK
        //  Read the serverList file
        FileInputStream fin = new FileInputStream(SERVERLISTPATH);
        ObjectInputStream ois = new ObjectInputStream(fin);
        ArrayList<String> serverList = (ArrayList) ois.readObject();

//            Registry myReg = LocateRegistry.getRegistry("localhost");
        //server = (InterfaceBlockServer) myReg.lookup("fs." + serverName);
        //keyList = server.readPubKeys();
        // TCP
        //Message response = sendMessageToServer(new MessageBuilder(LIST_PK)
        //        .createMessage(), PORT);
        //keyList = response.getPublicKeyList();

        Message response = broadcastMessageToServers(new MessageBuilder(LIST_PK)
                .createMessage());
        keyList = response.getPublicKeyList();
//ENDOF REPLICA CODE BLOCK

        return keyList;
    }


    protected static Message sendMessageToServer(Message messageToServer, int port) throws Exception {

        Message messageFromServer;

        Socket clientSocket = new Socket("localhost", port);
        ObjectOutputStream outToServer =
                new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream inFromServer =
                new ObjectInputStream(clientSocket.getInputStream());
        outToServer.writeObject(messageToServer);
        messageFromServer = (Message) inFromServer.readObject();
        if (messageFromServer.getMessageType().equals(ERROR))
            throw messageFromServer.getException();

        inFromServer.close();
        outToServer.close();
        clientSocket.close();

        return messageFromServer;
    }

    private Message broadcastMessageToServers(Message messageToServer) throws Exception{
        int majority = (int) Math.floor(REPLICAS/2.0+1);

        CountDownLatch countDownMajority = new CountDownLatch(majority);

        SendMessageThread[] sendMessageThreads = new SendMessageThread[REPLICAS];
        Thread[] threads = new Thread[REPLICAS];
        boolean[] ackList = new boolean[REPLICAS];

        for (int i = 0; i< REPLICAS; i++){
            sendMessageThreads[i] = new SendMessageThread(messageToServer, PORT+i, countDownMajority);
            threads[i] = new Thread(sendMessageThreads[i]);
            threads[i].start();
            ackList[i] = false;
        }

        //waits for the majority of replicas to reply with ACK, if return is false it timed out
        int timeout = 60;
        if(!countDownMajority.await(timeout,TimeUnit.SECONDS))
            throw new MajorityQuorumTimeoutException("Majority took too long to respond (" + timeout +"s)");

        //gets first ACKd message and returns it to the client
        for (int i = 0; i< REPLICAS; i++) {
            if(!threads[i].isAlive()){

                if (sendMessageThreads[i].getMessageFromServer().getMessageType().equals(ACK)) {
                    return sendMessageThreads[i].getMessageFromServer();
                }
            }
        }
        return null;
    }








    public static void main(String argv[]) throws Exception {
        TCPClient client = new TCPClient();
        Message message = new MessageBuilder(ACK).createMessage();
        //client.sendMessageToServer(message, PORT);
        client.broadcastMessageToServers(message);
        System.out.println(message.getMessageType());
    }




}
