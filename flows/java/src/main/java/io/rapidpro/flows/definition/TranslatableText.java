package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.rapidpro.flows.definition.tests.TranslatableTest;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Text that may be a single untranslated value or a translation map
 */
@JsonAdapter(TranslatableText.JsonAdapter.class)
public class TranslatableText {

    protected String m_untranslated;

    protected Map<String, String> m_translations;

    public TranslatableText(String untranslated) {
        m_untranslated = untranslated;
    }

    public TranslatableText(Map<String, String> translations) {
        m_translations = translations;
    }

    /**
     * Constructs a translatable text from language/translation pairs,
     * @param pairs alternating language name and translation, e.g. "eng", "Hello", "fra", "Bonjour"
     */
    public TranslatableText(String... pairs) {
        m_translations = new HashMap<>();
        for (int p = 0; p < pairs.length; p += 2) {
            this.m_translations.put(pairs[p], pairs[p + 1]);
        }
    }

    public static TranslatableText fromJson(JsonElement elem) throws FlowParseException {
        if (elem.isJsonObject()) {
            JsonObject translationSet = elem.getAsJsonObject();

            Map<String, String> translations = new HashMap<>();
            for (Map.Entry<String, JsonElement> prop : translationSet.entrySet()) {
                if (!prop.getValue().isJsonNull()) {
                    translations.put(prop.getKey(), prop.getValue().getAsString());
                }
            }
            return new TranslatableText(translations);
        }
        else if (elem.isJsonPrimitive()) {
            return new TranslatableText(elem.getAsString());
        }
        else {
            throw new FlowParseException("Translatable text be an object or string primitive");
        }
    }

    /**
     * Gets the localized text with the empty string as the default
     * @param run the ran state
     * @return the localized text
     */
    public String getLocalized(RunState run) {
        return getLocalized(run, "");
    }

    /**
     * Gets the localized text. We return according to the following precedence:
     *   1) Contact's language
     *   2) Org Primary Language
     *   3) Flow Base Language
     *   4) Default Text
     * @param run the ran state
     * @param defaultText the default to return if there's no suitable translation
     * @return the localized text
     */
    public String getLocalized(RunState run, String defaultText) {
        List<String> preferredLanguages = new ArrayList<>();

        if (StringUtils.isNotEmpty(run.getContact().getLanguage())) {
            preferredLanguages.add(run.getContact().getLanguage());
        }
        preferredLanguages.add(run.getOrg().getPrimaryLanguage());
        preferredLanguages.add(run.getFlow().getBaseLanguage());

        return getLocalized(preferredLanguages, defaultText);
    }

    public String getLocalized(List<String> preferredLangs, String defaultText) {
        if (StringUtils.isEmpty(m_untranslated) && (m_translations == null || m_translations.isEmpty())) {
            return defaultText;
        }

        if (m_untranslated != null) {
            return m_untranslated;
        }

        for (String lang : preferredLangs) {
            String localized = m_translations.get(lang);
            if (localized != null) {
                return localized;
            }
        }

        return defaultText;
    }

    /**
     * JSON serialization and de-serialization
     */
    public static class JsonAdapter extends TypeAdapter<TranslatableText> {
        @Override
        public void write(JsonWriter out, TranslatableText text) throws IOException {
            if (text.m_untranslated != null) {
                out.value(text.m_untranslated);
            } else {
                out.beginObject();
                for (Map.Entry<String, String> entry : text.m_translations.entrySet()) {
                    out.name(entry.getKey());
                    out.value(entry.getValue());
                }
                out.endObject();
            }
        }
        @Override
        public TranslatableText read(JsonReader in) throws IOException {
            if (in.peek().equals(JsonToken.STRING)) {
                return new TranslatableText(in.nextString());
            } else {
                return null; // TODO ?
            }
        }
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        if (m_untranslated != null) {
            return m_untranslated;
        } else {
            return m_translations.toString();
        }
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TranslatableText that = (TranslatableText) o;

        if (m_untranslated != null ? !m_untranslated.equals(that.m_untranslated) : that.m_untranslated != null)
            return false;
        return !(m_translations != null ? !m_translations.equals(that.m_translations) : that.m_translations != null);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = m_untranslated != null ? m_untranslated.hashCode() : 0;
        result = 31 * result + (m_translations != null ? m_translations.hashCode() : 0);
        return result;
    }
}
