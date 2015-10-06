package io.rapidpro.flows.runner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.BaseFlowsTest;
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
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(m_org);

        assertThat(json, is("{" +
                "\"country\":\"RW\"," +
                "\"primary_language\":\"eng\"," +
                "\"timezone\":\"Africa/Kigali\"," +
                "\"date_style\":\"day_first\"," +
                "\"anon\":false" +
        "}"));

        Org org = gson.fromJson(json, Org.class);

        assertThat(org.getCountry(), is("RW"));
        assertThat(org.getPrimaryLanguage(), is("eng"));
        assertThat(org.getTimezone(), is(ZoneId.of("Africa/Kigali")));
        assertThat(org.getDateStyle(), is(DateStyle.DAY_FIRST));
        assertThat(org.isAnon(), is(false));
    }
}
