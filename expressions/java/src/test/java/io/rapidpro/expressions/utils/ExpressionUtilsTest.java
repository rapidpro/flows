package io.rapidpro.expressions.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static io.rapidpro.expressions.utils.ExpressionUtils.*;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link ExpressionUtils}
 */
public class ExpressionUtilsTest {

    @Test
    public void _slice() {
        assertThat(slice(Collections.emptyList(), null, null), empty());
        assertThat(slice(Arrays.asList(1, 2, 3, 4), 3, 2), empty());
        assertThat(slice(Arrays.asList(1, 2, 3, 4), 7, 9), empty());

        assertThat(slice(Arrays.asList(1, 2, 3, 4), null, null), contains(1, 2, 3, 4));
        assertThat(slice(Arrays.asList(1, 2, 3, 4), 1, null), contains(2, 3, 4));
        assertThat(slice(Arrays.asList(1, 2, 3, 4), 1, 3), contains(2, 3));
        assertThat(slice(Arrays.asList(1, 2, 3, 4), 1, -1), contains(2, 3));
        assertThat(slice(Arrays.asList(1, 2, 3, 4), -3, -1), contains(2, 3));
    }

    @Test
    public void _tokenize() {
        assertThat(tokenize("this is a sentence"), arrayContaining("this", "is", "a", "sentence"));
        assertThat(tokenize("  hey  \t@ there  "), arrayContaining("hey", "there"));
        assertThat(tokenize("واحد اثنين ثلاثة"), arrayContaining("واحد", "اثنين", "ثلاثة"));
    }
}
