package sec.filesystem;

import exceptions.InvalidSignatureException;
import exceptions.WrongHeaderSequenceException;
import interfaces.InterfaceBlockServer;
import types.*;
import types.Message.MessageType;
import utils.CryptoUtils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

import static types.Message.MessageType.ACK;
import static types.Message.MessageType.NC_ACK;

public class SendMessageThread implements Runnable{
    private Message messageToServer;
    private Message messageFromServer;
    private int port;
    private CountDownLatch countdownMajority;


    protected SendMessageThread(Message messageToServer, int port, CountDownLatch countdownMajority){
        this.messageToServer = messageToServer;
        this.port = port;
        this.countdownMajority = countdownMajority;
    }


    @Override
    public void run() {
        System.out.println("Sends message to server" + (port-InterfaceBlockServer.PORT));

        try {
            messageFromServer = TCPClient.sendMessageToServer(messageToServer, port);

            // not confirmed ACK, must test timestamps first (WRITE HEADER FILE)

            switch (messageFromServer.getMessageType()){
                case NC_ACK: //WRITE HEADER FILE
                    if(getTimestampFromData(messageToServer.getData()).equals(messageFromServer.getTimestamp()))
                        messageFromServer.setMessageType(ACK);
                    else
                        throw new WrongHeaderSequenceException("Invalid timestamp");
                    break;
                case VALUE: //READ HEADER FILE
                    verifySignedData(messageFromServer.getData(), messageFromServer.getSignature(), messageFromServer.getPublicKey()); //already throws exception if wrong signature
                    break;
                default:
            }


            if(messageFromServer.getMessageType().equals(ACK))
                countdownMajority.countDown(); //valid ACK

            System.out.println("Receives message from server" + (port-InterfaceBlockServer.PORT));

        } catch (Exception e) {
            e.printStackTrace();
            messageFromServer = new Message.MessageBuilder(MessageType.ERROR)
                    .error(e)
                    .createMessage();
        }
    }

    private Timestamp getTimestampFromData(Data_t data) throws IOException, ClassNotFoundException {
        return ((Header_t) CryptoUtils.deserialize(data.getValue())).getTimestamp();
    }

    protected Message getMessageFromServer(){
        return messageFromServer;
    }

    private void verifySignedData(Data_t data, Sig_t signature, Pk_t public_key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidSignatureException {
        if (!CryptoUtils.verify(data.getValue(), public_key.getValue(), signature.getValue())) {
            throw new InvalidSignatureException("Invalid signature.");
        }
    }
}
