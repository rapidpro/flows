package io.rapidpro.excellent;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
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

        int numTests = 0;
        long start = System.currentTimeMillis();
        for (CSVRecord record : records) {
            String template = record.get(0);
            String expectedOutput = record.get(1);
            EvaluationContext context = EvaluationContext.fromJson(record.get(2));
            boolean urlEncode = record.get(3).equals("Y");
            EvaluatedTemplate evaluated = evaluator.evaluateTemplate(template, context, urlEncode);

            assertThat(evaluated.getOutput(), is(expectedOutput));
            numTests++;
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Completed " + numTests + " standard expression tests in " + duration + "ms");
    }
}
