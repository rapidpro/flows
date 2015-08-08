package io.rapidpro.excellent.functions;

import io.rapidpro.excellent.EvaluationError;
import io.rapidpro.excellent.functions.annotations.IntegerDefault;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link FunctionManager}
 */
public class FunctionManagerTest {

    @Test
    public void test_invokeFunction() {
        FunctionManager manager = new FunctionManager();
        manager.addLibrary(TestFunctions.class);

        assertThat(manager.invokeFunction("foo", new ArrayList<>(Arrays.asList(12))), is(24));
        assertThat(manager.invokeFunction("FOO", new ArrayList<>(Arrays.asList(12))), is(24));
        assertThat(manager.invokeFunction("bar", new ArrayList<>(Arrays.asList(12, 5))), is(17));
        assertThat(manager.invokeFunction("bar", new ArrayList<>(Arrays.asList(12))), is(14));
        assertThat(manager.invokeFunction("doh", new ArrayList<>(Arrays.asList(12, 1, 2, 3))), is(36));
    }

    @Test(expected = EvaluationError.class)
    public void test_invokeFunction_nonPublic() {
        FunctionManager manager = new FunctionManager();
        manager.addLibrary(TestFunctions.class);
        manager.invokeFunction( "zed", new ArrayList<>(Arrays.asList(12)));
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
