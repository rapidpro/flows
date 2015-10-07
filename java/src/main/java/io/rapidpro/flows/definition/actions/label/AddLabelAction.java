package io.rapidpro.flows.definition.actions.label;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.LabelRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds one or more labels to the incoming message
 */
public class AddLabelAction extends Action {

    public static final String TYPE = "add_label";

    @SerializedName("labels")
    protected List<LabelRef> m_labels;

    protected AddLabelAction(List<LabelRef> labels) {
        super(TYPE);
        m_labels = labels;
    }

    @Override
    public Result execute(Runner runner, RunState run, Input input) {
        EvaluationContext context = run.buildContext(input);
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
            return Result.performed(new AddLabelAction(labels), errors);
        } else {
            return Result.errors(errors);
        }
    }

    public List<LabelRef> getLabels() {
        return m_labels;
    }
}
