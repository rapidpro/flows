package io.rapidpro.flows.definition.tests.location;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link HasWardTest}
 */
public class HasWardTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "ward", "state", "Kigali", "district", "Gasabo");
        HasWardTest test = (HasWardTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.getState(), is("Kigali"));
        assertThat(test.getDistrict(), is("Gasabo"));
        assertThat(test.toJson(), is(elm));
    }
}
