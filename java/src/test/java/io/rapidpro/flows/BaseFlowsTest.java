package io.rapidpro.flows;

import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.group.AddToGroupsAction;
import io.rapidpro.flows.definition.actions.message.ReplyAction;
import io.rapidpro.flows.runner.*;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.threeten.bp.ZoneId;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Base class for project tests
 */
@Ignore
public abstract class BaseFlowsTest {

    protected Org m_org;

    protected List<Field> m_fields;

    protected Contact m_contact;

    @Before
    public void initBaseData() throws Exception {
        m_org = new Org("RW", "eng", ZoneId.of("Africa/Kigali"), DateStyle.DAY_FIRST, false);

        m_fields = new ArrayList<>(Arrays.asList(
                new Field("gender", "Gender", Field.ValueType.TEXT),
                new Field("age", "Age", Field.ValueType.DECIMAL),
                new Field("joined", "Joined", Field.ValueType.DATETIME)
        ));

        Map<String, String> contactFieldValues = new HashMap<>();
        contactFieldValues.put("gender", "M");
        contactFieldValues.put("age", "34");
        contactFieldValues.put("joined", "2015-10-06T11:30:01.123Z");

        m_contact = new Contact(
                "1234-1234",
                "Joe Flow",
                new ArrayList<>(Arrays.asList(
                        ContactUrn.fromString("tel:+260964153686"),
                        ContactUrn.fromString("twitter:realJoeFlow")
                )),
                new LinkedHashSet<>(Arrays.asList("Testers", "Developers")),
                contactFieldValues,
                "eng"
        );
    }

    public String readResource(String resource) throws IOException {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    /**
     * Location resolver for testing which has one state (Kigali) and one district (Gasabo)
     */
    public static class TestLocationResolver implements Location.Resolver {
        private static Location m_kigali = new Location("S0001", "Kigali", Location.Level.STATE);
        private static Location m_gasabo = new Location("D0001", "Gasabo", Location.Level.DISTRICT);

        @Override
        public Location resolve(String text, String country, Location.Level level, Location parent) {
            if (level == Location.Level.STATE && text.trim().equalsIgnoreCase("Kigali")) {
                return m_kigali;
            } else if (level == Location.Level.DISTRICT && text.trim().equalsIgnoreCase("Gasabo") && parent.equals(m_kigali)) {
                return m_gasabo;
            } else {
                return null;
            }
        }
    }

    protected void assertReply(Action action, String msg) {
        assertThat(action, instanceOf(ReplyAction.class));
        assertThat(((ReplyAction) action).getMsg(), is(new TranslatableText(msg)));
    }

    protected void assertAddToGroup(Action action, String... groupNames) {
        assertThat(action, instanceOf(AddToGroupsAction.class));

        List<String> names = new ArrayList<>();
        for (GroupRef group : ((AddToGroupsAction) action).getGroups()) {
            names.add(group.getName());
        }
        assertThat(names, is(Arrays.asList(groupNames)));
    }
}
