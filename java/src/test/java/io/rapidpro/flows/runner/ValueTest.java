package io.rapidpro.flows.runner;

import com.google.gson.JsonObject;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.utils.JsonUtils;
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

        JsonObject obj = (JsonObject) value.toJson();

        assertThat(obj, is(JsonUtils.object(
                "value", "no",
                "category", "No",
                "text", "no way!",
                "time", "2015-08-25T11:59:30.088Z"
        )));

        value = Value.fromJson(obj, null);

        assertThat(value.getValue(), is("no"));
        assertThat(value.getCategory(), is("No"));
        assertThat(value.getText(), is("no way!"));
        assertThat(value.getTime(), is(time));
    }
}
