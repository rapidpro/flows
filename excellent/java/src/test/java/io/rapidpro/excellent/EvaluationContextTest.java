package io.rapidpro.excellent;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link EvaluationContext}
 */
public class EvaluationContextTest {

    @Test
    public void fromJson() {
        EvaluationContext context = EvaluationContext.fromJson("{\"name\":\"Bob\", \"age\":32, \"weight\":91.6, \"registered\":true, \"icon\":null, \"address\":{\"city\":\"Kigali\", \"plot\":14}, \"groups\":[\"Testers\", \"Developers\"]}");
        assertThat(context, hasEntry("name", "Bob"));
        assertThat(context, hasEntry("age", 32));
        assertThat(context, hasEntry("weight", new BigDecimal("91.6")));
        assertThat(context, hasEntry("registered", true));
        assertThat(context, hasEntry("icon", null));

        Map<String, Object> address = (Map<String, Object>) context.get("address");
        assertThat(address, hasEntry("city", "Kigali"));
        assertThat(address, hasEntry("plot", 14));

        Object[] groups = (Object[]) context.get("groups");
        assertThat(groups, arrayContaining("Testers", "Developers"));
    }

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

    @Test(expected = RuntimeException.class)
    public void read_containerIsNotMap() {
        Map<String, Object> contact = new HashMap<>();
        contact.put("name", "Bob");
        contact.put("groups", Arrays.asList("Testers", "Developers"));

        EvaluationContext context = new EvaluationContext();
        context.put("contact", contact);

        context.read("contact.groups.something");
    }

    @Test
    public void toJson() {
        EvaluationContext context = new EvaluationContext();
        context.put("foo", 123);

        assertThat(context.toJson(), is("{\"foo\":123}"));
    }
}
