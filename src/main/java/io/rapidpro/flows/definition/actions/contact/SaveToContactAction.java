package io.rapidpro.flows.definition.actions.contact;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.*;
import io.rapidpro.flows.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;

/**
 * Saves an evaluated expression to the contact as a field or their name
 */
public class SaveToContactAction extends Action {

    public static final String TYPE = "save";

    protected String m_field;

    protected String m_label;

    protected String m_value;

    public SaveToContactAction(String field, String label, String value) {
        m_field = field;
        m_label = label;
        m_value = value;
    }

    /**
     * @see Action#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static SaveToContactAction fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new SaveToContactAction(
                JsonUtils.getAsString(obj, "field"),
                JsonUtils.getAsString(obj, "label"),
                JsonUtils.getAsString(obj, "value")
        );
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "field", m_field, "label", m_label, "value", m_value);
    }

    /**
     * @see Action#execute(Runner, RunState, Input)
     */
    @Override
    public Result execute(Runner runner, RunState run, Input input) {
        EvaluatedTemplate valueTpl = runner.substituteVariables(m_value, run.buildContext(runner, input));
        if (!valueTpl.hasErrors()) {
            String field = m_field;
            String label;
            String value = valueTpl.getOutput().trim();

            if ("name".equals(m_field)) {
                value = StringUtils.substring(value, 0, 128);
                label = "Contact Name";
                run.getContact().setName(value);
            }
            else if ("first_name".equals(m_field)) {
                value = StringUtils.substring(value, 0, 128);
                label = "First Name";
                run.getContact().setFirstName(value);
            }
            else if ("tel_e164".equals(m_field)) {
                value = StringUtils.substring(value, 0, 128);
                label = "Phone Number";

                ContactUrn urn = new ContactUrn(ContactUrn.Scheme.TEL, value).normalized(run.getOrg());
                run.getContact().getUrns().add(urn);
            }
            else {
                value = StringUtils.substring(value, 0, 640);
                label = m_label;
                try {
                    Field fieldObj = runner.updateContactField(run, m_field, value, label);
                    field = fieldObj.getKey();
                    label = fieldObj.getLabel();
                } catch (RuntimeException ex) {
                    return Result.errors(Collections.singletonList(ex.getMessage()));
                }
            }

            return Result.performed(new SaveToContactAction(field, label, value));
        }
        else {
            return Result.errors(valueTpl.getErrors());
        }
    }

    public String getField() {
        return m_field;
    }

    public String getLabel() {
        return m_label;
    }

    public String getValue() {
        return m_value;
    }
}
