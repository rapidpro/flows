package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.utils.Parameter;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

/**
 * Test that evaluates whether the contact is in a group
 */
public class InGroupTest extends Test {

    public static final String TYPE = "in_group";

    private GroupRef m_group;

    public InGroupTest(GroupRef group) {
        m_group = group;
    }

    @Override
    public Result evaluate(Runner runner, RunState run, EvaluationContext context, String text) {

        boolean inGroup = run.getContact().getGroups().contains(m_group.getName());
        if (inGroup) {
            return new Result(true, m_group.getName());
        } else {
            return new Result(false, null);
        }
    }

    public static InGroupTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        GroupRef group = JsonUtils.fromJson(obj, context, GroupRef.class);
        return new InGroupTest(group);
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = JsonUtils.object("type", TYPE, "name", m_group.getName());
        if (m_group.getUuid() != null) {
            obj.addProperty("uuid", m_group.getUuid());
        }
        return obj;
    }
}

