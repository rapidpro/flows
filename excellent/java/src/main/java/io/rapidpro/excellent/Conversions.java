package io.rapidpro.excellent;

import java.math.BigDecimal;

/**
 * Type conversions required for expression evaluation
 */
public class Conversions {

    public static String toString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        else if (value instanceof BigDecimal) {
            return value.toString();  // TODO replicate format_decimal in the Python
        }
        else if (value instanceof Integer) {
            return String.valueOf(value);
        }
        else if (value instanceof Boolean) {
            return (Boolean) value ? "TRUE" : "FALSE";
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
        else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        else {
            throw new EvaluationError("Can't convert '" + value + "' to an integer");
        }
    }

    /**
     * Attempts conversion of any value to a boolean (called a 'Logical Value' in Excel lingo)
     */
    public static boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        else if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.equalsIgnoreCase("TRUE")) {
                return true;
            }  else if (strVal.equalsIgnoreCase("FALSE")) {
                return false;
            }
        }
        else if (value instanceof Integer) {
            return ((Integer) value) != 0;
        }
        else if (value instanceof BigDecimal) {
            return value.equals(BigDecimal.ZERO);
        }

        throw new EvaluationError("Can't convert '" + value + "' to a boolean");
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
