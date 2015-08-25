package io.rapidpro.expressions.evaluator;

import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.EvaluationError;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import org.threeten.bp.*;
import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Conversions}
 */
public class ConversionsTest {

    private EvaluationContext m_context;

    @Before
    public void setup() {
        m_context = new EvaluationContext(new HashMap<String, Object>(), ZoneId.of("Africa/Kigali"), DateStyle.DAY_FIRST);
    }

    @Test
    public void test_toBoolean() {
        assertThat(Conversions.toBoolean(true, m_context), is(true));
        assertThat(Conversions.toBoolean(false, m_context), is(false));

        assertThat(Conversions.toBoolean(1, m_context), is(true));
        assertThat(Conversions.toBoolean(0, m_context), is(false));
        assertThat(Conversions.toBoolean(-1, m_context), is(true));

        assertThat(Conversions.toBoolean(new BigDecimal(0.5), m_context), is(true));
        assertThat(Conversions.toBoolean(new BigDecimal(0.0), m_context), is(false));
        assertThat(Conversions.toBoolean(new BigDecimal(-0.5), m_context), is(true));

        assertThat(Conversions.toBoolean("trUE", m_context), is(true));
        assertThat(Conversions.toBoolean("faLSE", m_context), is(false));
        assertThat(Conversions.toBoolean("faLSE", m_context), is(false));

        assertThat(Conversions.toBoolean(LocalDate.of(2012, 3, 4), m_context), is(true));
        assertThat(Conversions.toBoolean(OffsetTime.of(12, 34, 0, 0, ZoneOffset.UTC), m_context), is(true));
        assertThat(Conversions.toBoolean(ZonedDateTime.of(2012, 3, 4, 5, 6, 7, 8, ZoneId.of("UTC")), m_context), is(true));
    }

    @Test(expected = EvaluationError.class)
    public void test_toBoolean_unparseableString() {
        Conversions.toBoolean("x", m_context);
    }

    @Test
    public void test_toInteger() {
        assertThat(Conversions.toInteger(true, m_context), is(1));
        assertThat(Conversions.toInteger(false, m_context), is(0));

        assertThat(Conversions.toInteger(1234567890, m_context), is(1234567890));

        assertThat(Conversions.toInteger(new BigDecimal("1234"), m_context), is(1234));
        assertThat(Conversions.toInteger(new BigDecimal("1234.5678"), m_context), is(1235));
        assertThat(Conversions.toInteger(new BigDecimal("0.001"), m_context), is(0));

        assertThat(Conversions.toInteger("1234", m_context), is(1234));
    }

    @Test(expected = EvaluationError.class)
    public void test_toInteger_unparseableString() {
        Conversions.toInteger("x", m_context);
    }

    @Test(expected = EvaluationError.class)
    public void test_toInteger_overflow() {
        Conversions.toInteger(new BigDecimal("12345678901234567890"), m_context);
    }

    @Test
    public void test_toDecimal() {
        assertThat(Conversions.toDecimal(true, m_context), is(BigDecimal.ONE));
        assertThat(Conversions.toDecimal(false, m_context), is(BigDecimal.ZERO));

        assertThat(Conversions.toDecimal(123, m_context), is(new BigDecimal(123)));
        assertThat(Conversions.toDecimal(-123, m_context), is(new BigDecimal(-123)));

        assertThat(Conversions.toDecimal(new BigDecimal("1234.5678"), m_context), is(new BigDecimal("1234.5678")));

        assertThat(Conversions.toDecimal("1234.5678", m_context), is(new BigDecimal("1234.5678")));
    }

    @Test(expected = EvaluationError.class)
    public void test_toDecimal_unparseableString() {
        Conversions.toDecimal("x", m_context);
    }

    @Test
    public void test_toString() {
        assertThat(Conversions.toString(true, m_context), is("TRUE"));
        assertThat(Conversions.toString(false, m_context), is("FALSE"));

        assertThat(Conversions.toString(-1, m_context), is("-1"));
        assertThat(Conversions.toString(1234567890, m_context), is("1234567890"));

        assertThat(Conversions.toString(new BigDecimal("0.4440000"), m_context), is("0.444"));
        assertThat(Conversions.toString(new BigDecimal("1234567890.5"), m_context), is("1234567891"));
        assertThat(Conversions.toString(new BigDecimal("33.333333333333"), m_context), is("33.33333333"));
        assertThat(Conversions.toString(new BigDecimal("66.666666666666"), m_context), is("66.66666667"));

        assertThat(Conversions.toString("hello", m_context), is("hello"));

        assertThat(Conversions.toString(LocalDate.of(2012, 3, 4), m_context), is("04-03-2012"));
        assertThat(Conversions.toString(OffsetTime.of(12, 34, 0, 0, ZoneOffset.ofHours(2)), m_context), is("12:34"));
        assertThat(Conversions.toString(ZonedDateTime.of(2012, 3, 4, 5, 6, 7, 8, ZoneId.of("Africa/Kigali")), m_context), is("04-03-2012 05:06"));

        m_context.setDateStyle(DateStyle.MONTH_FIRST);

        assertThat(Conversions.toString(LocalDate.of(2012, 3, 4), m_context), is("03-04-2012"));
        assertThat(Conversions.toString(ZonedDateTime.of(2012, 3, 4, 5, 6, 7, 8, ZoneId.of("Africa/Kigali")), m_context), is("03-04-2012 05:06"));
    }

    @Test
    public void test_toDate() {
        assertThat(Conversions.toDate("14th Aug 2015", m_context), is(LocalDate.of(2015, 8, 14)));
        assertThat(Conversions.toDate("14/8/15", m_context), is(LocalDate.of(2015, 8, 14)));

        assertThat(Conversions.toDate(LocalDate.of(2015, 8, 14), m_context), is(LocalDate.of(2015, 8, 14)));

        assertThat(Conversions.toDate(ZonedDateTime.of(2015, 8, 14, 9, 12, 0, 0, ZoneId.of("Africa/Kigali")), m_context), is(LocalDate.of(2015, 8, 14)));

        m_context.setDateStyle(DateStyle.MONTH_FIRST);

        assertThat(Conversions.toDate("12/8/15", m_context), is(LocalDate.of(2015, 12, 8)));
        assertThat(Conversions.toDate("14/8/15", m_context), is(LocalDate.of(2015, 8, 14))); // ignored because doesn't make sense
    }

    @Test
    public void test_toTime() {
        assertThat(Conversions.toTime("9:12", m_context), is(OffsetTime.of(9, 12, 0, 0, ZoneOffset.ofHours(2))));
        assertThat(Conversions.toTime("0912", m_context), is(OffsetTime.of(9, 12, 0, 0, ZoneOffset.ofHours(2))));
        assertThat(Conversions.toTime("09.12am", m_context), is(OffsetTime.of(9, 12, 0, 0, ZoneOffset.ofHours(2))));

        assertThat(Conversions.toTime(OffsetTime.of(9, 12, 0, 0, ZoneOffset.UTC), m_context), is(OffsetTime.of(9, 12, 0, 0, ZoneOffset.UTC)));

        assertThat(Conversions.toTime(ZonedDateTime.of(2015, 8, 14, 9, 12, 0, 0, ZoneId.of("Africa/Kigali")), m_context), is(OffsetTime.of(9, 12, 0, 0, ZoneOffset.ofHours(2))));
    }
}
