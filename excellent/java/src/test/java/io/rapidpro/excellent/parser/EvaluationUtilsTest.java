package io.rapidpro.excellent.parser;

import io.rapidpro.excellent.EvaluationError;
import io.rapidpro.excellent.functions.annotations.IntegerDefault;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static io.rapidpro.excellent.parser.EvaluationUtils.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link EvaluationUtils}
 */
public class EvaluationUtilsTest {

    @Test
    public void test_invokeFunction() {
        assertThat(invokeFunction(TestFunctions.class, "foo", new ArrayList<>(Arrays.asList(12))), is(24));
        assertThat(invokeFunction(TestFunctions.class, "FOO", new ArrayList<>(Arrays.asList(12))), is(24));
        assertThat(invokeFunction(TestFunctions.class, "bar", new ArrayList<>(Arrays.asList(12, 5))), is(17));
        assertThat(invokeFunction(TestFunctions.class, "bar", new ArrayList<>(Arrays.asList(12))), is(14));
        assertThat(invokeFunction(TestFunctions.class, "doh", new ArrayList<>(Arrays.asList(12, 1, 2, 3))), is(36));
    }

    @Test(expected = EvaluationError.class)
    public void test_invokeFunction_nonPublic() {
        invokeFunction(TestFunctions.class, "zed", new ArrayList<>(Arrays.asList(12)));

    }

    @Test
    public void test_slice() {
        assertThat(slice(Collections.emptyList(), null, null), empty());
        assertThat(slice(Arrays.asList(1, 2, 3, 4), 3, 2), empty());
        assertThat(slice(Arrays.asList(1, 2, 3, 4), 7, 9), empty());

        assertThat(slice(Arrays.asList(1, 2, 3, 4), null, null), contains(1, 2, 3, 4));
        assertThat(slice(Arrays.asList(1, 2, 3, 4), 1, null), contains(2, 3, 4));
        assertThat(slice(Arrays.asList(1, 2, 3, 4), 1, 3), contains(2, 3));
        assertThat(slice(Arrays.asList(1, 2, 3, 4), 1, -1), contains(2, 3));
        assertThat(slice(Arrays.asList(1, 2, 3, 4), -3, -1), contains(2, 3));
    }

    public static class TestFunctions {

        public static int foo(int a) {
            return a * 2;
        }

        public static int _bar(int a, @IntegerDefault(2) int b) {
            return a + b;
        }

        public static int doh(int a, Object... args) {
            return args.length * a;
        }

        private static int zed(int a) {
            return a / 2;
        }
    }
}
