package io.rapidpro.flows.runner;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;
import org.threeten.bp.ZoneId;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Org}
 */
public class OrgTest extends BaseFlowsTest {

    @Test
    public void toAndFromJson() {
        JsonObject obj = (JsonObject) m_org.toJson();

        assertThat(obj, is(JsonUtils.object(
                "country", "RW",
                "primary_language", "eng",
                "timezone", "Africa/Kigali",
                "date_style", "day_first",
                "anon", false
        )));

        Org org = Org.fromJson(obj);

        assertThat(org.getCountry(), is("RW"));
        assertThat(org.getPrimaryLanguage(), is("eng"));
        assertThat(org.getTimezone(), is(ZoneId.of("Africa/Kigali")));
        assertThat(org.getDateStyle(), is(DateStyle.DAY_FIRST));
        assertThat(org.isAnon(), is(false));
    }
}
