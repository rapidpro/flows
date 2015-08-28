package io.rapidpro.expressions;

import io.rapidpro.expressions.dates.DateStyle;
import org.junit.Test;
import org.threeten.bp.ZoneId;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        assertThat(context.getDateStyle(), is(DateStyle.DAY_FIRST));

        assertThat(context.m_variables, hasEntry("name", (Object) "Bob"));
        assertThat(context.m_variables, hasEntry("age", (Object) 32));
        assertThat(context.m_variables, hasEntry("weight", (Object) new BigDecimal("91.6")));
        assertThat(context.m_variables, hasEntry("registered", (Object) true));
        assertThat(context.m_variables, hasEntry("icon", (Object) null));

        Map<String, Object> address = (Map<String, Object>) context.m_variables.get("address");
        assertThat(address, hasEntry("city", (Object) "Kigali"));
        assertThat(address, hasEntry("plot", (Object) 14));

        Object[] groups = (Object[]) context.m_variables.get("groups");
        assertThat(groups, arrayContaining("Testers", (Object) "Developers"));
    }

    @Test
    public void resolveVariable() {
        Map<String, Object> contact = new HashMap<>();
        contact.put("*", "Bob");
        contact.put("name", "Bob");
        contact.put("age", 33);
        contact.put("join_date_1", "28-08-2015 13:06");
        contact.put("isnull", null);

        EvaluationContext context = new EvaluationContext();
        context.putVariable("foo", 123);
        context.putVariable("contact", contact);

        assertThat(context.resolveVariable("foo"), is((Object) 123));
        assertThat(context.resolveVariable("FOO"), is((Object) 123));
        assertThat(context.resolveVariable("contact"), is((Object) "Bob"));
        assertThat(context.resolveVariable("contact.name"), is((Object) "Bob"));
        assertThat(context.resolveVariable("Contact.AGE"), is((Object) 33));
        assertThat(context.resolveVariable("Contact.join_date_1"), is((Object) "28-08-2015 13:06"));
        assertThat(context.resolveVariable("Contact.isnull"), is((Object) null));
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
