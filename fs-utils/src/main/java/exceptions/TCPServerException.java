package exceptions;

public class TCPServerException extends Exception {
    private static final long serialVersionUID = 1L;

    public TCPServerException() {
    }

    public TCPServerException(String message) {
        super(message);
    }

}
