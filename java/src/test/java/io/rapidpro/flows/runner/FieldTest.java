package io.rapidpro.flows.runner;

import io.rapidpro.flows.BaseFlowsTest;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Field}
 */
public class FieldTest extends BaseFlowsTest {

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
