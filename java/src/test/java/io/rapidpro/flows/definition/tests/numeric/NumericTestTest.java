package io.rapidpro.flows.definition.tests.numeric;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link NumericTest}
 */
public class NumericTestTest {

    @Test
    public void extractDecimal() throws Exception {
        assertThat(NumericTest.extractDecimal("120"), is((Pair) new ImmutablePair<>(new BigDecimal(120), "120")));
        assertThat(NumericTest.extractDecimal("l2O"), is((Pair) new ImmutablePair<>(new BigDecimal(120), "120")));
        assertThat(NumericTest.extractDecimal("123C"), is((Pair) new ImmutablePair<>(new BigDecimal(123), "123")));
    }

    @Test(expected = NumberFormatException.class)
    public void extractDecimal_whenTextIsNaN() {
        NumericTest.extractDecimal("abc");
    }

    @Test(expected = NumberFormatException.class)
    public void extractDecimal_whenTextHasAlphaSubsitutionsAndSuffix() {
        NumericTest.extractDecimal("I23C");
    }
}
