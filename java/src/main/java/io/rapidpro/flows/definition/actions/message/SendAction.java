package io.rapidpro.flows.definition.actions.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.*;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends a message to people other than the contact
 */
public class SendAction extends MessageAction {

    public static final String TYPE = "send";

    protected List<ContactRef> m_contacts;

    protected List<GroupRef> m_groups;

    protected List<VariableRef> m_variables;

    public SendAction(TranslatableText msg, List<ContactRef> contacts, List<GroupRef> groups, List<VariableRef> variables) {
        super(msg);
        m_contacts = contacts;
        m_groups = groups;
        m_variables = variables;
    }

    /**
     * @see Action#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static SendAction fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new SendAction(
                TranslatableText.fromJson(obj.get("msg")),
                JsonUtils.fromJsonArray(obj.get("contacts").getAsJsonArray(), context, ContactRef.class),
                JsonUtils.fromJsonArray(obj.get("groups").getAsJsonArray(), context, GroupRef.class),
                JsonUtils.fromJsonArray(obj.get("variables").getAsJsonArray(), context, VariableRef.class)
        );
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object(
                "type", TYPE,
                "contacts", JsonUtils.toJsonArray(m_contacts),
                "groups", JsonUtils.toJsonArray(m_groups),
                "variables", JsonUtils.toJsonArray(m_variables),
                "msg", m_msg.toJson()
        );
    }

    /**
     * @see MessageAction#executeWithMessage(Runner, EvaluationContext, String)
     */
    @Override
    protected Result executeWithMessage(Runner runner, EvaluationContext context, String msg) {
        List<String> errors = new ArrayList<>();

        // variables should evaluate to group names or phone numbers
        List<VariableRef> variables = new ArrayList<>();
        for (VariableRef variable : m_variables) {
            if (!variable.isNewContact()) {
                EvaluatedTemplate varTpl = runner.substituteVariables(variable.getValue(), context);
                if (!varTpl.hasErrors()) {
                    variables.add(new VariableRef(varTpl.getOutput()));
                } else {
                    errors.addAll(varTpl.getErrors());
                }
            } else {
                variables.add(new VariableRef(variable.getValue()));
            }
        }

        // create a new context without the @contact.* variables which will remain unresolved for now
        Map<String, Object> newVars = new HashMap<>(context.getVariables());
        newVars.remove("contact");
        EvaluationContext contextForOtherContacts = new EvaluationContext(newVars, context.getTimezone(), context.getDateStyle());

        EvaluatedTemplate template = runner.substituteVariablesIfAvailable(msg, contextForOtherContacts);
        errors.addAll(template.getErrors());

        Action performed = new SendAction(new TranslatableText(template.getOutput()), m_contacts, m_groups, variables);
        return Result.performed(performed, errors);
    }

    public List<ContactRef> getContacts() {
        return m_contacts;
    }

    public List<GroupRef> getGroups() {
        return m_groups;
    }

    public List<VariableRef> getVariables() {
        return m_variables;
    }
}
