package io.rapidpro.excellent.parser;

import io.rapidpro.excellent.EvaluationError;
import org.junit.Test;

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
        assertThat(Conversions.toBoolean(-1), is(true));
        assertThat(Conversions.toBoolean(0), is(false));
        assertThat(Conversions.toBoolean("trUE"), is(true));
        assertThat(Conversions.toBoolean("faLSE"), is(false));
    }

    @Test(expected = EvaluationError.class)
    public void test_toBoolean_unparseableString() {
        Conversions.toBoolean("x");
    }
}
