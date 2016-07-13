package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

/**
 * Test that evaulates the exit of a subflow.
 */
public class SubflowTest extends Test {

    public static final String TYPE = "subflow";
    public static final String EXIT_TYPE = "exit_type";

    private String m_exitType;

    public SubflowTest(String exitType) {
        m_exitType = exitType;
    }

    @Override
    public Result evaluate(Runner runner, RunState run, EvaluationContext context, String text) {
        return Result.match(text);
    }

    public static SubflowTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new SubflowTest(obj.get(EXIT_TYPE).getAsString());
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, EXIT_TYPE, m_exitType);
    }
}
