package io.rapidpro.excellent.evaluator;

import io.rapidpro.excellent.EvaluationContext;
import io.rapidpro.excellent.EvaluationError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Type conversions required for expression evaluation
 */
public class Conversions {

    /**
     * Tries conversion of any value to a boolean (called a 'Logical Value' in Excel lingo)
     */
    public static boolean toBoolean(Object value, EvaluationContext ctx) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        else if (value instanceof Integer) {
            return ((Integer) value) != 0;
        }
        else if (value instanceof BigDecimal) {
            return !value.equals(BigDecimal.ZERO);
        }
        else if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.equalsIgnoreCase("TRUE")) {
                return true;
            }  else if (strVal.equalsIgnoreCase("FALSE")) {
                return false;
            }
        }
        else if (value instanceof LocalDate || value instanceof OffsetTime || value instanceof ZonedDateTime) {
            return true;
        }

        throw new EvaluationError("Can't convert '" + value + "' to a boolean");
    }

    /**
     * Tries conversion of any value to an integer
     */
    public static int toInteger(Object value, EvaluationContext ctx) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        else if (value instanceof Integer) {
            return (Integer) value;
        }
        else if (value instanceof BigDecimal) {
            try {
                return ((BigDecimal) value).setScale(0, RoundingMode.HALF_UP).intValueExact();
            }
            catch (ArithmeticException ex) {}
        }
        else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            }
            catch (NumberFormatException e) {}
        }

        throw new EvaluationError("Can't convert '" + value + "' to an integer");
    }

    /**
     * Tries conversion of any value to a decimal
     */
    public static BigDecimal toDecimal(Object value, EvaluationContext ctx) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        else if (value instanceof Integer) {
            return new BigDecimal((Integer) value);
        }
        else if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            }
            catch (NumberFormatException e) {}
        }

        throw new EvaluationError("Can't convert '" + value + "' to a decimal");
    }

    /**
     * Tries conversion of any value to a string
     */
    public static String toString(Object value, EvaluationContext ctx) {
        if (value instanceof Boolean) {
            return (Boolean) value ? "TRUE" : "FALSE";
        }
        else if (value instanceof Integer) {
            return String.valueOf(value);
        }
        else if (value instanceof BigDecimal) {
            return formatDecimal((BigDecimal) value);
        }
        else if (value instanceof String) {
            return (String) value;
        }
        else if (value instanceof LocalDate) {
            return ctx.getDateFormatter(false).format((LocalDate) value);
        }
        else if (value instanceof OffsetTime) {
            return DateTimeFormatter.ofPattern("HH:mm").format((OffsetTime) value);
        }
        else if (value instanceof ZonedDateTime) {
            return ctx.getDateFormatter(true).format((ZonedDateTime) value);
        }

        throw new EvaluationError("Can't convert '" + value + "' to a string");
    }

    /**
     * Tries conversion of any value to a date
     */
    public static LocalDate toDate(Object value, EvaluationContext ctx) {
        if (value instanceof String) {
            ZonedDateTime dateTime = EvaluatorUtils.strToDateTime((String) value, ctx.getTimezone(), ctx.isDayFirst());
            if (dateTime != null) {
                return dateTime.toLocalDate();
            }
        }
        else if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toLocalDate(); // discard time
        }

        throw new EvaluationError("Can't convert '" + value + "' to a string");
    }

    /**
     * Tries conversion of any value to a date
     */
    public static ZonedDateTime toDateTime(Object value, EvaluationContext ctx) {
        if (value instanceof String) {
            ZonedDateTime dateTime = EvaluatorUtils.strToDateTime((String) value, ctx.getTimezone(), ctx.isDayFirst());
            if (dateTime != null) {
                return dateTime;
            }
        }
        else if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay(ctx.getTimezone());
        }
        else if (value instanceof ZonedDateTime) {
            return (ZonedDateTime) value;
        }

        throw new EvaluationError("Can't convert '" + value + "' to a string");
    }

    /**
     * Formats a decimal number using the same precision as Excel
     * @param decimal the decimal value
     * @return the formatted string value
     */
    private static String formatDecimal(BigDecimal decimal) {
        decimal = decimal.stripTrailingZeros();
        int intDigits = decimal.precision() - decimal.scale();  // number of non-fractional digits
        int fractionalDigits = Math.min(Math.max(10 - intDigits, 0), decimal.scale());
        decimal = decimal.setScale(fractionalDigits, RoundingMode.HALF_UP);
        return decimal.toString();
    }
}
