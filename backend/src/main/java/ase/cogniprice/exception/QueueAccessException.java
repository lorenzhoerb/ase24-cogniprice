package ase.cogniprice.exception;

public class QueueAccessException extends Exception {
    public QueueAccessException() {
    }

    public QueueAccessException(String message) {
        super(message);
    }

    public QueueAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueueAccessException(Throwable cause) {
        super(cause);
    }

    public QueueAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
