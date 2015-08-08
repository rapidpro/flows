package io.rapidpro.excellent.evaluator;

import java.math.BigDecimal;
import java.util.*;

/**
 * Utility methods
 */
public class EvaluatorUtils {

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
    public static BigDecimal pow(BigDecimal number, BigDecimal power) {
        return new BigDecimal(Math.pow(number.doubleValue(), power.doubleValue()));
    }
}
