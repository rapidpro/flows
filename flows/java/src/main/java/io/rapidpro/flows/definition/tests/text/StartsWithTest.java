package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;

/**
 * Whether the text starts with the given string
 */
public class StartsWithTest extends TranslatableTest {

    public StartsWithTest(TranslatableText test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static StartsWithTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new StartsWithTest(TranslatableText.fromJson(obj.get("test")));
    }

    /**
     * @see TranslatableTest#evaluateAgainstLocalized(RunState, EvaluationContext, String, String)
     */
    @Override
    protected Result evaluateAgainstLocalized(RunState run, EvaluationContext context, String text, String localizedTest) {
        localizedTest = run.substituteVariables(localizedTest, context).getOutput();

        // strip leading and trailing whitespace
        text = text.trim();

        // see whether we start with our test
        if (text.toLowerCase().startsWith(localizedTest.toLowerCase())) {
            return Result.textMatch(text.substring(0, localizedTest.length()));
        } else {
            return Result.NO_MATCH;
        }
    }
}
