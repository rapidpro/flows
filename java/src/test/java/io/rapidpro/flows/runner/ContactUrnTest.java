package io.rapidpro.flows.runner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rapidpro.flows.BaseFlowsTest;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link ContactUrn}
 */
public class ContactUrnTest extends BaseFlowsTest {

    @Test
    public void fromString() {
        ContactUrn urn = ContactUrn.fromString("tel:+260964153686");

        assertThat(urn.getScheme(), is(ContactUrn.Scheme.TEL));
        assertThat(urn.getPath(), is("+260964153686"));
    }

    @Test
    public void toAndFromJson() {
        Gson gson = new GsonBuilder().create();
        ContactUrn urn = new ContactUrn(ContactUrn.Scheme.TEL, "+260964153686");
        String json = gson.toJson(urn);

        assertThat(json, is("\"tel:+260964153686\""));

        urn = gson.fromJson(json, ContactUrn.class);

        assertThat(urn.getScheme(), is(ContactUrn.Scheme.TEL));
        assertThat(urn.getPath(), is("+260964153686"));
    }

    @Test
    public void normalized() {
        ContactUrn raw = new ContactUrn(ContactUrn.Scheme.TEL, " 078-383-5665 ");
        assertThat(raw.normalized(m_org), is(new ContactUrn(ContactUrn.Scheme.TEL, "+250783835665")));

        raw = new ContactUrn(ContactUrn.Scheme.TWITTER, "  @bob ");
        assertThat(raw.normalized(m_org), is(new ContactUrn(ContactUrn.Scheme.TWITTER, "bob")));
    }
}
