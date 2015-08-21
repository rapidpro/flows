package io.rapidpro.flows.runner;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link ContactUrn}
 */
public class ContactUrnTest {

    @Test
    public void parse() {
        ContactUrn urn = ContactUrn.fromString("tel:+260964153686");
        assertThat(urn.getScheme(), is(ContactUrn.Scheme.TEL));
        assertThat(urn.getPath(), is("+260964153686"));
    }
}
