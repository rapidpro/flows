package io.rapidpro.flows.runner;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.BaseFlowsTest;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Input}
 */
public class InputTest extends BaseFlowsTest {

    @Test
    public void buildContext() {
        Input input = Input.of("Hello");

        EvaluationContext container = new EvaluationContext(new HashMap<String, Object>(), ZoneId.of("Africa/Kigali"), DateStyle.DAY_FIRST);

        Map<String, String> context =  input.buildContext(container);
        assertThat(context, hasEntry("*", "Hello"));
        assertThat(context, hasEntry("value", "Hello"));
        assertThat(context, hasEntry(is("time"), notNullValue()));

        input = Input.of(new BigDecimal("123.456"));

        context =  input.buildContext(container);
        assertThat(context, hasEntry("*", "123.456"));
        assertThat(context, hasEntry("value", "123.456"));

        input = Input.of(LocalDate.of(2015, 9, 21));

        context =  input.buildContext(container);
        assertThat(context, hasEntry("*", "21-09-2015"));
        assertThat(context, hasEntry("value", "21-09-2015"));

        input = Input.of(ZonedDateTime.of(2015, 9, 21, 13, 30, 0, 0, ZoneId.of("UTC")));

        context =  input.buildContext(container);
        assertThat(context, hasEntry("*", "21-09-2015 15:30"));
        assertThat(context, hasEntry("value", "21-09-2015 15:30"));
    }
}
