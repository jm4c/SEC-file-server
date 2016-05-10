package types;

import static types.Message.MessageType.GET;

public class Message {

    public enum MessageType {
        //DATA, ACK, RE_ACK;
        PUT_K, PUT_H, GET, RE_PUT, RE_GET, ERROR
    }

    private MessageType messageType;
    private Id_t id;
    private Data_t data;
    private Sig_t signature;
    private Pk_t publicKey;
    private String errorMessage;


    public Message(
            final MessageType messageType,
            final Id_t id,
            final Data_t data,
            final Sig_t signature,
            final Pk_t publicKey,
            final String errorMessage )
    {
        this.messageType = messageType;
        this.id = id;
        this.data = data;
        this.signature = signature;
        this.publicKey = publicKey;
        //TODO HMAC instead of publicKey and signature?

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

    public String getErrorMessage(){
        return this.errorMessage;
    }

    public static class MessageBuilder
    {
        private MessageType messageType;
        private Id_t id;
        private Data_t data;
        private Sig_t signature;
        private Pk_t publicKey;
        private String errorMessage;

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

        public MessageBuilder error(String errorMessage){
            this.errorMessage = errorMessage;
            return this;
        }

        public Message createMessage()
        {
            return new Message(messageType, id, data, signature, publicKey, errorMessage);

        }
    }

}
