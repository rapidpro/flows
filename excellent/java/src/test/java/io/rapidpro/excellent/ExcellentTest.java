package io.rapidpro.excellent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for {@link Excellent}
 */
public class ExcellentTest {

    @Test
    public void getTemplateEvaluator() {
        assertThat(Excellent.getTemplateEvaluator(), instanceOf(Excellent.TemplateEvaluator.class));
    }

    @Test
    public void templateTests() throws Exception {
        Excellent.TemplateEvaluator evaluator = Excellent.getTemplateEvaluator();

        InputStream in = ExcellentTest.class.getClassLoader().getResourceAsStream("template_tests.json");

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
                System.out.println("Expected output: " + test.expectedOutput);
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
        @SerializedName("errors") String[] expectedErrors;

        String actualOutput;
        List<String> actualErrors;

        public boolean run(Excellent.TemplateEvaluator evaluator) {
            EvaluatedTemplate evaluated = evaluator.evaluateTemplate(template, context, urlEncode);
            this.actualOutput = evaluated.getOutput();
            this.actualErrors = evaluated.getErrors();

            if (!expectedOutput.equals(actualOutput)) {
                return false;
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
