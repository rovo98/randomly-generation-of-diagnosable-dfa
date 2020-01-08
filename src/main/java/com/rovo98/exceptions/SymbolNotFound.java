package com.rovo98.exceptions;

/**
 * This exception will be thrown when the given symbol of one specified transition not found.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019-12-21
 */
public class SymbolNotFound extends RuntimeException {

    private static final long serialVersionUID = -5657518998473264766L;

    public SymbolNotFound() {
        super();
    }

    public SymbolNotFound(String message) {
        super(message);
    }

    public SymbolNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
