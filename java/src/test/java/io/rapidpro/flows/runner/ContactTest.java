package io.rapidpro.flows.runner;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.RunnerBuilder;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Contact}
 */
public class ContactTest extends BaseFlowsTest {

    protected Runner m_runner;

    protected RunState m_run;

    protected EvaluationContext m_context;

    @Before
    public void setupRunState() throws Exception {
        Flow flow = Flow.fromJson(readResource("test_flows/mushrooms.json"));

        m_runner = new RunnerBuilder().withLocationResolver(new TestLocationResolver()).build();
        m_run = m_runner.start(m_org, m_fields, m_contact, flow);
        m_context = m_run.buildContext(m_runner, null);
    }

    @Test
    public void toAndFromJson() {
        JsonObject obj = (JsonObject) m_contact.toJson();

        assertThat(obj, is(JsonUtils.object(
                "uuid", "1234-1234",
                "name", "Joe Flow",
                "urns", JsonUtils.array("tel:+260964153686", "twitter:realJoeFlow"),
                "groups", JsonUtils.array("Testers", "Developers"),
                "fields", JsonUtils.object("age", "34", "gender", "M", "joined", "2015-10-06T11:30:01.123Z"),
                "language", "eng"
        )));

        Contact contact = Contact.fromJson(obj);

        assertThat(contact.getUuid(), is("1234-1234"));
        assertThat(contact.getName(), is("Joe Flow"));
        assertThat(contact.getUrns(), contains(new ContactUrn(ContactUrn.Scheme.TEL, "+260964153686"), new ContactUrn(ContactUrn.Scheme.TWITTER, "realJoeFlow")));
        assertThat(contact.getGroups(), containsInAnyOrder("Testers", "Developers"));
        assertThat(contact.getFields(), hasEntry("age", "34"));
        assertThat(contact.getFields(), hasEntry("gender", "M"));
        assertThat(contact.getFields(), hasEntry("joined", "2015-10-06T11:30:01.123Z"));
        assertThat(contact.getLanguage(), is("eng"));
    }

    @Test
    public void getFirstName() {
        assertThat(m_contact.getFirstName(m_org), is("Joe"));
        m_contact.m_name = "Joe";
        assertThat(m_contact.getFirstName(m_org), is("Joe"));
        m_contact.m_name = "";
        assertThat(m_contact.getFirstName(m_org), is("096 4153686"));
        m_contact.m_name = null;
        assertThat(m_contact.getFirstName(m_org), is("096 4153686"));
        m_org.m_anon = true;
        assertThat(m_contact.getFirstName(m_org), is("1234-1234"));
    }

    @Test
    public void setFirstName() {
        m_contact.setFirstName("Bob");
        assertThat(m_contact.m_name, is("Bob Flow"));
        m_contact.m_name = "Joe McFlow Jr III";
        m_contact.setFirstName("Bob");
        assertThat(m_contact.m_name, is("Bob McFlow Jr III"));
        m_contact.m_name = "";
        m_contact.setFirstName("Bob");
        assertThat(m_contact.m_name, is("Bob"));
        m_contact.m_name = null;
        m_contact.setFirstName("Bob");
        assertThat(m_contact.m_name, is("Bob"));
    }

    @Test
    public void buildContext() {
        Map<String, String> context =  m_contact.buildContext(m_run, m_context);
        assertThat(context, hasEntry("*", "Joe Flow"));
        assertThat(context, hasEntry("name", "Joe Flow"));
        assertThat(context, hasEntry("first_name", "Joe"));
        assertThat(context, hasEntry("tel_e164", "+260964153686"));
        assertThat(context, hasEntry("groups", "Testers,Developers"));
        assertThat(context, hasEntry("uuid", "1234-1234"));
        assertThat(context, hasEntry("language", "eng"));

        assertThat(context, hasEntry("tel", "096 4153686"));
        assertThat(context, hasEntry("twitter", "realJoeFlow"));

        assertThat(context, hasEntry("gender", "M"));
        assertThat(context, hasEntry("age", "34"));
        assertThat(context, hasEntry("joined", "06-10-2015 13:30"));

        m_org.m_anon = true;
        m_context.setDateStyle(DateStyle.MONTH_FIRST);
        context =  m_contact.buildContext(m_run, m_context);
        assertThat(context, hasEntry("*", "Joe Flow"));
        assertThat(context, hasEntry("name", "Joe Flow"));
        assertThat(context, hasEntry("first_name", "Joe"));
        assertThat(context, hasEntry("tel_e164", "1234-1234"));
        assertThat(context, hasEntry("groups", "Testers,Developers"));
        assertThat(context, hasEntry("uuid", "1234-1234"));
        assertThat(context, hasEntry("language", "eng"));

        assertThat(context, hasEntry("tel", "1234-1234"));
        assertThat(context, hasEntry("twitter", "1234-1234"));

        assertThat(context, hasEntry("gender", "M"));
        assertThat(context, hasEntry("age", "34"));
        assertThat(context, hasEntry("joined", "10-06-2015 13:30"));
    }
}
