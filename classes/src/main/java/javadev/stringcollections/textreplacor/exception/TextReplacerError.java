package javadev.stringcollections.textreplacor.exception;

public class TextReplacerError extends Exception {

    public TextReplacerError(String message) {
        super(message);
    }

    public TextReplacerError(String message, Throwable cause) {
        super(message, cause);
    }

    public TextReplacerError(Throwable cause) {
        super(cause);
    }

    public TextReplacerError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


}
