package io.rapidpro.expressions;

import io.rapidpro.expressions.evaluator.TemplateEvaluator;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link EvaluatorBuilder}
 */
public class EvaluatorBuilderTest {

    @Test
    public void build() {
        TemplateEvaluator evaluator = new EvaluatorBuilder()
                .withExpressionPrefix('=')
                .addFunctionLibrary(TestLibrary.class)
                .build();

        EvaluatedTemplate template = evaluator.evaluateTemplate("=(foo())", new EvaluationContext());
        assertThat(template.getOutput(), is("FOO!"));
    }

    public static class TestLibrary {
        public static String foo() {
            return "FOO!";
        }
    }
}
