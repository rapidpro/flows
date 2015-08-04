package io.rapidpro.excellent;

/**
 *
 */
public class EvaluationError extends RuntimeException {

    public EvaluationError(String message) {
        super(message);
    }

    public EvaluationError(String message, Throwable cause) {
        super(message, cause);
    }
}
