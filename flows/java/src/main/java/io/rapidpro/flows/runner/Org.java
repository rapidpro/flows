package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;

import java.time.ZoneId;

/**
 *
 */
public class Org {

    @SerializedName("primary_language")
    protected String m_primaryLanguage;

    @SerializedName("timezone")
    protected ZoneId m_timezone;

    @SerializedName("timezone")
    protected boolean m_dayFirst;

    @SerializedName("is_anon")
    protected boolean m_anon;

    public Org() {
    }

    public Org(String primaryLanguage, ZoneId timezone, boolean dayFirst, boolean anon) {
        m_primaryLanguage = primaryLanguage;
        m_timezone = timezone;
        m_dayFirst = dayFirst;
        m_anon = anon;
    }

    public String getPrimaryLanguage() {
        return m_primaryLanguage;
    }

    public ZoneId getTimezone() {
        return m_timezone;
    }

    public boolean isDayFirst() {
        return m_dayFirst;
    }

    public boolean isAnon() {
        return m_anon;
    }
}
