package io.rapidpro.flows.runner;

import com.google.gson.JsonObject;
import io.rapidpro.flows.FlowUtils;

/**
 *
 */
public class Contact {
    protected String m_name;

    protected String m_language;

    // TODO

    public static Contact fromJson(JsonObject json) {
        Contact obj = new Contact();
        obj.m_name = FlowUtils.getAsString(json, "name");
        obj.m_language = FlowUtils.getAsString(json, "language");
        return obj;
    }

    public String getName() {
        return m_name;
    }

    public String getLanguage() {
        return m_language;
    }
}
