package io.rapidpro.excellent.evaluator;

import org.junit.Test;

import java.time.LocalDate;
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
        DateParser parser = new DateParser(LocalDate.of(2015, 8, 12), ZoneId.of("UTC"), true);

        // dates only
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
            assertThat("Parse error for " + test, parser.auto(test), is(LocalDate.of(2034, 2, 1)));
        }

        // dayFirst should be ignored when it doesn't make sense
        assertThat(parser.auto("2/25-70"), is(LocalDate.of(1970, 2, 25)));

        // year can be omitted
        assertThat(parser.auto("1 feb"), is(LocalDate.of(2015, 2, 1)));
        assertThat(parser.auto("Feb 1st"), is(LocalDate.of(2015, 2, 1)));

        // ignore invalid values
        assertThat(parser.auto("1 feb 9999999"), is(LocalDate.of(2015, 2, 1)));
    }

    @Test
    public void parse_withTime() {
        DateParser parser = new DateParser(LocalDate.of(2015, 8, 12), ZoneId.of("UTC"), true);

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
            assertThat("Parse error for " + test, parser.auto(test), is(ZonedDateTime.of(2034, 2, 1, 14, 55, 0, 0, ZoneId.of("UTC"))));
        }
    }

    @Test
    public void yearFrom2Digits() {
        assertThat(DateParser.yearFrom2Digits(1, 2015), is(2001));
        assertThat(DateParser.yearFrom2Digits(64, 2015), is(2064));
        assertThat(DateParser.yearFrom2Digits(65, 2015), is(1965));
        assertThat(DateParser.yearFrom2Digits(99, 2015), is(1999));

        assertThat(DateParser.yearFrom2Digits(1, 1990), is(2001));
        assertThat(DateParser.yearFrom2Digits(40, 1990), is(2040));
        assertThat(DateParser.yearFrom2Digits(41, 1990), is(1941));
        assertThat(DateParser.yearFrom2Digits(99, 1990), is(1999));
    }
}
