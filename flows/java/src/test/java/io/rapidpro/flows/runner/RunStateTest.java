package io.rapidpro.flows.runner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import org.junit.Test;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link RunState}
 */
public class RunStateTest extends BaseFlowsTest {

    @Test
    public void buildDateContext() throws Exception {
        EvaluationContext container = new EvaluationContext(new HashMap<String, Object>(), ZoneId.of("Africa/Kigali"), DateStyle.DAY_FIRST);
        Instant now = Instant.from(ZonedDateTime.of(2015, 8, 24, 9, 44, 5, 0, ZoneId.of("Africa/Kigali")));

        Map<String, String> context = RunState.buildDateContext(container, now);

        assertThat(context, hasEntry("*", "24-08-2015 09:44"));
        assertThat(context, hasEntry("now", "24-08-2015 09:44"));
        assertThat(context, hasEntry("today", "24-08-2015"));
        assertThat(context, hasEntry("tomorrow", "25-08-2015"));
        assertThat(context, hasEntry("yesterday", "23-08-2015"));

        container = new EvaluationContext(new HashMap<String, Object>(), ZoneId.of("Africa/Kigali"), DateStyle.MONTH_FIRST);

        context = RunState.buildDateContext(container, now);

        assertThat(context, hasEntry("*", "08-24-2015 09:44"));
        assertThat(context, hasEntry("now", "08-24-2015 09:44"));
        assertThat(context, hasEntry("today", "08-24-2015"));
        assertThat(context, hasEntry("tomorrow", "08-25-2015"));
        assertThat(context, hasEntry("yesterday", "08-23-2015"));
    }

    @Test
    public void toAndFromJson() throws Exception {
        Flow flow = Flow.fromJson(readResource("flows/mushrooms.json"));
        Flows.Runner runner = new RunnerImpl(null);
        RunState run = runner.start(getOrg(), getContact(), flow);

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(run);

        // TODO

        //RunState run1 = RunState.fromJson(json, flow);
    }
}
