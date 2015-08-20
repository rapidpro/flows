package io.rapidpro.flows;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link FlowUtils}
 */
public class FlowUtilsTest {

    @Test
    public void editDistance() {
        assertThat(FlowUtils.editDistance("", ""), is(0));
        assertThat(FlowUtils.editDistance("abcd", "abcd"), is(0));    // 0 differences
        assertThat(FlowUtils.editDistance("abcd", "abc"), is(1));     // 1 deletion
        assertThat(FlowUtils.editDistance("abcd", "ad"), is(2));      // 2 deletions
        assertThat(FlowUtils.editDistance("abcd", "axbcd"), is(1));   // 1 addition
        assertThat(FlowUtils.editDistance("abcd", "acbd"), is(1));    // 1 transposition
        assertThat(FlowUtils.editDistance("abcd", "xabcd"), is(100)); // starts with different character
    }
}
