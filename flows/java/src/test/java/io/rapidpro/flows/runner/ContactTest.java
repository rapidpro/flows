package io.rapidpro.flows.runner;

import io.rapidpro.flows.BaseFlowsTest;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Contact}
 */
public class ContactTest extends BaseFlowsTest {

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
    }
}
