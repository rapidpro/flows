package io.rapidpro.excellent;

import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Excellent}
 */
public class ExcellentTest {

    @Test
    public void getTemplateEvaluator() {
        assertThat(Excellent.getTemplateEvaluator(), instanceOf(Excellent.TemplateEvaluator.class));
    }
}
