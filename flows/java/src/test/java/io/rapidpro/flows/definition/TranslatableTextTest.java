package io.rapidpro.flows.definition;

import com.google.gson.JsonParser;
import org.junit.Test;

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
        assertThat(text.m_translations.size(), is(0));

        text = TranslatableText.fromJson(parser.parse("{\"eng\": \"Hello\", \"fra\": \"Bonjour\"}"));
        assertThat(text.m_untranslated, nullValue());
        assertThat(text.m_translations, hasEntry("eng", "Hello"));
        assertThat(text.m_translations, hasEntry("fra", "Bonjour"));
    }
}
