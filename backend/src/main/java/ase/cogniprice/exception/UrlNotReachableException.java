package ase.cogniprice.exception;

public class UrlNotReachableException extends RuntimeException {
    public UrlNotReachableException(String message) {
        super(message);
    }

    public UrlNotReachableException(String message, Throwable cause) {
        super(message, cause);
    }
}
