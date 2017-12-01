package io.rapidpro.flows.runner;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.RunnerBuilder;
import io.rapidpro.flows.definition.Flow;
import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Input}
 */
public class InputTest extends BaseFlowsTest {

    protected Runner m_runner;

    protected RunState m_run;

    protected EvaluationContext m_context;

    @Before
    public void setupRunState() throws Exception {
        Flow flow = Flow.fromJson(readResource("test_flows/mushrooms.json"));

        m_runner = new RunnerBuilder().withLocationResolver(new TestLocationResolver()).build();
        m_run = m_runner.start(m_org, m_fields, m_contact, flow);
        m_context = m_run.buildContext(m_runner, null);
    }

    @Test
    public void buildContext() {
        Input input = Input.of("Hello");
        input.m_time = ZonedDateTime.of(2015, 9, 30, 14, 31, 30, 0, ZoneOffset.UTC).toInstant();

        EvaluationContext container = new EvaluationContext(new HashMap<String, Object>(), ZoneId.of("Africa/Kigali"), DateStyle.DAY_FIRST);

        Map<String, String> contactContext = m_contact.buildContext(m_run, container);

        Map<String, Object> context =  input.buildContext(container, contactContext);
        assertThat(context, hasEntry("*", (Object) "Hello"));
        assertThat(context, hasEntry("value", (Object) "Hello"));
        assertThat(context, hasEntry("time", (Object) "30-09-2015 16:31"));
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
