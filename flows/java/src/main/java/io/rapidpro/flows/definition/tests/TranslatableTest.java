package io.rapidpro.flows.definition.tests;

import io.rapidpro.flows.definition.TranslatableText;

/**
 * Abstract base class for tests that have a translatable text argument
 */
public abstract class TranslatableTest extends Test {

    protected TranslatableText m_test;

    protected TranslatableTest(TranslatableText test) {
        m_test = test;
    }
}
