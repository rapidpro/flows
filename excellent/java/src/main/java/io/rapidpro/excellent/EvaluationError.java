package io.rapidpro.excellent;

/**
 * Exception class for evaluation errors which should be given back to the user
 */
public class EvaluationError extends RuntimeException {

    public EvaluationError(String message) {
        super(message);
    }

    public EvaluationError(String message, Throwable cause) {
        super(message, cause);
    }
}
