package io.rapidpro.flows.runner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.definition.*;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.ReplyAction;
import org.junit.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Step}
 */
public class StepTest extends BaseFlowsTest {

    @Test
    public void toAndFromJson() throws Exception {
        Flow flow = Flow.fromJson(readResource("flows/mushrooms.json"));
        Instant arrivedOn = Instant.from(ZonedDateTime.of(2015, 8, 25, 11, 59, 30, 88 * 1000000, ZoneId.of("UTC")));
        Step step = new Step(flow.getEntry(), arrivedOn);

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(step);

        assertThat(json, is("{" +
                "\"node\":\"32cf414b-35e3-4c75-8a78-d5f4de925e13\"," +
                "\"arrived_on\":\"2015-08-25T11:59:30.088Z\"," +
                "\"actions\":[]," +
                "\"errors\":[]" +
                "}"));

        step.addActionResult(new Action.Result(new ReplyAction(new TranslatableText("Hi Joe"))));
        json = gson.toJson(step);

        assertThat(json, is("{" +
                "\"node\":\"32cf414b-35e3-4c75-8a78-d5f4de925e13\"," +
                "\"arrived_on\":\"2015-08-25T11:59:30.088Z\"," +
                "\"actions\":[{\"msg\":\"Hi Joe\",\"type\":\"reply\"}]," +
                "\"errors\":[]" +
                "}"));

        step.addActionResult(new Action.Result(null, Arrays.asList("This is an error", "This too")));
        json = gson.toJson(step);

        assertThat(json, is("{" +
                "\"node\":\"32cf414b-35e3-4c75-8a78-d5f4de925e13\"," +
                "\"arrived_on\":\"2015-08-25T11:59:30.088Z\"," +
                "\"actions\":[{\"msg\":\"Hi Joe\",\"type\":\"reply\"}]," +
                "\"errors\":[\"This is an error\",\"This too\"]" +
                "}"));

        step.getActions().clear();
        step.getErrors().clear();

        Rule yesRule = ((RuleSet) ((ActionSet) flow.getEntry()).getDestination()).getRules().get(0);

        step.setRuleResult(new Rule.Result(yesRule, "yes", "Yes", "yes ok"));
        json = gson.toJson(step);

        assertThat(json, is("{" +
                "\"node\":\"32cf414b-35e3-4c75-8a78-d5f4de925e13\"," +
                "\"arrived_on\":\"2015-08-25T11:59:30.088Z\"," +
                "\"rule\":{\"uuid\":\"a53e3607-ac87-4bee-ab95-30fd4ad8a837\",\"value\":\"yes\",\"category\":\"Yes\",\"text\":\"yes ok\"}," +
                "\"actions\":[]," +
                "\"errors\":[]" +
                "}"));
    }
}
