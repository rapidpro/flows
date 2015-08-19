package io.rapidpro.flows.definition;

import com.google.gson.JsonParser;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link TranslatableText}
 */
public class TranslatableTextTest {

    @Test
    public void fromJson() {
        JsonParser parser = new JsonParser();

        TranslatableText text = TranslatableText.fromJson(parser.parse("\"test\""));
        assertThat(text.m_untranslated, is("test"));
        assertThat(text.m_translations, nullValue());

        text = TranslatableText.fromJson(parser.parse("{\"eng\": \"Hello\", \"fra\": \"Bonjour\"}"));
        assertThat(text.m_untranslated, nullValue());
        assertThat(text.m_translations, hasEntry("eng", "Hello"));
        assertThat(text.m_translations, hasEntry("fra", "Bonjour"));
    }

    @Test
    public void getLocalized() {
        TranslatableText text = new TranslatableText("Hello");
        assertThat(text.getLocalized(Collections.<String>emptyList(), "default"), is("Hello"));
        assertThat(text.getLocalized(Arrays.asList("eng", "fra"), "default"), is("Hello"));

        text = new TranslatableText("");
        assertThat(text.getLocalized(Collections.<String>emptyList(), "default"), is("default"));
        assertThat(text.getLocalized(Arrays.asList("eng", "fra"), "default"), is("default"));

        text = new TranslatableText(new HashMap<String, String>());
        assertThat(text.getLocalized(Collections.<String>emptyList(), "default"), is("default"));
        assertThat(text.getLocalized(Arrays.asList("eng", "fra"), "default"), is("default"));

        Map<String, String> translations = new HashMap<>();
        translations.put("eng", "Hello");
        translations.put("fra", "Bonjour");
        text = new TranslatableText(translations);
        assertThat(text.getLocalized(Collections.<String>emptyList(), "default"), is("default"));
        assertThat(text.getLocalized(Arrays.asList("kin", "run"), "default"), is("default"));
        assertThat(text.getLocalized(Arrays.asList("eng", "fra"), "default"), is("Hello"));
        assertThat(text.getLocalized(Arrays.asList("fra", "eng"), "default"), is("Bonjour"));
    }

    @Test
    public void _equals() {
        assertThat(new TranslatableText("abc").equals(new TranslatableText("abc")), is(true));
        assertThat(new TranslatableText("abc").equals(new TranslatableText("cde")), is(false));

        // TODO test with translations
    }
}
