package io.rapidpro.flows.definition.tests.date;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link DateEqualTest}
 */
public class DateEqualTestTest extends BaseTestTest {

    @Test
    public void fromJson() throws Exception {
        DateEqualTest test = DateEqualTest.fromJson(parseObject("{\"test\": \"December 14, 1892\"}"), getDeserializationContext());
        assertThat(test.m_test, is("December 14, 1892"));
    }

    @Test
    public void evaluate() {
        DateEqualTest test = new DateEqualTest("24/8/2015");

        assertTest(test, "23-8-15", false, null);
        assertTest(test, "Aug 24, 2015", true, "24-08-2015");
        assertTest(test, "Twas 25th Aug '15", false, null);

        // date can be an expression
        getContext().putVariable("dob", "24-08-2015");
        test = new DateEqualTest("@(dob)");

        assertTest(test, "23-8-15", false, null);
        assertTest(test, "Aug 24, 2015", true, "24-08-2015");
        assertTest(test, "Twas 25th Aug '15", false, null);
    }
}
