package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Text that may be a single untranslated value or a translation map
 */
public class TranslatableText {

    protected String m_untranslated;
    protected Map<String, String> m_translations = new HashMap<>();

    public static TranslatableText fromJson(JsonElement elem) {
        TranslatableText text = new TranslatableText();

        if (elem.isJsonObject()) {
            JsonObject translationSet = elem.getAsJsonObject();
            for (Map.Entry<String, JsonElement> prop : translationSet.entrySet()) {
                if (!prop.getValue().isJsonNull()) {
                    text.m_translations.put(prop.getKey(), prop.getValue().getAsString());
                }
            }
        }
        else if (elem.isJsonPrimitive()) {
            text.m_untranslated = elem.getAsString();
        }

        return text;
    }

    public String getLocalized(List<String> preferredLangs, String defaultText) {
        if (m_untranslated == null && m_translations.isEmpty()) {
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
}
