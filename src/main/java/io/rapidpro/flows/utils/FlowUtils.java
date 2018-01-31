package io.rapidpro.flows.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Miscellaneous utility methods
 */
public class FlowUtils {

    /**
     * Normalizes the passed in number, they should be only digits, some backends prepend + and maybe crazy users put in
     * dashes or parentheses in the console
     * @param number the number, e.g. "0783835665"
     * @param countryCode the 2-letter country code, e.g. "RW"
     * @return a pair of the normalized number and whether it looks like a possible full international number
     */
    public static Pair<String, Boolean> normalizeNumber(String number, String countryCode) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        number = number.toLowerCase();

        // if the number ends with e11, then that is Excel corrupting it, remove it
        if (number.endsWith("e+11") || number.endsWith("e+12")) {
            number = number.substring(0, number.length() - 4).replace(".", "");
        }

        // remove other characters
        number = number.replaceAll("[^0-9a-z\\+]", "");

        // add on a plus if it looks like it could be a fully qualified number
        if (number.length() >= 11 && number.charAt(0) != '+') {
            number = '+' + number;
        }

        try {
            Phonenumber.PhoneNumber normalized = phoneUtil.parse(number, countryCode);

            // now does it look plausible ?
            if (phoneUtil.isValidNumber(normalized)) {
                return new ImmutablePair<>(phoneUtil.format(normalized, PhoneNumberUtil.PhoneNumberFormat.E164), true);
            }
        } catch (NumberParseException ignored) {}

        // this must be a local number of some kind, just lowercase and save
        return new ImmutablePair<>(number.replaceAll("[^0-9a-z]", ""), false);
    }

    /**
     * Equivalent to str.title() in Python
     */
    public static String title(String str) {
        return WordUtils.capitalize(str, ' ');
    }
}
