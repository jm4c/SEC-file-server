package types;

public class Message {

    public enum MessageType {
        DATA, ACK, RE_ACK;
    }

    private String msg;
    private int id;
    private MessageType msgtype;

    public Message(String msg, int id) {
        this.msg = msg;
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getId() {
        return id;
    }

    public MessageType getType() {
        return msgtype;
    }

    public void setType(MessageType type) {
        this.msgtype = type;
    }
}
