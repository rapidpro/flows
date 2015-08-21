package io.rapidpro.flows.runner;

import com.google.gson.JsonObject;
import io.rapidpro.flows.FlowUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * A contact that can participate in a flow
 */
public class Contact {

    protected String m_uuid;

    protected String m_name;

    protected List<ContactUrn> m_urns;

    protected Set<String> m_groups;

    protected Map<String, String> m_fields;

    protected String m_language;

    public Contact() {
    }

    public Contact(String uuid, String name, List<ContactUrn> urns, Set<String> groups, Map<String, String> fields, String language) {
        m_uuid = uuid;
        m_name = name;
        m_urns = urns;
        m_groups = groups;
        m_fields = fields;
        m_language = language;
    }

    public static Contact fromJson(JsonObject json) {
        Contact obj = new Contact();
        obj.m_uuid = FlowUtils.getAsString(json, "uuid");
        obj.m_name = FlowUtils.getAsString(json, "name");
        obj.m_language = FlowUtils.getAsString(json, "language");
        return obj;
    }

    public String getUuid() {
        return m_uuid;
    }

    public String getName() {
        return m_name;
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

    public String getLanguage() {
        return m_language;
    }

    public String getFirstName(Org org) {
        if (StringUtils.isEmpty(m_name)) {
            return getUrnDisplay(org, null, false);
        }
        else {
            String[] names = m_name.split("\\s");
            return names.length > 1 ? names[0] : m_name;
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
     * Gets the highest priority matching URN for this contact in any scheme
     */
    public ContactUrn getUrn() {
        return getUrn(null);
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

    protected String getAnonIdentifier() {
        // TODO where can we get the usual anon identifier from? Is UUID an ok substitute?
        return m_uuid;
    }

    public String getUrnDisplay(Org org, ContactUrn.Scheme scheme, boolean full) {
        if (org.isAnon()) {
            return getAnonIdentifier();
        }

        ContactUrn urn = getUrn(Collections.singletonList(scheme));
        return urn != null ? urn.getDisplay(org, full) : "";
    }

    public Map<String, Object> buildContext(Org org) {
        Map<String, Object> context = new HashMap<>();
        context.put("*", getDisplay(org, false));
        context.put("name", StringUtils.isNotEmpty(m_name) ? m_name : "");
        context.put("first_name", getFirstName(org));
        context.put("tel_e164", getUrnDisplay(org, ContactUrn.Scheme.TEL, true));
        context.put("groups", StringUtils.join(m_groups, ","));
        context.put("uuid", m_uuid);
        context.put("language", m_language);  // TODO what happens when these are null?

        // add all URNs
        for (ContactUrn.Scheme scheme : ContactUrn.Scheme.values()) {
            context.put(scheme.name().toLowerCase(), getUrnDisplay(org, scheme, false));
        }

        // TODO add contact fields

        // get all the values for this contact
        //contact_values = {v.contact_field.key: v for v in Value.objects.filter(contact=self).exclude(contact_field=None).select_related('contact_field')}

        // add all fields
        //for field in ContactField.objects.filter(org_id=self.org_id).select_related('org'):
        //field_value = Contact.get_field_display_for_value(field, contact_values.get(field.key, None))
        //contact_dict[field.key] = field_value if not field_value is None else ''

        return context;
    }
}
