package io.rapidpro.flows.runner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;
import org.threeten.bp.ZoneId;

/**
 * An organization - used to provide additional information about how a flow should be run
 */
public class Org implements Jsonizable {

    protected String m_country;

    protected String m_primaryLanguage;

    protected ZoneId m_timezone;

    protected DateStyle m_dateStyle;

    protected boolean m_anon;

    public Org(String country, String primaryLanguage, ZoneId timezone, DateStyle dateStyle, boolean anon) {
        m_country = country;
        m_primaryLanguage = primaryLanguage;
        m_timezone = timezone;
        m_dateStyle = dateStyle;
        m_anon = anon;
    }

    public static Org fromJson(JsonElement elm) {
        JsonObject obj = elm.getAsJsonObject();
        return new Org(
                obj.get("country").getAsString(),
                obj.get("primary_language").getAsString(),
                ZoneId.of(obj.get("timezone").getAsString()),
                DateStyle.valueOf(obj.get("date_style").getAsString().toUpperCase()),
                obj.get("anon").getAsBoolean()
        );
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object(
                "country", m_country,
                "primary_language", m_primaryLanguage,
                "timezone", m_timezone.getId(),
                "date_style", m_dateStyle.name().toLowerCase(),
                "anon", m_anon
        );
    }

    public String getCountry() {
        return m_country;
    }

    public String getPrimaryLanguage() {
        return m_primaryLanguage;
    }

    public ZoneId getTimezone() {
        return m_timezone;
    }

    public DateStyle getDateStyle() {
        return m_dateStyle;
    }

    public boolean isAnon() {
        return m_anon;
    }
}
