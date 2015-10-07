package io.rapidpro.flows.definition.actions.contact;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.ContactUrn;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Saves an evaluated expression to the contact as a field or their name
 */
public class SaveToContactAction extends Action {

    public static final String TYPE = "save";

    @SerializedName("field")
    protected String m_field;

    @SerializedName("label")
    protected String m_label;

    @SerializedName("value")
    protected String m_value;

    public SaveToContactAction(String field, String label, String value) {
        super(TYPE);
        m_field = field;
        m_label = label;
        m_value = value;
    }

    /**
     * @see Action#execute(Runner, RunState, Input)
     */
    @Override
    public Result execute(Runner runner, RunState run, Input input) {
        EvaluatedTemplate template = runner.substituteVariables(m_value, run.buildContext(input));
        if (!template.hasErrors()) {
            String label;
            String value = template.getOutput().trim();

            if (m_field.equals("name")) {
                value = StringUtils.substring(value, 0, 128);
                label = "Contact Name";
                run.getContact().setName(value);
            }
            else if (m_field.equals("first_name")) {
                value = StringUtils.substring(value, 0, 128);
                label = "First Name";
                run.getContact().setFirstName(value);
            }
            else if (m_field.equals("tel_e164")) {
                value = StringUtils.substring(value, 0, 128);
                label = "Phone Number";

                ContactUrn urn = run.getContact().getUrn(Collections.singletonList(ContactUrn.Scheme.TEL));

                List<ContactUrn> urns = run.getContact().getUrns();
                if (urn != null) {
                    urns.remove(urn);
                }

                urns.add(ContactUrn.fromString("tel:" + value));
            }
            else {
                // TODO does the 255 char limit still stand?
                value = StringUtils.substring(value, 0, 255);
                label = m_label;
                runner.updateContactField(run, m_field, value);
            }

            return Result.performed(new SaveToContactAction(m_field, label, value));
        }
        else {
            return Result.errors(template.getErrors());
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
