package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.runner.RunState;

/**
 * Whether the text starts with the given string
 */
public class StartsWithTest extends Test.Translatable {

    public StartsWithTest(TranslatableText test) {
        super(test);
    }

    public static StartsWithTest fromJson(JsonObject json) {
        return new StartsWithTest(TranslatableText.fromJson(json.get("test")));
    }

    /**
     * @see Test#evaluate(RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(RunState run, EvaluationContext context, String text) {
        // localize and substitute any variables
        String localizedTest = m_test.getLocalized(run);
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
