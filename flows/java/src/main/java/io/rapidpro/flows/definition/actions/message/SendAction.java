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

        // Create a new context where the @contact.* variables will be output unresolved. Not a perfect solution as
        // those variables could be combined with other variables in more complex expressions
        Map<String, Object> newVars = stubContactVariables(context.getVariables());
        EvaluationContext contextForOtherContacts = new EvaluationContext(newVars, context.getTimezone(), context.getDateStyle());

        EvaluatedTemplate template = runner.substituteVariables(message, contextForOtherContacts);

        Action performed = new SendAction(new TranslatableText(template.getOutput()), m_groups, m_contacts, m_variables);
        return new Result(performed, template.getErrors());
    }

    /**
     * Copies variables from an existing context, whilst stubbing the contact values. For example, the variable
     * "contact"."name" -> "Joe" will be replaced as "contact"."name" -> "@contact.name".
     */
    protected static Map<String, Object> stubContactVariables(Map<String, Object> variables) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (entry.getKey().equals("contact")) {
                Map<String, String> realContact = (Map<String, String>) entry.getValue();
                Map<String, String> stubContact = new HashMap<>();
                for (Map.Entry<String, String> e : realContact.entrySet()) {
                    stubContact.put(e.getKey(), "@contact." + e.getKey());
                }
                result.put("contact", stubContact);
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
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
