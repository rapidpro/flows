package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

/**
 * Test that returns whether the text starts with the given text
 */
public class StartsWithTest extends TranslatableTest {

    public static final String TYPE = "starts";

    public StartsWithTest(TranslatableText test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static StartsWithTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new StartsWithTest(TranslatableText.fromJson(obj.get("test")));
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "test", m_test.toJson());
    }

    /**
     * @see TranslatableTest#evaluateForLocalized(Runner, RunState, EvaluationContext, String, String)
     */
    @Override
    protected Result evaluateForLocalized(Runner runner, RunState run, EvaluationContext context, String text, String localizedTest) {
        localizedTest = runner.substituteVariables(localizedTest, context).getOutput();

        // strip leading and trailing whitespace
        text = text.trim();

        // see whether we start with our test
        if (text.toLowerCase().startsWith(localizedTest.toLowerCase())) {
            return Result.match(text.substring(0, localizedTest.length()));
        } else {
            return Result.NO_MATCH;
        }
    }
}
