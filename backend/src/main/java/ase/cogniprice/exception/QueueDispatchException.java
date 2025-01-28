package ase.cogniprice.exception;

public class QueueDispatchException extends Exception {
    public QueueDispatchException() {
    }

    public QueueDispatchException(String message) {
        super(message);
    }

    public QueueDispatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueueDispatchException(Throwable cause) {
        super(cause);
    }

    public QueueDispatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
