package io.rapidpro.flows.utils;

import io.rapidpro.flows.BaseFlowsTest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link FlowUtils}
 */
public class FlowUtilsTest extends BaseFlowsTest {

    @Test
    public void normalizeNumber() {
        // valid numbers
        assertThat(FlowUtils.normalizeNumber("0788383383", "RW"), is((Pair) new ImmutablePair<>("+250788383383", true)));
        assertThat(FlowUtils.normalizeNumber("+250788383383", "KE"), is((Pair) new ImmutablePair<>("+250788383383", true)));
        assertThat(FlowUtils.normalizeNumber("+250788383383", null), is((Pair) new ImmutablePair<>("+250788383383", true)));
        assertThat(FlowUtils.normalizeNumber("250788383383", null), is((Pair) new ImmutablePair<>("+250788383383", true)));
        assertThat(FlowUtils.normalizeNumber("2.50788383383E+11", null), is((Pair) new ImmutablePair<>("+250788383383", true)));
        assertThat(FlowUtils.normalizeNumber("2.50788383383E+12", null), is((Pair) new ImmutablePair<>("+250788383383", true)));
        assertThat(FlowUtils.normalizeNumber("(917) 992-5253", "US"), is((Pair) new ImmutablePair<>("+19179925253", true)));
        assertThat(FlowUtils.normalizeNumber("19179925253", null), is((Pair) new ImmutablePair<>("+19179925253", true)));
        assertThat(FlowUtils.normalizeNumber("+62877747666", null), is((Pair) new ImmutablePair<>("+62877747666", true)));
        assertThat(FlowUtils.normalizeNumber("62877747666", "ID"), is((Pair) new ImmutablePair<>("+62877747666", true)));
        assertThat(FlowUtils.normalizeNumber("0877747666", "ID"), is((Pair) new ImmutablePair<>("+62877747666", true)));

        // invalid numbers
        assertThat(FlowUtils.normalizeNumber("12345", "RW"), is((Pair) new ImmutablePair<>("12345", false)));
        assertThat(FlowUtils.normalizeNumber("0788383383", null), is((Pair) new ImmutablePair<>("0788383383", false)));
        assertThat(FlowUtils.normalizeNumber("0788383383", "ZZ"), is((Pair) new ImmutablePair<>("0788383383", false)));
        assertThat(FlowUtils.normalizeNumber("MTN", "RW"), is((Pair) new ImmutablePair<>("mtn", false)));
    }
}
