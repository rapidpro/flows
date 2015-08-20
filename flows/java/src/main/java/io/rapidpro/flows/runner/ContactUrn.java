package io.rapidpro.flows.runner;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class ContactUrn {

    public enum Scheme {
        TEL,
        TWITTER
    }

    protected static String ANON_MASK = "********";

    private Scheme m_scheme;

    private String m_path;

    public ContactUrn(Scheme scheme, String path) {
        m_scheme = scheme;
        m_path = path;
    }

    /**
     * Parses a URN from a string
     * @param urn the string, e.g. tel:+260964153686, twitter:joe
     * @return the parsed URN
     */
    public static ContactUrn parse(String urn) {
        String[] parts = urn.split(":", 2);
        Scheme scheme = Scheme.valueOf(parts[0].toUpperCase());
        return new ContactUrn(scheme, parts[1]);
    }

    public Scheme getScheme() {
        return m_scheme;
    }

    public String getPath() {
        return m_path;
    }

    public String getDisplay(Org org, boolean full) {
        if (org.isAnon()) {
            return ANON_MASK;
        }

        if (m_scheme == Scheme.TEL && !full) {
            // if we don't want a full tell, see if we can show the national format instead
            try {
                if (StringUtils.isNotEmpty(m_path) && m_path.charAt(0) == '+') {
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    return phoneUtil.format(phoneUtil.parse(m_path, null), PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                }
            }
            catch (Exception ignored) {}
        }

        return m_path;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return m_scheme + ":" + m_path;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContactUrn that = (ContactUrn) o;

        if (m_scheme != that.m_scheme) return false;
        return m_path.equals(that.m_path);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = m_scheme.hashCode();
        result = 31 * result + m_path.hashCode();
        return result;
    }
}
