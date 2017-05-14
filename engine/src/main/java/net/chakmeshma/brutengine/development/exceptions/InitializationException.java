package net.chakmeshma.brutengine.development.exceptions;

/**
 * Created by chakmeshma on 05.05.2017.
 */

public class InitializationException extends Exception {
    private String message;

    public InitializationException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
