package io.rapidpro.flows;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class FlowUtils {

    /**
     * Gets the named member as a string, returning null if it's null
     */
    public static String getAsString(JsonObject obj, String memberName) {
        JsonElement member = obj.get(memberName);
        return member.isJsonNull() ? null : member.getAsString();
    }

    /**
     * Computes the Damerau-Levenshtein distance between the two given strings
     */
    public static int editDistance(String s1, String s2) {
        // if first letters are different, infinite distance
        if (StringUtils.isNotEmpty(s1) && StringUtils.isNotEmpty(s2) && s1.charAt(0) != s2.charAt(0)) {
            return 100;
        }

        Map<Pair<Integer, Integer>, Integer> d = new HashMap<>();
        int lenstr1 = s1.length();
        int lenstr2 = s2.length();

        for (int i = -1; i < lenstr1 + 1; i++) {
            d.put(new ImmutablePair<>(i, -1), i + 1);
        }
        for (int j = -1; j < lenstr2 + 1; j++) {
            d.put(new ImmutablePair<>(-1, j), j + 1);
        }

        for (int i = 0; i < lenstr1; i++) {
            for (int j = 0; j < lenstr2; j++) {
                int cost = s1.charAt(i) == s2.charAt(j) ? 0 : 1;

                int deletion = d.get(new ImmutablePair<>(i - 1, j)) + 1;
                int insertion = d.get(new ImmutablePair<>(i, j - 1)) + 1;
                int substitution = d.get(new ImmutablePair<>(i - 1, j - 1)) + cost;

                int val = Math.min(deletion, Math.min(insertion, substitution));

                if (i > 1 && j > 1 && s1.charAt(i) == s2.charAt(j - 1) && s1.charAt(i - 1) == s2.charAt(j)) {
                    int transposition = d.get(new ImmutablePair<>(i - 2, j - 2)) + cost;
                    val = Math.min(val, transposition);
                }

                d.put(new ImmutablePair<>(i, j), val);
            }
        }
        return d.get(new ImmutablePair<>(lenstr1-1,lenstr2-1));
    }
}
