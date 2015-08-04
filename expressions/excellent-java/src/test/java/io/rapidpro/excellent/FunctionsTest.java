package io.rapidpro.excellent;

import org.junit.Test;

import java.math.BigDecimal;

import static io.rapidpro.excellent.Functions.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for {@link io.rapidpro.excellent.Functions}
 */
public class FunctionsTest {

    @Test
    public void test_len() {
        assertThat(len(""), is(0));
        assertThat(len(" "), is(1));
        assertThat(len("qwérty"), is(6));
        assertThat(len("سلام"), is(4));
    }

    @Test
    public void test_pi() {
        assertThat(pi(), is(new BigDecimal(Math.PI)));
    }

    @Test
    public void test_sum() {
        try {
            sum();  // no args is not allowed
            fail();
        }
        catch (EvaluationError e) {}

        assertThat(sum(1), is(new BigDecimal(1)));
        assertThat(sum(1, 2), is(new BigDecimal(3)));
        assertThat(sum(1, 2, "3"), is(new BigDecimal(6)));
    }
}
