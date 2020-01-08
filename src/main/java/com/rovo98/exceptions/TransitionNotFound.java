package com.rovo98.exceptions;

/**
 * This Runtime Exception will be thrown when target transition not found.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.27
 */
public class TransitionNotFound extends RuntimeException {
    private static final long serialVersionUID = 4731058511698923108L;

    public TransitionNotFound() {
    }

    public TransitionNotFound(String message) {
        super(message);
    }

    public TransitionNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
