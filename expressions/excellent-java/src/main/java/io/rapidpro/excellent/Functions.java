package io.rapidpro.excellent;

import java.math.BigDecimal;

/**
 *
 */
public class Functions {

    public static int len(Object text) {
        return Conversions.toString(text).length();
    }

    public static BigDecimal pi() {
        return new BigDecimal(Math.PI);
    }

    public static BigDecimal sum(Object... args) {
        if (args.length == 0) {
            throw new EvaluationError("Wrong number of arguments");
        }

        BigDecimal result = BigDecimal.ZERO;
        for (Object arg : args) {
            result = result.add(Conversions.toDecimal(arg));
        }
        return result;
    }
}
