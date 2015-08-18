package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RuleSet extends Flow.Node {

    protected List<Rule> m_rules = new ArrayList<>();

    public static RuleSet fromJson(JsonObject json, Map<Flow.ConnectionStart, String> destinationsToSet) {
        RuleSet ruleSet = new RuleSet();
        ruleSet.m_uuid = json.get("uuid").getAsString();

        for (JsonElement ruleElem : json.get("rules").getAsJsonArray()) {
            ruleSet.m_rules.add(Rule.fromJson(ruleElem.getAsJsonObject(), destinationsToSet));
        }
        return ruleSet;
    }
}
