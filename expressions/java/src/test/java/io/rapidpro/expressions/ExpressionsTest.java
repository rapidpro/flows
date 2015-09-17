package io.rapidpro.expressions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.evaluator.Evaluator;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Template tests
 */
public class ExpressionsTest {

    @Test
    public void templateTests() throws Exception {
        Evaluator evaluator = new EvaluatorBuilder()
                .withAllowedTopLevels(new String[] {"channel", "contact", "date", "extra", "flow", "step"})
                .build();

        InputStream in = ExpressionsTest.class.getClassLoader().getResourceAsStream("template_tests.json");

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(EvaluationContext.class, new EvaluationContext.Deserializer());
        Gson gson = builder.create();

        TemplateTest[] tests = gson.fromJson(new InputStreamReader(in), TemplateTest[].class);

        List<TemplateTest> failures = new ArrayList<>();
        long start = System.currentTimeMillis();

        // run the tests
        for (TemplateTest test : tests) {
            if (!test.run(evaluator)) {
                failures.add(test);
            }
        }

        long duration = System.currentTimeMillis() - start;

        System.out.println("Completed " + tests.length + " template tests in " + duration + "ms (failures=" + failures.size() + ")");

        if (!failures.isEmpty()) {
            System.out.println("Failed tests:");

            for (TemplateTest test : failures) {
                System.out.println("========================================");
                System.out.println("Template: " + test.template);
                if (test.expectedOutput != null) {
                    System.out.println("Expected output: " + test.expectedOutput);
                } else {
                    System.out.println("Expected output regex: " + test.expectedOutputRegex);
                }
                System.out.println("Actual output: " + test.actualOutput);
                System.out.println("Expected errors: " + StringUtils.join(test.expectedErrors, ", "));
                System.out.println("Actual errors: " + StringUtils.join(test.actualErrors, ", "));
            }

            fail("There were failures in the template tests");  // fail unit test if there were any errors
        }
    }

    protected class TemplateTest {
        @SerializedName("template") String template;
        @SerializedName("context") EvaluationContext context;
        @SerializedName("url_encode") boolean urlEncode;
        @SerializedName("output") String expectedOutput;
        @SerializedName("output_regex") String expectedOutputRegex;
        @SerializedName("errors") String[] expectedErrors;

        String actualOutput;
        List<String> actualErrors;

        public boolean run(Evaluator evaluator) {
            EvaluatedTemplate evaluated = evaluator.evaluateTemplate(template, context, urlEncode);
            this.actualOutput = evaluated.getOutput();
            this.actualErrors = evaluated.getErrors();

            if (expectedOutput != null) {
                if (!expectedOutput.equals(actualOutput)) {
                    return false;
                }
            } else {
                if (!Pattern.compile(expectedOutputRegex).matcher(actualOutput).matches()) {
                    return false;
                }
            }

            if (expectedErrors.length != actualErrors.size()) {
                return false;
            }
            for (int e = 0; e < expectedErrors.length; e++) {
                if (!expectedErrors[e].equals(actualErrors.get(e))) {
                    return false;
                }
            }
            return true;
        }
    }
}
