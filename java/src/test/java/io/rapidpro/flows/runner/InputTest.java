package io.rapidpro.flows.runner;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.BaseFlowsTest;
import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.math.BigDecimal;
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

        Map<String, String> contactContext = m_contact.buildContext(m_org);

        Map<String, Object> context =  input.buildContext(container, contactContext);
        assertThat(context, hasEntry("*", (Object) "Hello"));
        assertThat(context, hasEntry("value", (Object) "Hello"));
        assertThat(context, hasEntry(is("time"), notNullValue()));
        assertThat(context, hasEntry("contact", (Object) contactContext));

        input = Input.of(new BigDecimal("123.456"));

        context =  input.buildContext(container, contactContext);
        assertThat(context, hasEntry("*", (Object) "123.456"));
        assertThat(context, hasEntry("value", (Object) "123.456"));

        input = Input.of(LocalDate.of(2015, 9, 21));

        context =  input.buildContext(container, contactContext);
        assertThat(context, hasEntry("*", (Object) "21-09-2015"));
        assertThat(context, hasEntry("value", (Object) "21-09-2015"));

        input = Input.of(ZonedDateTime.of(2015, 9, 21, 13, 30, 0, 0, ZoneId.of("UTC")));

        context =  input.buildContext(container, contactContext);
        assertThat(context, hasEntry("*", (Object) "21-09-2015 15:30"));
        assertThat(context, hasEntry("value", (Object) "21-09-2015 15:30"));
    }
}
