package sec.filesystem;

import exceptions.MajorityQuorumTimeoutException;
import exceptions.WrongHeaderSequenceException;
import interfaces.InterfaceBlockServer;
import types.*;
import utils.CryptoUtils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static interfaces.InterfaceBlockServer.REPLICAS;
import static types.Message.MessageType.ACK;
import static types.Message.MessageType.ERROR;


/*  Demo Class used for demonstrating a client connecting to the File Server, 
    and attempting to perform a write request with an old header.

    Supported runtime arguments:
        -more       Shows more detailed information during runtime.
        -log        Shows the logging of relevant exceptions during runtime.
 */
public class DemoInvalidTimeStamp {

    private final static int PORT = 1099;
    static PrintStream dummyStream = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
            //NO-OP
        }
    });
    static PrintStream originalStream = System.out;
    //private static Library c;
    private static TCPClient c;

    private static void swapOutStream(String mode, String[] in) {
        if (mode.equalsIgnoreCase("enable")) {
            System.setOut(originalStream);
            return;
        }
        if (mode.equalsIgnoreCase("disable")) {
            System.setOut(dummyStream);
            for (String s : in) {
                if (s.equalsIgnoreCase("-more")) {
                    System.setOut(originalStream);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            //c = new Library();
            c = new TCPClient();
            Buffer_t buffer = new Buffer_t(CryptoUtils.serialize(""));

            // Initializing the file system
            System.out.println("// [1] Initializing the File System (with a timestamp set in the future...)");
            swapOutStream("disable", args);
            c.setClientID(fs_init_withForgedTimestamp());
            swapOutStream("enable", args);
            System.out.println("// [2] File System has been initialized successfully.");
            System.out.println("// [2] Client ID assigned by the server:\n\t" + c.getClientID().getValue());

            // Writing to the file at position 0
            swapOutStream("disable", args);
            String s = "The quick brown fox jumps over the lazy dog";
            for (int i = 0; i < 7; i++) {
                s = s.concat(s);
            }
            buffer.setValue(CryptoUtils.serialize(s));
            swapOutStream("enable", args);
            System.out.println("// [3] Performing a write request (using a header with a \"newer\" timestamp) ...");
            System.out.println("// [3] Writing some data of size " + buffer.getValue().length + " to the file, at pos 0 ...");
            swapOutStream("disable", args);
            c.fs_write(0, buffer.getValue().length, buffer);
            swapOutStream("enable", args);
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);

        } catch (WrongHeaderSequenceException ex) {
            swapOutStream("enable", args);
            System.err.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoInvalidTimeStamp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has terminated.");
            System.exit(0);

        } catch (Exception ex) {
            swapOutStream("enable", args);
            System.err.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoInvalidTimeStamp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);
        }
    }

    //fs_init where the headers timestamp is a year later, causing remaining fs_writes to throw a wrong header sequence exception
    protected static Id_t fs_init_withForgedTimestamp() throws Exception {

        PrivateKey privateKey = null;
        InterfaceBlockServer server;
        Id_t id = null;

        KeyPair kp = CryptoUtils.setKeyPair();
        c.setPrivateKey(kp);
        c.setPublicKey(kp);
        privateKey = kp.getPrivate();


        //current (empty) header file
        List<Id_t> emptyFileList = new ArrayList<>();
        Header_t header = new Header_t(emptyFileList);
        // sets timestamp in the future


        header.setTimestamp((new Timestamp(System.currentTimeMillis() + 9999999999L)));
        System.out.println(header.getTimestamp().toString());

        Data_t headerData = new Data_t(CryptoUtils.serialize(header));

        Sig_t signature;

        signature = new Sig_t(CryptoUtils.sign(headerData.getValue(), privateKey));


        //REPLICA CODE BLOCK
        for (int i = 0; i < REPLICAS; i++) {
            //Registry myReg = LocateRegistry.getRegistry("localhost");
            System.out.println("Contacting server-" + i);
//            try {
//                server = (InterfaceBlockServer) myReg.lookup("fs.server-" + i);
//            } catch (NotBoundException rme) {
//                System.out.println("server-" + i + " is unresponsive...");
//                continue;
//            }
            //ENDOF REPLICA CODE BLOCK
//            System.out.println(server.greeting() + "\n");

            System.out.println("DATA SENT (empty header): " + header.toString() + "\n");
            //id = server.put_k(headerData, signature, c.getPublicKey());
            //TCP
            Message response = broadcastMessageToServers(new Message.MessageBuilder(Message.MessageType.PUT_K)
                    .data(headerData).signature(signature).publicKey(c.getPublicKey())
                    .createMessage());
            id = response.getID();

            //server.storePubKey(c.getPublicKey());
            //TCP
            broadcastMessageToServers(new Message.MessageBuilder(Message.MessageType.STORE_PK)
                    .publicKey(c.getPublicKey())
                    .createMessage());
        }
        return id;
    }

//    private static Message sendMessageToServer(Message messageToServer) throws Exception {
//        Message messageFromServer;
//        Socket clientSocket = new Socket("localhost", PORT);
//        ObjectOutputStream outToServer =
//                new ObjectOutputStream(clientSocket.getOutputStream());
//        ObjectInputStream inFromServer =
//                new ObjectInputStream(clientSocket.getInputStream());
//        outToServer.writeObject(messageToServer);
//        messageFromServer = (Message) inFromServer.readObject();
//
//        if (messageFromServer.getMessageType().equals(Message.MessageType.ERROR))
//            throw messageFromServer.getException();
//
//        inFromServer.close();
//        outToServer.close();
//        clientSocket.close();
//
//        return messageFromServer;
//    }


    private static Message broadcastMessageToServers(Message messageToServer) throws Exception {
        int majority = REPLICAS / 2 + 1;

        CountDownLatch countDownMajority = new CountDownLatch(majority);

        SendMessageThread[] sendMessageThreads = new SendMessageThread[REPLICAS];
        Thread[] threads = new Thread[REPLICAS];

        for (int i = 0; i < REPLICAS; i++) {
            sendMessageThreads[i] = new SendMessageThread(messageToServer, PORT + i, countDownMajority);
            threads[i] = new Thread(sendMessageThreads[i]);
            threads[i].start();
        }

        //waits for the majority of replicas to reply with ACK, if return is false it timed out
        int timeout = 10;
        if (!countDownMajority.await(timeout, TimeUnit.SECONDS)) {
            for (int i = 0; i < REPLICAS; i++) {
                if (!threads[i].isAlive()) {
                    if (sendMessageThreads[i].getMessageFromServer().getMessageType().equals(ERROR)) {
                        throw sendMessageThreads[i].getMessageFromServer().getException();
                    }
                }
            }
            throw new MajorityQuorumTimeoutException("Majority took too long to respond (" + timeout + "s)");
        }

        //gets first ACKd message and returns it to the client
        for (int i = 0; i < REPLICAS; i++) {
            if (!threads[i].isAlive()) {

                if (sendMessageThreads[i].getMessageFromServer().getMessageType().equals(ACK)) {
                    return sendMessageThreads[i].getMessageFromServer();
                }
            }
        }
        return null;
    }


}
