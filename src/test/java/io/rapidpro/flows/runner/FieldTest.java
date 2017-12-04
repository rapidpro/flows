package io.rapidpro.flows.runner;

import com.google.gson.JsonObject;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Field}
 */
public class FieldTest extends BaseFlowsTest {

    @Test
    public void toAndFromJson() {
        JsonObject obj = (JsonObject) m_fields.get(0).toJson();

        assertThat(obj, is(JsonUtils.object(
                "key", "gender",
                "label", "Gender",
                "value_type", "T"
        )));

        Field field = Field.fromJson(obj);

        assertThat(field.getKey(), is("gender"));
        assertThat(field.getLabel(), is("Gender"));
        assertThat(field.getValueType(), is(Field.ValueType.TEXT));
    }

    @Test
    public void makeKey() {
        assertThat(Field.makeKey("First Name"), is("first_name"));
        assertThat(Field.makeKey("Second   Name  "), is("second_name"));
        assertThat(Field.makeKey("  ^%$# %$$ $##323 ffsn slfs ksflskfs!!!! fk$%%%$$$anfaDDGAS ))))))))) "), is("323_ffsn_slfs_ksflskfs_fk_anfaddgas"));
    }

    @Test
    public void isValidKey() {
        assertThat(Field.isValidKey("age"), is(true));
        assertThat(Field.isValidKey("age_now_2"), is(true));
        assertThat(Field.isValidKey("Age"), is(false));  // must be lowercase
        assertThat(Field.isValidKey("age!"), is(false)); // can 't have punctuation
        assertThat(Field.isValidKey("âge"), is(false));  // a - z only
        assertThat(Field.isValidKey("2up"), is(false));  // can 't start with a number
        assertThat(Field.isValidKey("name"), is(false)); // can 't be a reserved name
        assertThat(Field.isValidKey("uuid"), is(false));
    }

    @Test
    public void isValidLabel() {
        assertThat(Field.isValidLabel("Age"), is(true));
        assertThat(Field.isValidLabel("Age Now 2"), is(true));
        assertThat(Field.isValidLabel("Age_Now"), is(false)); // can 't have punctuation
        assertThat(Field.isValidLabel("âge"), is(false)); // a - z only
    }
}
