package io.rapidpro.flows.definition;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Flow}
 */
public class FlowTest {

    @Test
    public void fromJson() throws IOException {
        String json = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("mushrooms.json"));

        Flow definition = Flow.fromJson(json);

        assertThat(definition.getBaseLanguage(), is("eng"));
        assertThat(definition.getEntry(), notNullValue());
    }
}
