package io.rapidpro.flows.definition.actions.message;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.ContactRef;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.RecipientVariable;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Runner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends a message to people other than the contact
 */
public class SendAction extends MessageAction {

    public static final String TYPE = "send";

    @SerializedName("contacts")
    protected List<ContactRef> m_contacts;

    @SerializedName("groups")
    protected List<GroupRef> m_groups;

    @SerializedName("variables")
    protected List<RecipientVariable> m_variables;

    public SendAction(TranslatableText msg, List<GroupRef> groups, List<ContactRef> contacts, List<RecipientVariable> variables) {
        super(TYPE, msg);
        m_contacts = contacts;
        m_groups = groups;
        m_variables = variables;
    }

    /**
     * @see MessageAction#executeWithMessage(Runner, EvaluationContext, String)
     */
    @Override
    protected Result executeWithMessage(Runner runner, EvaluationContext context, String message) {
        // TODO evaluate variables (except @new_contact)... though what do we return them as ?

        // create a new context without the @contact.* variables which will remain unresolved for now
        Map<String, Object> newVars = new HashMap<>(context.getVariables());
        newVars.remove("contact");
        EvaluationContext contextForOtherContacts = new EvaluationContext(newVars, context.getTimezone(), context.getDateStyle());

        EvaluatedTemplate template = runner.substituteVariablesIfAvailable(message, contextForOtherContacts);

        Action performed = new SendAction(new TranslatableText(template.getOutput()), m_groups, m_contacts, m_variables);
        return new Result(performed, template.getErrors());
    }

    public List<ContactRef> getContacts() {
        return m_contacts;
    }

    public List<GroupRef> getGroups() {
        return m_groups;
    }

    public List<RecipientVariable> getVariables() {
        return m_variables;
    }
}
