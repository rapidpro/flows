package io.rapidpro.flows.runner;

import com.google.gson.JsonObject;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.definition.*;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.message.ReplyAction;
import io.rapidpro.flows.utils.JsonUtils;
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
        Flow flow = Flow.fromJson(readResource("test_flows/mushrooms.json"));
        Instant arrivedOn = Instant.from(ZonedDateTime.of(2015, 8, 25, 11, 59, 30, 88 * 1000000, ZoneId.of("UTC")));
        Step step = new Step(flow, flow.getEntry(), arrivedOn);

        JsonObject obj = (JsonObject) step.toJson();

        assertThat(obj, is(JsonUtils.object(
                "node", "32cf414b-35e3-4c75-8a78-d5f4de925e13",
                "arrived_on", "2015-08-25T11:59:30.088Z",
                "left_on", null,
                "rule", null,
                "actions", JsonUtils.array(),
                "errors", JsonUtils.array(),
                "flow_id", 17576
        )));

        step.addActionResult(Action.Result.performed(new ReplyAction(new TranslatableText("Hi Joe"))));
        obj = (JsonObject) step.toJson();

        assertThat(obj, is(JsonUtils.object(
                "node", "32cf414b-35e3-4c75-8a78-d5f4de925e13",
                "arrived_on", "2015-08-25T11:59:30.088Z",
                "left_on", null,
                "rule", null,
                "actions", JsonUtils.array(JsonUtils.object("type", "reply", "msg", "Hi Joe")),
                "errors", JsonUtils.array(),
                "flow_id", 17576
        )));

        step.addActionResult(Action.Result.performed(null, Arrays.asList("This is an error", "This too")));
        obj = (JsonObject) step.toJson();

        assertThat(obj, is(JsonUtils.object(
                "node", "32cf414b-35e3-4c75-8a78-d5f4de925e13",
                "arrived_on", "2015-08-25T11:59:30.088Z",
                "left_on", null,
                "rule", null,
                "actions", JsonUtils.array(JsonUtils.object("type", "reply", "msg", "Hi Joe")),
                "errors", JsonUtils.array("This is an error", "This too"),
                "flow_id", 17576
        )));

        step.getActions().clear();
        step.getErrors().clear();

        Rule yesRule = ((RuleSet) ((ActionSet) flow.getEntry()).getDestination()).getRules().get(0);

        step.setRuleResult(new RuleSet.Result(yesRule, "yes", "Yes", "yes ok", null, flow.getId()));
        obj = (JsonObject) step.toJson();

        assertThat(obj, is(JsonUtils.object(
                "node", "32cf414b-35e3-4c75-8a78-d5f4de925e13",
                "arrived_on", "2015-08-25T11:59:30.088Z",
                "left_on", null,
                "rule", JsonUtils.object(
                        "uuid", "a53e3607-ac87-4bee-ab95-30fd4ad8a837",
                        "value", "yes",
                        "category", "Yes",
                        "text", "yes ok",
                        "media", null,
                        "flow_id", 17576
                ),
                "actions", JsonUtils.array(),
                "errors", JsonUtils.array(),
                "flow_id", 17576
        )));

        // test to and from with media value
        step.getActions().clear();
        step.getErrors().clear();

        step.setRuleResult(new RuleSet.Result(yesRule, "yes", "Yes", null, "image/png:file://var/blah.png", flow.getId()));
        obj = (JsonObject) step.toJson();
        assertThat(obj, is(JsonUtils.object(
                "node", "32cf414b-35e3-4c75-8a78-d5f4de925e13",
                "arrived_on", "2015-08-25T11:59:30.088Z",
                "left_on", null,
                "rule", JsonUtils.object(
                        "uuid", "a53e3607-ac87-4bee-ab95-30fd4ad8a837",
                        "value", "yes",
                        "category", "Yes",
                        "text", null,
                        "media", "image/png:file://var/blah.png",
                        "flow_id", 17576
                ),
                "actions", JsonUtils.array(),
                "errors", JsonUtils.array(),
                "flow_id", 17576
        )));
    }
}
