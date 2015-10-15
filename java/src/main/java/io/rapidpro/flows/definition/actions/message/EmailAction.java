package io.rapidpro.flows.definition.actions.message;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends an email to someone
 */
public class EmailAction extends Action {

    public static final String TYPE = "email";

    @SerializedName("emails")
    protected List<String> m_addresses;

    @SerializedName("subject")
    protected String m_subject;

    @SerializedName("msg")
    protected String m_msg;

    protected EmailAction(List<String> addresses, String subject, String msg) {
        super(TYPE);
        m_addresses = addresses;
        m_subject = subject;
        m_msg = msg;
    }

    @Override
    public Result execute(Runner runner, RunState run, Input input) {
        EvaluationContext context = run.buildContext(runner, input);

        EvaluatedTemplate subject = runner.substituteVariables(m_subject, context);
        EvaluatedTemplate message = runner.substituteVariables(m_msg, context);

        List<String> errors = new ArrayList<>();
        errors.addAll(subject.getErrors());
        errors.addAll(message.getErrors());

        List<String> addresses = new ArrayList<>();
        for (String address : m_addresses) {
            EvaluatedTemplate addr = runner.substituteVariables(address, context);
            addresses.add(addr.getOutput());
            errors.addAll(addr.getErrors());
        }

        Action performed = new EmailAction(addresses, subject.getOutput(), message.getOutput());
        return Result.performed(performed, errors);
    }

    public List<String> getAddresses() {
        return m_addresses;
    }

    public String getSubject() {
        return m_subject;
    }

    public String getMsg() {
        return m_msg;
    }
}
