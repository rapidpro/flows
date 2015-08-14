package io.rapidpro.excellent.evaluator;

import org.junit.Test;

import java.time.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link DateParser}
 */
public class DateParserTest {

    @Test
    public void auto() {
        DateParser parser = new DateParser(LocalDate.of(2015, 8, 12), ZoneId.of("Africa/Kigali"), true);

        Object[][] tests = {
                { "1/2/34", LocalDate.of(2034, 2, 1) },
                { "1-2-34", LocalDate.of(2034, 2, 1) },
                { "01 02 34", LocalDate.of(2034, 2, 1) },
                { "1 Feb 34", LocalDate.of(2034, 2, 1) },
                { "1. 2 '34", LocalDate.of(2034, 2, 1) },
                { "1st february 2034", LocalDate.of(2034, 2, 1) },
                { "1er février 2034", LocalDate.of(2034, 2, 1) },
                { "2/25-70", LocalDate.of(1970, 2, 25) }, // dayFirst should be ignored when it doesn't make sense
                { "1 feb", LocalDate.of(2015, 2, 1) }, // year can be omitted
                { "Feb 1st", LocalDate.of(2015, 2, 1) },
                { "1 feb 9999999", LocalDate.of(2015, 2, 1) }, // ignore invalid values
                { "1/2/34 14:55", ZonedDateTime.of(2034, 2, 1, 14, 55, 0, 0, ZoneId.of("Africa/Kigali")) },
                { "1-2-34 2:55PM", ZonedDateTime.of(2034, 2, 1, 14, 55, 0, 0, ZoneId.of("Africa/Kigali")) },
                { "01 02 34 1455", ZonedDateTime.of(2034, 2, 1, 14, 55, 0, 0, ZoneId.of("Africa/Kigali")) },
                { "1 Feb 34 02:55 PM", ZonedDateTime.of(2034, 2, 1, 14, 55, 0, 0, ZoneId.of("Africa/Kigali")) },
                { "1. 2 '34 02:55pm", ZonedDateTime.of(2034, 2, 1, 14, 55, 0, 0, ZoneId.of("Africa/Kigali")) },
                { "1st february 2034 14.55", ZonedDateTime.of(2034, 2, 1, 14, 55, 0, 0, ZoneId.of("Africa/Kigali"))},
                {"1er février 2034 1455h", ZonedDateTime.of(2034, 2, 1, 14, 55, 0, 0, ZoneId.of("Africa/Kigali")) }
        };
        for (Object[] test : tests) {
            assertThat("Parse error for " + test[0], parser.auto((String) test[0]), is(test[1]));
        }
    }

    @Test
    public void time() {
        DateParser parser = new DateParser(LocalDate.of(2015, 8, 12), ZoneId.of("Africa/Kigali"), true);

        Object[][] tests = {
                { "2:55", OffsetTime.of(2, 55, 0, 0, ZoneOffset.ofHours(2)) },
                { "2:55 AM", OffsetTime.of(2, 55, 0, 0, ZoneOffset.ofHours(2)) },
                { "14:55", OffsetTime.of(14, 55, 0, 0, ZoneOffset.ofHours(2)) },
                { "2:55PM", OffsetTime.of(14, 55, 0, 0, ZoneOffset.ofHours(2)) },
                { "1455", OffsetTime.of(14, 55, 0, 0, ZoneOffset.ofHours(2)) },
                { "02:55 PM", OffsetTime.of(14, 55, 0, 0, ZoneOffset.ofHours(2)) },
                { "02:55pm", OffsetTime.of(14, 55, 0, 0, ZoneOffset.ofHours(2)) },
                { "14.55", OffsetTime.of(14, 55, 0, 0, ZoneOffset.ofHours(2)) },
                { "1455h", OffsetTime.of(14, 55, 0, 0, ZoneOffset.ofHours(2)) },
                { "14:55:30", OffsetTime.of(14, 55, 30, 0, ZoneOffset.ofHours(2)) },
                { "14:55.30PM", OffsetTime.of(14, 55, 30, 0, ZoneOffset.ofHours(2)) }
        };
        for (Object[] test : tests) {
            assertThat("Parse error for " + test[0], parser.time((String) test[0]), is(test[1]));
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
