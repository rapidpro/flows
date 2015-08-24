package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.dates.DateStyle;

import java.time.ZoneId;

/**
 * An organization - used to provide additional information about how a flow should be run
 */
public class Org {

    @SerializedName("primary_language")
    protected String m_primaryLanguage;

    @SerializedName("timezone")
    protected ZoneId m_timezone;

    @SerializedName("date_style")
    protected DateStyle m_dateStyle;

    @SerializedName("is_anon")
    protected boolean m_anon;

    public Org() {
    }

    public Org(String primaryLanguage, ZoneId timezone, DateStyle dateStyle, boolean anon) {
        m_primaryLanguage = primaryLanguage;
        m_timezone = timezone;
        m_dateStyle = dateStyle;
        m_anon = anon;
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
