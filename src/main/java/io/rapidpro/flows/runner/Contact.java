package io.rapidpro.flows.runner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;
import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.ZonedDateTime;

import java.util.*;

/**
 * A contact that can participate in a flow
 */
public class Contact implements Jsonizable {

    protected String m_uuid;

    protected String m_name;

    protected List<ContactUrn> m_urns;

    protected Set<String> m_groups;

    protected Map<String, String> m_fields;

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

    public static Contact fromJson(JsonElement elm) {
        JsonObject obj = elm.getAsJsonObject();
        return new Contact(
                JsonUtils.getAsString(obj, "uuid"),
                JsonUtils.getAsString(obj, "name"),
                JsonUtils.fromJsonArray(obj.get("urns").getAsJsonArray(), null, ContactUrn.class),
                new LinkedHashSet<>(JsonUtils.fromJsonArray(obj.get("groups").getAsJsonArray(), null, String.class)),
                JsonUtils.fromJsonObject(obj.get("fields").getAsJsonObject(), null, String.class),
                JsonUtils.getAsString(obj, "language")
        );
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object(
                "uuid", m_uuid,
                "name", m_name,
                "urns", JsonUtils.toJsonArray(m_urns),
                "groups", JsonUtils.toJsonArray(m_groups),
                "fields", JsonUtils.toJsonObject(m_fields),
                "language", m_language
        );
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
     * @param run the current run state
     * @param container the containing evaluation context
     * @return the context
     */
    public Map<String, String> buildContext(RunState run, EvaluationContext container) {
        Map<String, String> context = new HashMap<>();
        context.put("*", getDisplay(run.getOrg(), false));
        context.put("name", m_name);
        context.put("first_name", getFirstName(run.getOrg()));
        context.put("tel_e164", getUrnDisplay(run.getOrg(), ContactUrn.Scheme.TEL, true));
        context.put("groups", StringUtils.join(m_groups, ","));
        context.put("uuid", m_uuid);
        context.put("language", m_language);

        // add all URNs
        for (ContactUrn.Scheme scheme : ContactUrn.Scheme.values()) {
            context.put(scheme.name().toLowerCase(), getUrnDisplay(run.getOrg(), scheme, false));
        }

        // add all fields
        for (Map.Entry<String, String> entry : m_fields.entrySet()) {
            String rawValue = entry.getValue();
            Field field = run.getOrCreateField(entry.getKey());
            String value;

            if (field != null && field.getValueType().equals(Field.ValueType.DATETIME)) {
                ZonedDateTime asDatetime = Conversions.toDateTime(rawValue, container);
                value = Conversions.toString(asDatetime, container);
            } else {
                value = entry.getValue();
            }

            context.put(entry.getKey(), value);
        }

        return context;
    }
}
