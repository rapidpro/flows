package io.rapidpro.expressions.utils;

import io.rapidpro.expressions.functions.annotations.BooleanDefault;
import io.rapidpro.expressions.functions.annotations.IntegerDefault;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link ParameterTest}
 */
public class ParameterTest {

    @Test
    public void fromMethod() throws Exception {
        Parameter[] params = Parameter.fromMethod(getMethod("foo"));
        assertThat(params, arrayWithSize(3));
        assertThat(params[0].getType(), equalTo((Class) int.class));
        assertThat(params[0].getAnnotations(), emptyArray());
        assertThat(params[1].getType(), equalTo((Class) String.class));
        assertThat(params[2].getType(), equalTo((Class) boolean.class));

        params = Parameter.fromMethod(getMethod("bar"));
        assertThat(params, arrayWithSize(1));
        assertThat(params[0].getType(), equalTo((Class) Object[].class));
        assertThat(params[0].getAnnotations(), emptyArray());

        params = Parameter.fromMethod(getMethod("zed"));
        assertThat(params, arrayWithSize(1));
        assertThat(params[0].getType(), equalTo((Class) int.class));
        assertThat(params[0].getAnnotations(), arrayWithSize(1));
        assertThat(params[0].getAnnotation(IntegerDefault.class), instanceOf(IntegerDefault.class));
        assertThat(params[0].getAnnotation(BooleanDefault.class), nullValue());
    }

    protected void foo(int a, String b, boolean c) {
    }

    protected void bar(Object... args) {
    }

    protected void zed(@IntegerDefault(123) int val) {
    }

    protected Method getMethod(String name) {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
}
