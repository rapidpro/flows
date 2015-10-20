package io.rapidpro.flows.definition.actions.label;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.LabelRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds one or more labels to the incoming message
 */
public class AddLabelsAction extends Action {

    public static final String TYPE = "add_label";

    protected List<LabelRef> m_labels;

    protected AddLabelsAction(List<LabelRef> labels) {
        m_labels = labels;
    }

    /**
     * @see Action#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static AddLabelsAction fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new AddLabelsAction(JsonUtils.fromJsonArray(obj.get("labels").getAsJsonArray(), context, LabelRef.class));
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "labels", JsonUtils.toJsonArray(m_labels));
    }

    @Override
    public Result execute(Runner runner, RunState run, Input input) {
        EvaluationContext context = run.buildContext(runner, input);
        List<LabelRef> labels = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (LabelRef label : m_labels) {
            if (label.getId() == null) {
                EvaluatedTemplate template = runner.substituteVariables(label.getName(), context);
                if (!template.hasErrors()) {
                    labels.add(new LabelRef(template.getOutput()));
                } else {
                    errors.addAll(template.getErrors());
                }
            } else {
                labels.add(label);
            }
        }

        if (labels.size() > 0) {
            return Result.performed(new AddLabelsAction(labels), errors);
        } else {
            return Result.errors(errors);
        }
    }

    public List<LabelRef> getLabels() {
        return m_labels;
    }
}
