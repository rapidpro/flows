package io.rapidpro.flows.definition.actions.message;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.ContactRef;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.VariableRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Runner;

import java.util.ArrayList;
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
    protected List<VariableRef> m_variables;

    public SendAction(TranslatableText msg, List<GroupRef> groups, List<ContactRef> contacts, List<VariableRef> variables) {
        super(TYPE, msg);
        m_contacts = contacts;
        m_groups = groups;
        m_variables = variables;
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

        Action performed = new SendAction(new TranslatableText(template.getOutput()), m_groups, m_contacts, variables);
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
