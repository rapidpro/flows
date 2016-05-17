package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link ContainsAnyTest}
 */
public class ContainsAnyTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "contains_any", "test", "yes,affirmative");
        ContainsAnyTest test = (ContainsAnyTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.getTest(), is(new TranslatableText("yes,affirmative")));

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        ContainsAnyTest test = new ContainsAnyTest(new TranslatableText("eng", "yes,affirmative", "fre", "non"));

        assertTest(test, "yes", true, "yes");
        assertTest(test, "AFFIRMATIVE SIR", true, "AFFIRMATIVE");
        assertTest(test, "affirmative yes", true, "affirmative yes");
        assertTest(test, "afirmative!", true, "afirmative"); // edit distance

        // edit distance doesn't apply for words shorter than 4 chars
        assertTest(test, "Ok YEES I will", false, null);

        assertTest(test, "no", false, null);
        assertTest(test, "NO way jose", false, null);

        test = new ContainsAnyTest(new TranslatableText("klab Kacyiru good"));
        assertTest(test, "kLab is awesome", true, "kLab");
        assertTest(test, "telecom is located at Kacyiru", true, "Kacyiru");
        assertTest(test, "good morning", true, "good");
        assertTest(test, "kLab is good", true, "kLab good");
        assertTest(test, "kigali city", false, null);

        // have the same behaviour when we have commas even a trailing one
        test = new ContainsAnyTest(new TranslatableText("klab, kacyiru, good, "));
        assertTest(test, "kLab is awesome", true, "kLab");
        assertTest(test, "telecom is located at Kacyiru", true, "Kacyiru");
        assertTest(test, "good morning", true, "good");
        assertTest(test, "kLab is good", true, "kLab good");
        assertTest(test, "kigali city", false, null);

        test = new ContainsAnyTest(new TranslatableText("north,North"));

        assertTest(test, "go north NORTH", true, "north NORTH");
        assertTest(test, "EAST then NORRTH", true, "NORRTH");

        assertTest(test, "go north", true, "north");
        assertTest(test, "east", false, null);
    }
}
