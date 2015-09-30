package io.rapidpro.flows.runner;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import io.rapidpro.flows.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * A contact that can participate in a flow
 */
public class Contact {

    @SerializedName("uuid")
    protected String m_uuid;

    @SerializedName("name")
    protected String m_name;

    @SerializedName("urns")
    protected List<ContactUrn> m_urns;

    @SerializedName("groups")
    protected Set<String> m_groups;

    @SerializedName("fields")
    protected Map<String, String> m_fields;

    @SerializedName("language")
    protected String m_language;

    public Contact() {
        m_urns = new ArrayList<>();
        m_groups = new HashSet<>();
        m_fields = new HashMap<>();
    }

    public Contact(String uuid, String name, ContactUrn urn, String language) {
        this(uuid, name, new ArrayList<>(Collections.singletonList(urn)),
                new HashSet<String>(), new HashMap<String, String>(), language);
    }

    public Contact(String uuid, String name, List<ContactUrn> urns, Set<String> groups, Map<String, String> fields, String language) {
        m_uuid = uuid;
        m_name = name;
        m_urns = urns;
        m_groups = groups;
        m_fields = fields;
        m_language = language;
    }

    public String getUuid() {
        return m_uuid;
    }

    public void setUuid(String uuid) {
        m_uuid = uuid;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public List<ContactUrn> getUrns() {
        return m_urns;
    }

    public Set<String> getGroups() {
        return m_groups;
    }

    public Map<String, String> getFields() {
        return m_fields;
    }

    public void setField(String key, String value) {
        m_fields.put(key, value);
    }

    public String getLanguage() {
        return m_language;
    }

    public void setLanguage(String language) {
        m_language = language;
    }

    public String getFirstName(Org org) {
        if (StringUtils.isEmpty(m_name)) {
            return getUrnDisplay(org, null, false);
        }
        else {
            String[] names = m_name.split("\\s+");
            return names.length > 1 ? names[0] : m_name;
        }
    }

    public void setFirstName(String firstName) {
        if (StringUtils.isEmpty(m_name)) {
            m_name = firstName;
        } else {
            String[] names = m_name.split("\\s+");
            names[0]= firstName;
            m_name = StringUtils.join(names, " ");
        }
    }

    public String getDisplay(Org org, boolean full) {
        if (StringUtils.isNotEmpty(m_name)) {
            return m_name;
        }
        else if (org.isAnon()) {
            return getAnonIdentifier();
        }
        else {
            return getUrnDisplay(org, null, full);
        }
    }

    /**
     * Gets the highest priority matching URN for this contact in one of the given schemes
     */
    public ContactUrn getUrn(List<ContactUrn.Scheme> schemes) {
        if (schemes != null) {
            for (ContactUrn urn : m_urns) {
                if (schemes.contains(urn.getScheme())) {
                    return urn;
                }
            }
            return null;
        } else {
            // otherwise return highest priority of any scheme
            return m_urns.size() > 0 ? m_urns.get(0) : null;
        }
    }

    public String getUrnDisplay(Org org, ContactUrn.Scheme scheme, boolean full) {
        if (org.isAnon()) {
            return getAnonIdentifier();
        }

        List<ContactUrn.Scheme> schemes = scheme != null ? Collections.singletonList(scheme) : null;
        ContactUrn urn = getUrn(schemes);
        return urn != null ? urn.getDisplay(org, full) : "";
    }

    protected String getAnonIdentifier() {
        // TODO where can we get the usual anon identifier from? Is UUID an ok substitute?
        return m_uuid;
    }

    /**
     * Builds the evaluation context for this contact
     * @param org the org
     * @return the context
     */
    public Map<String, String> buildContext(Org org) {
        Map<String, String> context = new HashMap<>();
        context.put("*", getDisplay(org, false));
        context.put("name", m_name);
        context.put("first_name", getFirstName(org));
        context.put("tel_e164", getUrnDisplay(org, ContactUrn.Scheme.TEL, true));
        context.put("groups", StringUtils.join(m_groups, ","));
        context.put("uuid", m_uuid);
        context.put("language", m_language);

        // add all URNs
        for (ContactUrn.Scheme scheme : ContactUrn.Scheme.values()) {
            context.put(scheme.name().toLowerCase(), getUrnDisplay(org, scheme, false));
        }

        // add all fields
        for (Map.Entry<String, String> field : m_fields.entrySet()) {
            context.put(field.getKey(), field.getValue());
        }

        return context;
    }
}
