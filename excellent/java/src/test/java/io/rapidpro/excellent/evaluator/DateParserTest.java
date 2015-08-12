package io.rapidpro.excellent.evaluator;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link DateParser}
 */
public class DateParserTest {

    @Test
    public void parse_dateOnly() {
        DateParser parser = new DateParser(ZonedDateTime.of(2015, 8, 12, 13, 45, 10, 7, ZoneId.of("UTC")), true);

        // dates only - time will come from "now"
        String[] tests = {
                "1/2/34",
                "1-2-34",
                "01 02 34",
                "1 Feb 34",
                "1. 2 '34",
                "1st february 2034",
                "1er février 2034",
        };
        for (String test : tests) {
            assertThat("Parse error for " + test, parser.parse(test), is(ZonedDateTime.of(2034, 2, 1, 13, 45, 10, 7, ZoneId.of("UTC"))));
        }

        // dayFirst should be ignored when it doesn't make sense
        assertThat(parser.parse("2/25-70"), is(ZonedDateTime.of(1970, 2, 25, 13, 45, 10, 7, ZoneId.of("UTC"))));

        // year can be omitted
        assertThat(parser.parse("1 feb"), is(ZonedDateTime.of(2015, 2, 1, 13, 45, 10, 7, ZoneId.of("UTC"))));
        assertThat(parser.parse("Feb 1st"), is(ZonedDateTime.of(2015, 2, 1, 13, 45, 10, 7, ZoneId.of("UTC"))));

        // ignore invalid values
        assertThat(parser.parse("1 feb 9999999"), is(ZonedDateTime.of(2015, 2, 1, 13, 45, 10, 7, ZoneId.of("UTC"))));
    }

    @Test
    public void parse_withTime() {
        DateParser parser = new DateParser(ZonedDateTime.of(2015, 8, 12, 13, 45, 10, 7, ZoneId.of("UTC")), true);

        String[] tests = {
                "1/2/34 14:55",
                "1-2-34 2:55PM",
                "01 02 34 1455",
                "1 Feb 34 02:55 PM",
                "1. 2 '34 02:55pm",
                "1st february 2034 14.55",
                "1er février 2034 1455h",
        };
        for (String test : tests) {
            assertThat("Parse error for " + test, parser.parse(test), is(ZonedDateTime.of(2034, 2, 1, 14, 55, 10, 7, ZoneId.of("UTC"))));
        }
    }

    @Test
    public void yearFrom2Digits() {
        DateParser parser = new DateParser(ZonedDateTime.of(2015, 8, 12, 12, 0, 0, 0, ZoneId.of("UTC")), true);

        assertThat(parser.yearFrom2Digits(1), is(2001));
        assertThat(parser.yearFrom2Digits(64), is(2064));
        assertThat(parser.yearFrom2Digits(65), is(1965));
        assertThat(parser.yearFrom2Digits(99), is(1999));

        parser = new DateParser(ZonedDateTime.of(1990, 8, 12, 12, 0, 0, 0, ZoneId.of("UTC")), true);

        assertThat(parser.yearFrom2Digits(1), is(2001));
        assertThat(parser.yearFrom2Digits(40), is(2040));
        assertThat(parser.yearFrom2Digits(41), is(1941));
        assertThat(parser.yearFrom2Digits(99), is(1999));
    }
}
