package io.rapidpro.excellent.functions;

import io.rapidpro.excellent.parser.Conversions;
import io.rapidpro.excellent.functions.annotations.BooleanDefault;
import io.rapidpro.excellent.functions.annotations.IntegerDefault;
import io.rapidpro.excellent.parser.EvaluationUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementations of supported Excel functions.
 *
 * See https://support.office.com/en-us/article/Excel-functions-by-category-5f91f4e9-7b42-46d2-9bd1-63f26a86c0eb
 */
public class Functions {

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
        return new BigDecimal(Math.pow(_number.doubleValue(), _power.doubleValue()));
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

    /************************************************************************************
     * Custom (non Excel) Functions
     ************************************************************************************/

    /**
     * Returns the first word in the given text string
     */
    public static String first_word(Object text) {
        // In Excel this would be IF(ISERR(FIND(" ",A2)),"",LEFT(A2,FIND(" ",A2)-1))
        return word(text, 1, false);
    }

    /**
     * Formats a number as a percentage
     */
    public static String percent(Object number) {
        int percent = Conversions.toInteger(Conversions.toDecimal(number).multiply(new BigDecimal(100)));
        return percent + "%";
    }

    /**
     * Formats digits in text for reading in TTS
     */
    public static String read_digits(Object text) {
        String _text = Conversions.toString(text).trim();
        if (StringUtils.isEmpty(_text)) {
            return "";
        }

        // trim off the plus for phone numbers
        if (_text.startsWith("+")) {
            _text = _text.substring(1);
        }

        if (_text.length() == 9) { // SSN
            return StringUtils.join(_text.substring(0, 3).toCharArray(), ' ')
                    + " , " + StringUtils.join(_text.substring(3, 5).toCharArray(), ' ')
                    + " , " + StringUtils.join(_text.substring(5).toCharArray(), ' ');
        }
        else if (_text.length() % 3 == 0 && _text.length() > 3) { // triplets, most international phone numbers
            List<String> chunks = chunk(_text, 3);
            return StringUtils.join(StringUtils.join(chunks, ',').toCharArray(), ' ');
        }
        else if (_text.length() % 4 == 0) { // quads, credit cards
            List<String> chunks = chunk(_text, 4);
            return StringUtils.join(StringUtils.join(chunks, ',').toCharArray(), ' ');
        }
        else {
            // otherwise, just put a comma between each number
            return StringUtils.join(_text.toCharArray(), ',');
        }
    }

    /**
     * Removes the first word from the given text string
     */
    public static String remove_first_word(Object text) {
        String _text = StringUtils.stripStart(Conversions.toString(text), null);
        String firstWord = first_word(_text);

        if (StringUtils.isNotEmpty(firstWord)) {
            return StringUtils.stripStart(_text.substring(firstWord.length()), null);
        } else {
            return "";
        }
    }

    /**
     * Extracts the nth word from the given text string
     */
    public static String word(Object text, Object number, @BooleanDefault(false) Object bySpaces) {
        return word_slice(text, number, Conversions.toInteger(number) + 1, bySpaces);
    }

    /**
     * Returns the number of words in the given text string
     */
    public static int word_count(Object text, @BooleanDefault(false) Object bySpaces) {
        String _text = Conversions.toString(text);
        boolean _bySpaces = Conversions.toBoolean(bySpaces);
        return getWords(_text, _bySpaces).size();
    }

    /**
     * Extracts a substring spanning from start up to but not-including stop
     */
    public static String word_slice(Object text, Object start, @IntegerDefault(0) Object stop, @BooleanDefault(false) Object bySpaces) {
        String _text = Conversions.toString(text);
        int _start = Conversions.toInteger(start);
        Integer _stop = Conversions.toInteger(stop);
        boolean _bySpaces = Conversions.toBoolean(bySpaces);

        if (_start == 0) {
            throw new RuntimeException("Start word cannot be zero");
        } else if (_start > 0) {
            _start -= 1;  // convert to a zero-based offset
        }

        if (_stop == 0) {  // zero is treated as no end
            _stop = null;
        } else if (_stop > 0) {
            _stop -= 1; // convert to a zero-based offset
        }

        List<String> words = getWords(_text, _bySpaces);
        List<String> selection = EvaluationUtils.slice(words, _start, _stop);

        // re-combine selected words with a single space
        return StringUtils.join(selection, ' ');
    }

    /************************************************************************************
     * Helper (not available in expressions)
     ************************************************************************************/

    /**
     * Helper function which splits the given text string into words. If by_spaces is false, then text like '01-02-2014'
     * will be split into 3 separate words. For backwards compatibility, this is the default for all expression functions.
     * @param text the text to split
     * @param bySpaces whether words should be split only by spaces or by punctuation like '-', '.' etc
     * @return the words as a list of strings
     */
    private static List<String> getWords(String text, boolean bySpaces) {
        Pattern pattern = Pattern.compile(bySpaces ? "\\s+" : "\\W+", Pattern.MULTILINE|Pattern.UNICODE_CHARACTER_CLASS);
        String[] splits = pattern.split(text);
        return Arrays.asList(splits).stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }

    /**
     * Splits a string into equally sized chunks
     * @param text the text to split
     * @param size the chunk size
     * @return the list of chunks
     */
    private static List<String> chunk(String text, int size) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += size) {
            chunks.add(StringUtils.substring(text, i, i + size));
        }
        return chunks;
    }
}
