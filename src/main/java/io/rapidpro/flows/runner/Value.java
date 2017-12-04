package io.rapidpro.flows.runner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.expressions.utils.ExpressionUtils;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;
import org.threeten.bp.Instant;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the result of a contact's last visit to a ruleset
 */
public class Value implements Jsonizable {

    protected String m_value;

    protected String m_category;

    protected String m_text;

    protected Instant m_time;

    public Value(String value, String category, String text, Instant time) {
        m_value = value;
        m_category = category;
        m_text = text;
        m_time = time;
    }

    public static Value fromJson(JsonElement elm) {
        JsonObject obj = elm.getAsJsonObject();
        return new Value(
                obj.get("value").getAsString(),
                obj.get("category").getAsString(),
                obj.get("text").getAsString(),
                ExpressionUtils.parseJsonDate(obj.get("time").getAsString())
        );
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object(
                "value", m_value,
                "category", m_category,
                "text", m_text,
                "time", ExpressionUtils.formatJsonDate(m_time)
        );
    }

    public Map<String, String> buildContext(EvaluationContext container) {
        Map<String, String> context = new HashMap<>();
        context.put("*", m_value);
        context.put("value", m_value);
        context.put("category", m_category);
        context.put("text", m_text);
        context.put("time", Conversions.toString(m_time.atZone(container.getTimezone()), container));
        return context;
    }

    public String getValue() {
        return m_value;
    }

    public String getCategory() {
        return m_category;
    }

    public String getText() {
        return m_text;
    }

    public Instant getTime() {
        return m_time;
    }
}
