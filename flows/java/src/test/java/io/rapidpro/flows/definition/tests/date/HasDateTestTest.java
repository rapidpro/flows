package io.rapidpro.flows.definition.tests.date;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

/**
 * Test for {@link HasDateTest}
 */
public class HasDateTestTest extends BaseTestTest {

    @Test
    public void fromJson() throws Exception {
        HasDateTest.fromJson(parseObject("{}"), getDeserializationContext());
    }

    @Test
    public void evaluate() {
        HasDateTest test = new HasDateTest();

        assertTest(test, "December 14, 1892", true, "14-12-1892");
        assertTest(test, "sometime on 24/8/15", true, "24-08-2015");

        assertTest(test, "no date in this text", false, null);
        assertTest(test, "123", false, null);  // this differs from old implementation which was a bit too flexible regarding dates
    }
}
