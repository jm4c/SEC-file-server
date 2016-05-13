package exceptions;

public class MajorityQuorumTimeoutException extends Exception {

    private static final long serialVersionUID = 1L;

    public MajorityQuorumTimeoutException() {
    }

    public MajorityQuorumTimeoutException(String message) {
        super(message);
    }
}
