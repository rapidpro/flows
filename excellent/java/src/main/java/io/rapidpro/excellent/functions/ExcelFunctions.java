package io.rapidpro.excellent.functions;

import io.rapidpro.excellent.evaluator.Conversions;
import io.rapidpro.excellent.functions.annotations.BooleanDefault;
import io.rapidpro.excellent.functions.annotations.IntegerDefault;
import io.rapidpro.excellent.evaluator.EvaluatorUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;

/**
 * Library of supported Excel functions.
 *
 * See https://support.office.com/en-us/article/Excel-functions-by-category-5f91f4e9-7b42-46d2-9bd1-63f26a86c0eb
 */
public class ExcelFunctions {

    /************************************************************************************
     * Text Functions
     ************************************************************************************/

    /**
     * Returns the character specified by a number
     */
    public static String _char(Object number) {
        return "" + (char) Conversions.toInteger(number);
    }

    /**
     * Removes all non-printable characters from a text string
     */
    public static String clean(Object text) {
        return Conversions.toString(text).replaceAll("\\p{C}", "");
    }

    /**
     * Returns a numeric code for the first character in a text string
     */
    public static int code(Object text) {
        return unicode(text); // everything is unicode
    }

    /**
     * Joins text strings into one text string
     */
    public static String concatenate(Object... args) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append(Conversions.toString(arg));
        }
        return sb.toString();
    }

    /**
     * Formats the given number in decimal format using a period and commas
     */
    public static String fixed(Object number, @IntegerDefault(2) Object decimals, @BooleanDefault(false) Object noCommas) {
        BigDecimal _number = Conversions.toDecimal(number);
        _number = _number.setScale(Conversions.toInteger(decimals), RoundingMode.HALF_UP);

        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(9);
        format.setGroupingUsed(!Conversions.toBoolean(noCommas));
        return format.format(_number);
    }

    /**
     * Returns the first characters in a text string
     */
    public static String left(Object text, Object numChars) {
        int _numChars = Conversions.toInteger(numChars);
        if (_numChars < 0) {
            throw new RuntimeException("Number of chars can't be negative");
        }

        return StringUtils.left(Conversions.toString(text), _numChars);
    }

    /**
     * Returns the number of characters in a text string
     */
    public static int len(Object text) {
        return Conversions.toString(text).length();
    }

    /**
     * Converts a text string to lowercase
     */
    public static String lower(Object text) {
        return Conversions.toString(text).toLowerCase();
    }

    /**
     * Capitalizes the first letter of every word in a text string
     */
    public static String proper(Object text) {
        String _text = Conversions.toString(text).toLowerCase();

        if (!StringUtils.isEmpty(_text)) {
            char[] buffer = _text.toCharArray();
            boolean capitalizeNext = true;

            for (int i = 0; i < buffer.length; ++i) {
                char ch = buffer[i];
                if (!Character.isAlphabetic(ch)) {
                    capitalizeNext = true;
                } else if (capitalizeNext) {
                    buffer[i] = Character.toTitleCase(ch);
                    capitalizeNext = false;
                }
            }
            return new String(buffer);
        } else {
            return _text;
        }
    }

    /**
     * Repeats text a given number of times
     */
    public static String rept(Object text, Object numberTimes) {
        int _numberTimes = Conversions.toInteger(numberTimes);
        if (_numberTimes < 0) {
            throw new RuntimeException("Number of times can't be negative");
        }

        return StringUtils.repeat(Conversions.toString(text), _numberTimes);
    }

    /**
     * Returns the last characters in a text string
     */
    public static String right(Object text, Object numChars) {
        int _numChars = Conversions.toInteger(numChars);
        if (_numChars < 0) {
            throw new RuntimeException("Number of chars can't be negative");
        }

        return StringUtils.right(Conversions.toString(text), _numChars);
    }

    /**
     * Substitutes new_text for old_text in a text string
     */
    public static String substitute(Object text, Object oldText, Object newText, @IntegerDefault(-1) Object instanceNum) {
        String _text = Conversions.toString(text);
        String _oldText = Conversions.toString(oldText);
        String _newText = Conversions.toString(newText);
        int _instanceNum = Conversions.toInteger(instanceNum);

        if (_instanceNum < 0) {
            return _text.replace(_oldText, _newText);
        }
        else {
            String[] splits = _text.split(_oldText);
            StringBuilder output = new StringBuilder(splits[0]);
            for (int s = 1; s < splits.length; s++) {
                String sep = s == _instanceNum ? _newText : _oldText;
                output.append(sep);
                output.append(splits[s]);
            }
            return output.toString();
        }
    }

    /**
     * Returns the unicode character specified by a number
     */
    public static String unichar(Object number) {
        return "" + (char) Conversions.toInteger(number);
    }

    /**
     * Returns a numeric code for the first character in a text string
     */
    public static int unicode(Object text) {
        String _text = Conversions.toString(text);
        if (_text.length() == 0) {
            throw new RuntimeException("Text can't be empty");
        }
        return (int) _text.charAt(0);
    }

    /**
     * Converts a text string to uppercase
     */
    public static String upper(Object text) {
        return Conversions.toString(text).toUpperCase();
    }

    /************************************************************************************
     * Date and Time Functions
     ************************************************************************************/

    /**
     * Defines a date value
     */
    public static LocalDate date(Object year, Object month, Object day) {
        return LocalDate.of(Conversions.toInteger(year), Conversions.toInteger(month), Conversions.toInteger(day));
    }

    // TODO add the rest of the date functions

    /************************************************************************************
     * Math Functions
     ************************************************************************************/

    /**
     * Returns the absolute value of a number
     */
    public static BigDecimal abs(Object number) {
        return Conversions.toDecimal(number).abs();
    }

    /**
     * Returns the maximum of all arguments
     */
    public static BigDecimal max(Object... args) {
        if (args.length == 0) {
            throw new RuntimeException("Wrong number of arguments");
        }

        BigDecimal result = null;
        for (Object arg : args) {
            BigDecimal _arg = Conversions.toDecimal(arg);
            result = result != null ? _arg.max(result) : _arg;
        }
        return result;
    }

    /**
     * Returns the minimum of all arguments
     */
    public static BigDecimal min(Object... args) {
        if (args.length == 0) {
            throw new RuntimeException("Wrong number of arguments");
        }

        BigDecimal result = null;
        for (Object arg : args) {
            BigDecimal _arg = Conversions.toDecimal(arg);
            result = result != null ? _arg.min(result) : _arg;
        }
        return result;
    }

    /**
     * Returns the result of a number raised to a power
     */
    public static BigDecimal power(Object number, Object power) {
        BigDecimal _number = Conversions.toDecimal(number);
        BigDecimal _power = Conversions.toDecimal(power);
        return EvaluatorUtils.pow(_number, _power);
    }

    /**
     * Returns an evenly distributed random real number greater than or equal to 0 and less than 1
     */
    public static BigDecimal rand() {
        return new BigDecimal(Math.random());
    }

    /**
     * Returns the sum of all arguments
     */
    public static BigDecimal sum(Object... args) {
        if (args.length == 0) {
            throw new RuntimeException("Wrong number of arguments");
        }

        BigDecimal result = BigDecimal.ZERO;
        for (Object arg : args) {
            result = result.add(Conversions.toDecimal(arg));
        }
        return result;
    }

    /************************************************************************************
     * Logical Functions
     ************************************************************************************/

    /**
     * Returns TRUE if and only if all its arguments evaluate to TRUE
     */
    public static boolean and(Object... args) {
        for (Object arg : args) {
            if (!Conversions.toBoolean(arg)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the logical value FALSE
     */
    public static boolean _false() {
        return false;
    }

    /**
     * Returns one value if the condition evaluates to TRUE, and another value if it evaluates to FALSE
     */
    public static Object _if(Object logicalTest, @IntegerDefault(0) Object valueIfTrue, @BooleanDefault(false) Object valueIfFalse) {
        return Conversions.toBoolean(logicalTest) ? valueIfTrue : valueIfFalse;
    }

    /**
     * Returns TRUE if any argument is TRUE
     */
    public static boolean or(Object... args) {
        for (Object arg : args) {
            if (Conversions.toBoolean(arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the logical value TRUE
     */
    public static boolean _true() {
        return true;
    }
}
