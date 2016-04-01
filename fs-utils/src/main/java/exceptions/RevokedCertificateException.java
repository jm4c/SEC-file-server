package exceptions;

public class RevokedCertificateException extends Exception {

    private static final long serialVersionUID = 1L;

    public RevokedCertificateException() {
    }

    public RevokedCertificateException(String message) {
        super(message);
    }
}
