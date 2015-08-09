package io.rapidpro.excellent;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Excellent}
 */
public class ExcellentTest {

    @Test
    public void getTemplateEvaluator() {
        assertThat(Excellent.getTemplateEvaluator(), instanceOf(Excellent.TemplateEvaluator.class));
    }

    @Test
    public void standardDataSetTest() throws Exception {
        Excellent.TemplateEvaluator evaluator = Excellent.getTemplateEvaluator();

        InputStream in = ExcellentTest.class.getClassLoader().getResourceAsStream("tests.csv");
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(new InputStreamReader(in));

        List<TemplateTest> tests = new ArrayList<>();
        List<TemplateTest> failures = new ArrayList<>();

        // load tests from spreadsheet
        boolean isHeader = true;
        for (CSVRecord record : records) {
            if (isHeader) {
                isHeader = false;
                continue;
            }
            tests.add(new TemplateTest(
                    record.get(0),
                    EvaluationContext.fromJson(record.get(1)),
                    record.get(2).equals("Y"),
                    record.get(3),
                    record.get(4)
            ));
        }

        long start = System.currentTimeMillis();

        // run the tests
        for (TemplateTest test : tests) {
            if (!test.run(evaluator)) {
                failures.add(test);
            }
        }

        long duration = System.currentTimeMillis() - start;

        System.out.println("Completed " + tests.size() + " template tests in " + duration + "ms (failures=" + failures.size() + ")");
        System.out.println("Failed tests:\n");

        for (TemplateTest test : failures) {
            System.out.println("Template: " + test.template);
            if (!test.expectedOutput.equals(test.actualOutput)) {
                System.out.println("Expected output: " + test.expectedOutput);
                System.out.println("Actual output: " + test.actualOutput);
            }
            if (!test.expectedError.equals(test.actualError)) {
                System.out.println("Expected error: " + test.expectedError);
                System.out.println("Actual error: " + test.actualError);
            }
            System.out.println();
        }

        // fail unit test if there were any errors
        assertThat(failures, empty());
    }

    protected class TemplateTest {
        String template;
        EvaluationContext context;
        boolean urlEncode;

        String expectedOutput;
        String expectedError;

        String actualOutput;
        String actualError;

        public TemplateTest(String template, EvaluationContext context, boolean urlEncode, String expectedOutput, String expectedError) {
            this.template = template;
            this.context = context;
            this.urlEncode = urlEncode;
            this.expectedOutput = expectedOutput;
            this.expectedError = expectedError;
        }

        public boolean run(Excellent.TemplateEvaluator evaluator) {
            EvaluatedTemplate evaluated = evaluator.evaluateTemplate(template, context, urlEncode);
            this.actualOutput = evaluated.getOutput();
            this.actualError = evaluated.getErrors().isEmpty() ? "" : evaluated.getErrors().get(0);

            return actualOutput.equals(expectedOutput) && actualError.equals(expectedError);
        }
    }
}
