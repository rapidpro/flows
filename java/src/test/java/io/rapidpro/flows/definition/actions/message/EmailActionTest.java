package io.rapidpro.flows.definition.actions.message;

import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link EmailAction}
 */
public class EmailActionTest extends BaseActionTest {

    @Test
    public void execute() {
        EmailAction action = new EmailAction(Arrays.asList("rowan@nyaruka.com", "@(LOWER(contact.gender))@chws.org"),
                "Update from @contact", "This is to notify you that @contact did something");

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        EmailAction performed = (EmailAction) result.getPerformed();

        assertThat(performed.getAddresses(), contains("rowan@nyaruka.com", "m@chws.org"));
        assertThat(performed.getSubject(), is("Update from Joe Flow"));
        assertThat(performed.getMessage(), is("This is to notify you that Joe Flow did something"));
    }
}
