package io.rapidpro.flows;

import io.rapidpro.flows.runner.Contact;
import io.rapidpro.flows.runner.ContactUrn;
import io.rapidpro.flows.runner.Org;
import org.junit.Before;
import org.junit.Ignore;

import java.time.ZoneId;
import java.util.*;

/**
 * Base class for project tests
 */
@Ignore
public abstract class BaseFlowsTest {

    protected Org m_org;

    protected Contact m_contact;

    @Before
    public void setup() throws Exception {
        m_org = new Org("eng", ZoneId.of("Africa/Kigali"), true, false);

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
                new HashSet<>(Collections.singleton("Testers")),
                contactFields,
                "eng"
        );
    }
}
