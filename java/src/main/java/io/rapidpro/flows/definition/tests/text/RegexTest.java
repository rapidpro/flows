package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Test that returns whether the input matches a regular expression
 */
public class RegexTest extends TranslatableTest {

    public static final String TYPE = "regex";

    protected RegexTest(TranslatableText test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static RegexTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new RegexTest(TranslatableText.fromJson(obj.get("test")));
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "test", m_test.toJson());
    }

    /**
     * @see TranslatableTest#evaluateForLocalized(Runner, RunState, EvaluationContext, String, String)
     */
    @Override
    protected Result evaluateForLocalized(Runner runner, RunState run, EvaluationContext context, String text, String localizedTest) {
        try {
            Map<String, String> groupNames = new HashMap<>();
            String javaRegex = pythonToJavaRegex(localizedTest, groupNames);

            // check whether we match
            int flags = getPatternUnicodeFlag() | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
            Pattern regex = Pattern.compile(javaRegex, flags);
            Matcher matcher = regex.matcher(text);

            if (matcher.find()) {
                String returnMatch = matcher.group(0);

                Map<String, String> groupValues = new HashMap<>();

                // add group values by name
                for (Map.Entry<String, String> entry : groupNames.entrySet()) {
                    String replacedName = entry.getKey();
                    String originalName = entry.getValue();

                    String value = matcher.group(replacedName);
                    groupValues.put(originalName, value);
                }

                // add group values by index
                for (int g = 0; g <= matcher.groupCount(); g++) {
                    String value = matcher.group(g);
                    groupValues.put(String.valueOf(g), value);
                }

                // update @extra
                runner.updateExtra(run, groupValues);

                return Result.match(returnMatch);
            }
        } catch (PatternSyntaxException ignored) {}

        return Result.NO_MATCH;
    }

    /**
     * Converts a Python-style regular expression to a Java-style one. Replaces all group names with name1, name2 ..
     * as Java is stricter about which characters can occur in a group name.
     * @param pythonStyle the Python style regular expression, e.g. "(?P<first_name>\w+)"
     * @param groupNames an empty map which will receive replacement and original group names
     * @return the Java style regular expression, e.g. "(?<name1>\w+)"
     */
    protected static String pythonToJavaRegex(String pythonStyle, Map<String, String> groupNames) {
        StringBuffer javaStyle = new StringBuffer();

        Matcher namedGroups = Pattern.compile("\\(\\?P<(\\w+)>").matcher(pythonStyle);
        int groupNum = 1;
        while (namedGroups.find()) {
            String name = namedGroups.group(1);
            String nameReplacement = "name" + groupNum;

            groupNames.put(nameReplacement, name);

            name = nameReplacement;
            groupNum += 1;

            namedGroups.appendReplacement(javaStyle, "(?<" + name + ">");
        }

        namedGroups.appendTail(javaStyle);
        return javaStyle.toString();
    }

    /**
     * Gets the value of UNICODE_CHARACTER_CLASS if it exists (Java SE) or zero if it's implied and isn't defined (Android)
     * @return the field value or zero
     */
    protected static int getPatternUnicodeFlag() {
        try {
            Field field = Pattern.class.getField("UNICODE_CHARACTER_CLASS");
            return field.getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return 0;
        }
    }
}
