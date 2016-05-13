package types;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

public class Message implements Serializable{

    public enum MessageType {
        PUT_K, PUT_H, GET, GET_ID, STORE_PK, LIST_PK,
        VALUE, ACK, ERROR, NC_ACK /*not confirmed ACK*/
    }

    private MessageType messageType;
    private Id_t id;
    private Data_t data;
    private Sig_t signature;
    private Pk_t publicKey;
    private List publicKeyList;
    private Timestamp timestamp;
    private Exception errorMessage;


    private Message(
            final MessageType messageType,
            final Id_t id,
            final Data_t data,
            final Sig_t signature,
            final Pk_t publicKey,
            final List publicKeyList,
            final Timestamp timestamp,
            final Exception errorMessage )
    {
        this.messageType = messageType;
        this.id = id;
        this.data = data;
        this.signature = signature;
        this.publicKey = publicKey;
        this.publicKeyList = publicKeyList;
        this.timestamp = timestamp;
        this.errorMessage = errorMessage;
    }


    public MessageType getMessageType(){
        return this.messageType;
    }

    public Id_t getID(){
        return this.id;
    }

    public Data_t getData(){
        return this.data;
    }

    public Sig_t getSignature(){
        return this.signature;
    }

    public Pk_t getPublicKey(){
        return this.publicKey;
    }

    public List getPublicKeyList(){
        return this.publicKeyList;
    }

    public void setMessageType(MessageType messageType){
        this.messageType=messageType;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public Exception getException(){
        return this.errorMessage;
    }

    public static class MessageBuilder
    {
        private MessageType messageType;
        private Id_t id;
        private Data_t data;
        private Sig_t signature;
        private Pk_t publicKey;
        private List publicKeyList;
        private Timestamp timestamp;
        private Exception errorMessage;

        public MessageBuilder (MessageType messageType){
            this.messageType = messageType;
        }

        public MessageBuilder id(Id_t id){
            this.id = id;
            return this;
        }

        public MessageBuilder data(Data_t data){
            this.data = data;
            return this;
        }

        public MessageBuilder signature(Sig_t signature){
            this.signature = signature;
            return this;
        }

        public MessageBuilder publicKey(Pk_t publicKey){
            this.publicKey = publicKey;
            return this;
        }
        public MessageBuilder list(List publicKeyList){
            this.publicKeyList = publicKeyList;
            return this;
        }

        public MessageBuilder timestamp(Timestamp timestamp){
            this.timestamp = timestamp;
            return this;
        }

        public MessageBuilder error(Exception errorMessage){
            this.errorMessage = errorMessage;
            return this;
        }

        public Message createMessage()
        {
            return new Message(messageType, id, data, signature, publicKey, publicKeyList, timestamp, errorMessage);

        }
    }

}
