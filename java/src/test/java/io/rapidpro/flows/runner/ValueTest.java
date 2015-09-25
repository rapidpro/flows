package io.rapidpro.flows.runner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rapidpro.flows.BaseFlowsTest;
import org.junit.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Value}
 */
public class ValueTest extends BaseFlowsTest {

    @Test
    public void toAndFromJson() {
        Instant time = Instant.from(ZonedDateTime.of(2015, 8, 25, 11, 59, 30, 88 * 1000000, ZoneId.of("UTC")));
        Value value = new Value("no", "No", "no way!", time);

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(value);

        assertThat(json, is("{" +
                "\"value\":\"no\"," +
                "\"category\":\"No\"," +
                "\"text\":\"no way!\"," +
                "\"time\":\"2015-08-25T11:59:30.088Z\"" +
        "}"));

        value = gson.fromJson(json, Value.class);

        assertThat(value.getValue(), is("no"));
        assertThat(value.getCategory(), is("No"));
        assertThat(value.getText(), is("no way!"));
        assertThat(value.getTime(), is(time));
    }
}
