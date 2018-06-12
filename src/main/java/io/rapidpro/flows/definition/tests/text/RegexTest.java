package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.utils.ExpressionUtils;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Test that returns whether the input matches a regular expression
 */
public class RegexTest extends TranslatableTest {

    public static final String TYPE = "regex";

    protected RegexTest(TranslatableText test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static RegexTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new RegexTest(TranslatableText.fromJson(obj.get("test")));
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
        try {
            // check whether we match
            int flags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
            Pattern regex = Pattern.compile(localizedTest, flags);
            Matcher matcher = regex.matcher(text);

            if (matcher.find()) {
                String returnMatch = matcher.group(0);

                // add group values by index
                Map<String, String> groupValues = new HashMap<>();
                for (int g = 0; g <= matcher.groupCount(); g++) {
                    String value = matcher.group(g);
                    groupValues.put(String.valueOf(g), value);
                }

                // update @extra
                runner.updateExtra(run, groupValues);

                return Result.match(returnMatch);
            }
        } catch (PatternSyntaxException ignored) {}

        return Result.NO_MATCH;
    }
}
