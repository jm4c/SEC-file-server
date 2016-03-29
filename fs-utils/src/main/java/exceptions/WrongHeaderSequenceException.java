package exceptions;

public class WrongHeaderSequenceException extends Exception {

    private static final long serialVersionUID = 1L;

    public WrongHeaderSequenceException() {
    }

    public WrongHeaderSequenceException(String message) {
        super(message);
    }
}
