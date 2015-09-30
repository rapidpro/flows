package io.rapidpro.flows.runner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rapidpro.flows.BaseFlowsTest;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Contact}
 */
public class ContactTest extends BaseFlowsTest {

    @Test
    public void toAndFromJson() {
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(getContact());

        Contact contact = gson.fromJson(json, Contact.class);

        assertThat(contact.getUuid(), is("1234-1234"));
        assertThat(contact.getName(), is("Joe Flow"));
        assertThat(contact.getUrns(), contains(new ContactUrn(ContactUrn.Scheme.TEL, "+260964153686"), new ContactUrn(ContactUrn.Scheme.TWITTER, "realJoeFlow")));
        assertThat(contact.getGroups(), contains("Testers", "Developers"));
        assertThat(contact.getFields(), hasEntry("age", "34"));
        assertThat(contact.getFields(), hasEntry("gender", "M"));
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
        Map<String, String> context =  getContact().buildContext(getOrg());
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

        m_org.m_anon = true;
        context =  getContact().buildContext(getOrg());
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
    }
}
