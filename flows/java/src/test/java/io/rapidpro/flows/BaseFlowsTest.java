package io.rapidpro.flows;

import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.runner.Contact;
import io.rapidpro.flows.runner.ContactUrn;
import io.rapidpro.flows.runner.Org;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Base class for project tests
 */
@Ignore
public abstract class BaseFlowsTest {

    protected Org m_org;

    protected Contact m_contact;

    @Before
    public void initBaseData() throws Exception {
        m_org = new Org("eng", ZoneId.of("Africa/Kigali"), DateStyle.DAY_FIRST, false);

        Map<String, String> contactFields = new HashMap<>();
        contactFields.put("gender", "M");
        contactFields.put("age", "34");

        m_contact = new Contact(
                "1234-1234",
                "Joe Flow",
                Arrays.asList(
                        ContactUrn.fromString("tel:+260964153686"),
                        ContactUrn.fromString("twitter:realJoeFlow")
                ),
                new LinkedHashSet<>(Arrays.asList("Testers", "Developers")),
                contactFields,
                "eng"
        );
    }

    public String readResource(String resource) throws IOException {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public Org getOrg() {
        return m_org;
    }

    public Contact getContact() {
        return m_contact;
    }
}
