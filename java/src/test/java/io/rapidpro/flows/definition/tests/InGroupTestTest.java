package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.utils.JsonUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InGroupTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "in_group", "name", "Subscribers");
        InGroupTest test = (InGroupTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {

        InGroupTest test = new InGroupTest(new GroupRef("123", "Subscribers"));
        assertTest(test, "", false, null);

        // add us to the subscribers group and it should evaluate to true
        m_run.getContact().getGroups().add("Subscribers");
        assertTest(test, "", true, "Subscribers");
    }
}
