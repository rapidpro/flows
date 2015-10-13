package io.rapidpro.flows.definition.tests.date;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;
import org.threeten.bp.LocalDate;

/**
 * Test for {@link HasDateTest}
 */
public class HasDateTestTest extends BaseTestTest {

    @Test
    public void fromJson() throws Exception {
        HasDateTest.fromJson(parseObject("{}"), m_deserializationContext);
    }

    @Test
    public void evaluate() {
        HasDateTest test = new HasDateTest();

        assertTest(test, "December 14, 1992", true, "December 14, 1992", LocalDate.of(1992, 12, 14));
        assertTest(test, "sometime on 24/8/15", true, "24/8/15", LocalDate.of(2015, 8, 24));

        assertTest(test, "no date in this text", false, null);
        assertTest(test, "123", false, null);  // this differs from old implementation which was a bit too flexible regarding dates
    }
}
