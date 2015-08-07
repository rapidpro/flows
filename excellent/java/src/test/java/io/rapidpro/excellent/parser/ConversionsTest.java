package io.rapidpro.excellent.parser;

import io.rapidpro.excellent.EvaluationError;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Conversions}
 */
public class ConversionsTest {

    @Test
    public void test_toBoolean() {
        assertThat(Conversions.toBoolean(true), is(true));
        assertThat(Conversions.toBoolean(false), is(false));

        assertThat(Conversions.toBoolean(1), is(true));
        assertThat(Conversions.toBoolean(0), is(false));
        assertThat(Conversions.toBoolean(-1), is(true));

        assertThat(Conversions.toBoolean(new BigDecimal(0.5)), is(true));
        assertThat(Conversions.toBoolean(new BigDecimal(0.0)), is(false));
        assertThat(Conversions.toBoolean(new BigDecimal(-0.5)), is(true));

        assertThat(Conversions.toBoolean("trUE"), is(true));
        assertThat(Conversions.toBoolean("faLSE"), is(false));
        assertThat(Conversions.toBoolean("faLSE"), is(false));
    }

    @Test(expected = EvaluationError.class)
    public void test_toBoolean_unparseableString() {
        Conversions.toBoolean("x");
    }

    @Test
    public void test_toInteger() {
        assertThat(Conversions.toInteger(true), is(1));
        assertThat(Conversions.toInteger(false), is(0));

        assertThat(Conversions.toInteger(1234567890), is(1234567890));

        assertThat(Conversions.toInteger(new BigDecimal("1234")), is(1234));
        assertThat(Conversions.toInteger(new BigDecimal("1234.5678")), is(1235));
        assertThat(Conversions.toInteger(new BigDecimal("0.001")), is(0));

        assertThat(Conversions.toInteger("1234"), is(1234));
    }

    @Test(expected = EvaluationError.class)
    public void test_toInteger_unparseableString() {
        Conversions.toInteger("x");
    }

    @Test(expected = EvaluationError.class)
    public void test_toInteger_overflow() {
        Conversions.toInteger(new BigDecimal("12345678901234567890"));
    }

    @Test
    public void test_toDecimal() {
        assertThat(Conversions.toDecimal(true), is(BigDecimal.ONE));
        assertThat(Conversions.toDecimal(false), is(BigDecimal.ZERO));

        assertThat(Conversions.toDecimal(123), is(new BigDecimal(123)));
        assertThat(Conversions.toDecimal(-123), is(new BigDecimal(-123)));

        assertThat(Conversions.toDecimal(new BigDecimal("1234.5678")), is(new BigDecimal("1234.5678")));

        assertThat(Conversions.toDecimal("1234.5678"), is(new BigDecimal("1234.5678")));
    }

    @Test(expected = EvaluationError.class)
    public void test_toDecimal_unparseableString() {
        Conversions.toDecimal("x");
    }

    @Test
    public void test_toString() {
        assertThat(Conversions.toString(true), is("TRUE"));
        assertThat(Conversions.toString(false), is("FALSE"));

        assertThat(Conversions.toString(-1), is("-1"));
        assertThat(Conversions.toString(1234567890), is("1234567890"));

        assertThat(Conversions.toString(new BigDecimal("0.4440000")), is("0.444"));
        assertThat(Conversions.toString(new BigDecimal("1234567890.5")), is("1234567891"));
        assertThat(Conversions.toString(new BigDecimal("33.333333333333")), is("33.33333333"));
        assertThat(Conversions.toString(new BigDecimal("66.666666666666")), is("66.66666667"));

        assertThat(Conversions.toString("hello"), is("hello"));
    }
}
