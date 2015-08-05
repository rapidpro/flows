package io.rapidpro.excellent;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EvaluationContextTest {

    @Test
    public void read() {
        Map<String, Object> contact = new HashMap<>();
        contact.put("*", "Bob");
        contact.put("name", "Bob");
        contact.put("age", 33);

        EvaluationContext context = new EvaluationContext();
        context.put("foo", 123);
        context.put("contact", contact);

        assertThat(context.read("foo"), is(123));
        assertThat(context.read("FOO"), is(123));
        assertThat(context.read("contact"), is("Bob"));
        assertThat(context.read("contact.name"), is("Bob"));
        assertThat(context.read("Contact.Age"), is(33));
    }

    @Test(expected = RuntimeException.class)
    public void read_noSuchItem() {
        EvaluationContext context = new EvaluationContext();
        context.put("foo", 123);

        context.read("bar");
    }

    @Test(expected = RuntimeException.class)
    public void read_containerHasNoDefault() {
        Map<String, Object> contact = new HashMap<>();
        contact.put("name", "Bob");

        EvaluationContext context = new EvaluationContext();
        context.put("contact", contact);

        context.read("contact");
    }
}
