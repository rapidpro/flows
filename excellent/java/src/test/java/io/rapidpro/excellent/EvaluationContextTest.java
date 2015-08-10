package io.rapidpro.excellent;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link EvaluationContext}
 */
public class EvaluationContextTest {

    @Test
    public void fromJson() {
        EvaluationContext context = EvaluationContext.fromJson("{\"vars\":{\"name\":\"Bob\", \"age\":32, \"weight\":91.6, \"registered\":true, \"icon\":null, \"address\":{\"city\":\"Kigali\", \"plot\":14}, \"groups\":[\"Testers\", \"Developers\"]}, \"tz\": \"UTC\", \"day_first\": true}");
        assertThat(context.getTimezone(), is(ZoneId.of("UTC")));
        assertThat(context.isDayFirst(), is(true));

        assertThat(context.m_variables, hasEntry("name", "Bob"));
        assertThat(context.m_variables, hasEntry("age", 32));
        assertThat(context.m_variables, hasEntry("weight", new BigDecimal("91.6")));
        assertThat(context.m_variables, hasEntry("registered", true));
        assertThat(context.m_variables, hasEntry("icon", null));

        Map<String, Object> address = (Map<String, Object>) context.m_variables.get("address");
        assertThat(address, hasEntry("city", "Kigali"));
        assertThat(address, hasEntry("plot", 14));

        Object[] groups = (Object[]) context.m_variables.get("groups");
        assertThat(groups, arrayContaining("Testers", "Developers"));
    }

    @Test
    public void resolveVariable() {
        Map<String, Object> contact = new HashMap<>();
        contact.put("*", "Bob");
        contact.put("name", "Bob");
        contact.put("age", 33);

        EvaluationContext context = new EvaluationContext();
        context.putVariable("foo", 123);
        context.putVariable("contact", contact);

        assertThat(context.resolveVariable("foo"), is(123));
        assertThat(context.resolveVariable("FOO"), is(123));
        assertThat(context.resolveVariable("contact"), is("Bob"));
        assertThat(context.resolveVariable("contact.name"), is("Bob"));
        assertThat(context.resolveVariable("Contact.Age"), is(33));
    }

    @Test(expected = RuntimeException.class)
    public void read_noSuchItem() {
        EvaluationContext context = new EvaluationContext();
        context.putVariable("foo", 123);

        context.resolveVariable("bar");
    }

    @Test(expected = RuntimeException.class)
    public void read_containerHasNoDefault() {
        Map<String, Object> contact = new HashMap<>();
        contact.put("name", "Bob");

        EvaluationContext context = new EvaluationContext();
        context.putVariable("contact", contact);

        context.resolveVariable("contact");
    }

    @Test(expected = RuntimeException.class)
    public void read_containerIsNotMap() {
        Map<String, Object> contact = new HashMap<>();
        contact.put("name", "Bob");
        contact.put("groups", Arrays.asList("Testers", "Developers"));

        EvaluationContext context = new EvaluationContext();
        context.putVariable("contact", contact);

        context.resolveVariable("contact.groups.something");
    }
}
