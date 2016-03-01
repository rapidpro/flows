package io.rapidpro.flows.definition.tests.location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.Location;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Test that returns whether the text contains a valid ward in the given district in the given state
 */
public class HasWardTest extends Test {

    public static final String TYPE = "ward";

    protected String m_state;
    protected String m_district;

    public HasWardTest(String state, String district) {
        m_state = state;
        m_district = district;
    }

    /**
     * @see Test#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static HasWardTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new HasWardTest(obj.get("state").getAsString(), obj.get("district").getAsString());
    }

    @Override
    public JsonElement toJson() {
    return JsonUtils.object("type", TYPE, "state", m_state, "district", m_district);
  }

    /**
     * @see Test#evaluate(Runner, RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(Runner runner, RunState run, EvaluationContext context, String text) {
        String country = run.getOrg().getCountry();
        if (StringUtils.isNotEmpty(country)) {
            // state and district might be an expression
            EvaluatedTemplate stateTpl = runner.substituteVariables(m_state, context);
            EvaluatedTemplate districtTpl = runner.substituteVariables(m_district, context);

            if (!stateTpl.hasErrors() && !districtTpl.hasErrors()) {
                Location state = runner.parseLocation(stateTpl.getOutput(), country, Location.Level.STATE, null);
                System.out.println(state);
                if (state != null) {
                    Location district = runner.parseLocation(text, country, Location.Level.DISTRICT, state);
                    System.out.println(district);
                    if (district != null) {
                        Location ward = runner.parseLocation(text, country, Location.Level.WARD, district);
                        if (ward != null) {
                            return Result.match(ward.getName());
                        }
                    }
                }
            }
        }

        return Result.NO_MATCH;
    }

    public String getState() {
        return m_state;
    }
    public String getDistrict() {
        return m_district;
    }
}
