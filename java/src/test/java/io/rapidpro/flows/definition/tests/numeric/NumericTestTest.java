package io.rapidpro.flows.definition.tests.numeric;

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
        assertThat(NumericTest.extractDecimal("120"), is(new BigDecimal(120)));
        assertThat(NumericTest.extractDecimal("l2O"), is(new BigDecimal(120)));
        assertThat(NumericTest.extractDecimal("123C"), is(new BigDecimal(123)));
    }

    @Test(expected = NumberFormatException.class)
    public void extractDecimal_whenTextIsNaN() {
        NumericTest.extractDecimal("abc");
    }

    @Test(expected = NumberFormatException.class)
    public void extractDecimal_whenTextHasAlphaSubstitutionsAndSuffix() {
        NumericTest.extractDecimal("I23C");
    }
}
