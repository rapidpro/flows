package io.rapidpro.expressions.utils;

import io.rapidpro.expressions.dates.DateStyle;
import org.apache.commons.lang3.ArrayUtils;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility methods
 */
public class ExpressionUtils {

    /**
     * Slices a list, Python style
     * @param list the list
     * @param start the start index (null means the beginning of the list)
     * @param stop the stop index (null means the end of the list)
     * @return the slice
     */
    public static <T> List<T> slice(List<T> list, Integer start, Integer stop) {
        int size = list.size();

        if (start == null) {
            start = 0;
        } else if (start < 0) {
            start = size + start;
        }

        if (stop == null) {
            stop = size;
        } else if (stop < 0) {
            stop = size + stop;
        }

        if (start >= size || stop <= 0 || start >= stop) {
            return Collections.emptyList();
        }

        start = Math.max(0, start);
        stop = Math.min(size, stop);

        return list.subList(start, stop);
    }

    /**
     * Pow for two decimals
     */
    public static BigDecimal decimalPow(BigDecimal number, BigDecimal power) {
        return new BigDecimal(Math.pow(number.doubleValue(), power.doubleValue()));
    }

    /**
     * Encodes text for inclusion in a URL query string. Should be equivalent to Django's urlquote function.
     * @param text the text to encode
     * @return the encoded text
     */
    public static String urlquote(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets a formatter for dates or datetimes
     * @param dateStyle whether parsing should be day-first or month-first
     * @param incTime whether to include time
     * @return the formatter
     */
    public static DateTimeFormatter getDateFormatter(DateStyle dateStyle, boolean incTime) {
        String format = dateStyle.equals(DateStyle.DAY_FIRST) ? "dd-MM-yyyy" : "MM-dd-yyyy";
        return DateTimeFormatter.ofPattern(incTime ? format + " HH:mm" : format);
    }

    /**
     * Replacement for Java 8's Map#getOrDefault(String, Object)
     */
    public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        return map.containsKey(key) ? map.get(key) : defaultValue;
    }

    /**
     * Tokenizes a string by splitting on non-word characters. This should be equivalent to splitting on \W+ in Python.
     * The meaning of \W is different in Android, Java 7, Java 8 hence the non-use of regular expressions.
     * @param str the input string
     * @return the string tokens
     */
    public static String[] tokenize(String str) {
        if (str == null) {
            return null;
        }
        final int len = str.length();
        if (len == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        final List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        while (i < len) {
            int ch = str.codePointAt(i);
            if (!(Character.isDigit(ch) || ch == '_' || Character.isAlphabetic(ch))) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }
}
