package io.rapidpro.excellent;

import java.math.BigDecimal;

/**
 *
 */
public class Conversions {

    public static String toString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        else if (value instanceof BigDecimal) {
            return value.toString();  // TODO replicate format_decimal in the Python
        }
        else {
            throw new EvaluationError("Can't convert '" + value + "' to a string");
        }
    }

    public static BigDecimal toDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        else if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        else if (value instanceof Integer) {
            return new BigDecimal((Integer) value);
        }
        else {
            throw new EvaluationError("Can't convert '" + value + "' to a decimal");
        }
    }

    public static int toInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        else if (value instanceof String) {
            return parseInt((String) value);
        }
        else {
            throw new EvaluationError("Can't convert '" + value + "' to a decimal");
        }
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            throw new EvaluationError("Can't convert '" + value + "' to an integer", e);
        }
    }
}
