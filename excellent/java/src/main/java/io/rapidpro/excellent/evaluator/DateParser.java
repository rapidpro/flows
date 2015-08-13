package io.rapidpro.excellent.evaluator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flexible date parser for human written dates
 */
public class DateParser {

    private static final Map<String, Integer> monthsByAlias;
    static {
        try {
            monthsByAlias = loadMonthAliases("month.aliases");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final int AM = 0;
    private static final int PM = 1;

    private enum Component {
        YEAR,
        MONTH,
        DAY,
        HOUR,
        MINUTE,
        HOUR_AND_MINUTE,
        AM_PM
    }

    private static final Component[][] s_dateSequencesDayFirst = new Component[][] {
            { Component.DAY, Component.MONTH, Component.YEAR },
            { Component.MONTH, Component.DAY, Component.YEAR },
            { Component.YEAR , Component.MONTH, Component.DAY, },
            { Component.DAY, Component.MONTH },
            { Component.MONTH, Component.DAY },
            { Component.MONTH, Component.YEAR },
    };

    private static final Component[][] s_dateSequencesMonthFirst = new Component[][] {
            { Component.MONTH, Component.DAY, Component.YEAR },
            { Component.DAY, Component.MONTH, Component.YEAR },
            { Component.YEAR , Component.MONTH, Component.DAY, },
            { Component.MONTH, Component.DAY },
            { Component.DAY, Component.MONTH },
            { Component.MONTH, Component.YEAR },
    };

    private static final Component[][] s_timeSequences = new Component[][] {
            { },
            { Component.HOUR, Component.MINUTE },
            { Component.HOUR, Component.MINUTE, Component.AM_PM },
            { Component.HOUR_AND_MINUTE },
    };

    private final LocalDate m_now;
    private final ZoneId m_timezone;
    private final boolean m_dayFirst;

    /**
     * Creates a new date parser
     * @param now the now which parsing happens relative to
     * @param dayFirst whether dates are usually entered day first or month first
     */
    public DateParser(LocalDate now, ZoneId timezone, boolean dayFirst) {
        this.m_now = now;
        this.m_timezone = timezone;
        this.m_dayFirst = dayFirst;
    }

    /**
     * Returns a date or datetime depending on what information is available
     * @return the parsed date or datetime
     */
    public Temporal auto(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // split the text into numerical and text tokens
        Pattern pattern = Pattern.compile("([0-9]+|\\w+)", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(text);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(matcher.group(0));
        }

        // get the possibilities for each token
        List<Map<Component, Integer>> tokenPossibilities = new ArrayList<>();
        for (String token : tokens) {
            Map<Component, Integer> possibilities = getTokenPossibilities(token);
            if (possibilities.size() > 0) {
                tokenPossibilities.add(possibilities);
            }
        }

        // see what valid sequences we can make
        Component[][] dateSequences = m_dayFirst ? s_dateSequencesDayFirst : s_dateSequencesMonthFirst;
        List<Map<Component, Integer>> possibleMatches = new ArrayList<>();

        for (Component[] dateSeq : dateSequences) {
            outer:
            for (Component[] timeSeq : s_timeSequences) {
                if (dateSeq.length + timeSeq.length != tokenPossibilities.size()) {
                    continue;
                }

                Component[] sequence = ArrayUtils.addAll(dateSeq, timeSeq);
                Map<Component, Integer> match = new LinkedHashMap<>();

                for (int c = 0; c < sequence.length; c++) {
                    Component component = sequence[c];
                    Integer value = tokenPossibilities.get(c).get(component);
                    match.put(component, value);

                    if (value == null) {
                        break outer;
                    }
                }

                possibleMatches.add(match);
            }
        }

        // find the first match that can form a valid date or datetime
        for (Map<Component, Integer> match : possibleMatches) {
            Temporal obj = makeResult(match, m_now, m_timezone);
            if (obj != null) {
                return obj;
            }
        }

        return null;
    }

    /**
     * Returns all possible component types of a token without regard to its context. For example "26" could be year,
     * date or minute, but can't be a month or an hour.
     * @param token the token to classify
     * @return the map of possible types and values if token was of that type
     */
    private static Map<Component, Integer> getTokenPossibilities(String token) {
        token = token.toLowerCase().trim();
        Map<Component, Integer> possibilities = new HashMap<>();
        try {
            int asInt = Integer.parseInt(token);
            if (asInt >= 1 && asInt <= 9999 && (token.length() == 2 || token.length() == 4)) {
                possibilities.put(Component.YEAR, asInt);
            }
            if (asInt >= 1 && asInt <= 12) {
                possibilities.put(Component.MONTH, asInt);
            }
            if (asInt >= 1 && asInt <= 31) {
                possibilities.put(Component.DAY, asInt);
            }
            if (asInt >= 0 && asInt <= 23) {
                possibilities.put(Component.HOUR, asInt);
            }
            if (asInt >= 0 && asInt <= 59) {
                possibilities.put(Component.MINUTE, asInt);
            }
            if (token.length() == 4) {
                int hour = asInt / 100;
                int minute = asInt - (hour * 100);
                if (hour >= 1 && hour <= 24 && minute >= 1 && minute <= 59) {
                    possibilities.put(Component.HOUR_AND_MINUTE, asInt);
                }
            }
        }
        catch (NumberFormatException ex) {
            // could it be a month alias?
            Integer month = monthsByAlias.get(token);
            if (month != null) {
                possibilities.put(Component.MONTH, month);
            }

            // could it be an AM/PM marker?
            boolean isAmMarker = token.equals("am");
            boolean isPmMarker = token.equals("pm");
            if (isAmMarker || isPmMarker) {
                possibilities.put(Component.AM_PM, isAmMarker ? AM : PM);
            }
        }

        return possibilities;
    }

    /**
     * Makes a date or datetime object from a map of component values
     * @param values the component values
     * @param timezone the current timezone
     * @return the date, datetime or null if values are invalid
     */
    private static Temporal makeResult(Map<Component, Integer> values, LocalDate now, ZoneId timezone) {
        LocalDate date;

        int year = yearFrom2Digits(values.getOrDefault(Component.YEAR, now.getYear()), now.getYear());
        int month = values.get(Component.MONTH);
        int day = values.getOrDefault(Component.DAY, 1);
        try {
            date = LocalDate.of(year, month, day);
        } catch (DateTimeException ex) {
            return null;  // not a valid date
        }

        if ((values.containsKey(Component.HOUR) && values.containsKey(Component.MINUTE)) || values.containsKey(Component.HOUR_AND_MINUTE)) {
            int hour, minute;
            if (values.containsKey(Component.HOUR_AND_MINUTE)) {
                int combined = values.get(Component.HOUR_AND_MINUTE);
                hour = combined / 100;
                minute = combined - (hour * 100);
            }
            else {
                hour = values.get(Component.HOUR);
                minute = values.get(Component.MINUTE);

                if (hour <= 12 && values.getOrDefault(Component.AM_PM, AM) == PM) {
                    hour += 12;
                }
            }

            try {
                LocalTime time = LocalTime.of(hour, minute);
                return ZonedDateTime.of(date, time, timezone);
            }
            catch (DateTimeException ex) {
                return null;  // not a valid time
            }
        }
        else {
            return date;
        }
    }

    /**
     * Converts a relative 2-digit year to an absolute 4-digit year
     * @param shortYear the relative year
     * @return the absolute year
     */
    protected static int yearFrom2Digits(int shortYear, int currentYear) {
        if (shortYear < 100) {
            shortYear += currentYear - (currentYear % 100);
            if (Math.abs(shortYear - currentYear) >= 50) {
                if (shortYear < currentYear) {
                    return shortYear + 100;
                } else {
                    return shortYear - 100;
                }
            }
        }
        return shortYear;
    }

    /**
     * Loads month aliases from the given resource file
     */
    private static Map<String, Integer> loadMonthAliases(String file) throws IOException {
        InputStream in = DateParser.class.getClassLoader().getResourceAsStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Map<String, Integer> map = new HashMap<>();
        String line;
        int month = 1;
        while ((line = reader.readLine()) != null) {
            for (String alias : line.split(",")) {
                map.put(alias, month);
            }
            month++;
        }
        reader.close();
        return map;
    }
}
