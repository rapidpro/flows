package io.rapidpro.flows.runner;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.utils.JsonUtils;
import org.threeten.bp.ZoneId;

/**
 * An organization - used to provide additional information about how a flow should be run
 */
public class Org {

    @SerializedName("country")
    protected String m_country;

    @SerializedName("primary_language")
    protected String m_primaryLanguage;

    @SerializedName("timezone")
    @JsonAdapter(JsonUtils.TimezoneAdapter.class)
    protected ZoneId m_timezone;

    @SerializedName("date_style")
    protected DateStyle m_dateStyle;

    @SerializedName("anon")
    protected boolean m_anon;

    public Org() {
    }

    public Org(String country, String primaryLanguage, ZoneId timezone, DateStyle dateStyle, boolean anon) {
        m_country = country;
        m_primaryLanguage = primaryLanguage;
        m_timezone = timezone;
        m_dateStyle = dateStyle;
        m_anon = anon;
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
