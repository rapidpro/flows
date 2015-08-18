package io.rapidpro.expressions.functions;

import io.rapidpro.expressions.EvaluationContext;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static io.rapidpro.expressions.functions.CustomFunctions.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link CustomFunctions}
 */
public class CustomFunctionsTest {

    private EvaluationContext m_context;

    @Before
    public void setup() {
        m_context = new EvaluationContext();
    }

    @Test
    public void constructor() {
        new CustomFunctions();
    }

    @Test
    public void test_first_word() {
        assertThat(first_word(m_context, "  "), is(""));
        assertThat(first_word(m_context, " abc "), is("abc"));
        assertThat(first_word(m_context, " abc def ghi"), is("abc"));
        assertThat(first_word(m_context, " واحد "), is("واحد"));
        assertThat(first_word(m_context, " واحد اثنين ثلاثة "), is("واحد"));
    }

    @Test
    public void test_percent() {
        assertThat(percent(m_context, "0.25321"), is("25%"));
        assertThat(percent(m_context, new BigDecimal("0.33")), is("33%"));
        assertThat(percent(m_context, new BigDecimal("0.6666666")), is("67%"));
    }

    @Test
    public void test_read_digits() {
        assertThat(read_digits(m_context, "1234567890123456"), is("1 2 3 4 , 5 6 7 8 , 9 0 1 2 , 3 4 5 6")); // credit card
        assertThat(read_digits(m_context, "+123456789012"), is("1 2 3 , 4 5 6 , 7 8 9 , 0 1 2")); // phone number
        assertThat(read_digits(m_context, "123456"), is("1 2 3 , 4 5 6")); // triplets
        assertThat(read_digits(m_context, "123456789"), is("1 2 3 , 4 5 , 6 7 8 9")); // SSN
        assertThat(read_digits(m_context, "12345"), is("1,2,3,4,5")); // regular number, street address, etc
        assertThat(read_digits(m_context, "123"), is("1,2,3")); // regular number, street address, etc
        assertThat(read_digits(m_context, ""), is("")); // empty
    }

    @Test
    public void test_remove_first_word() {
        assertThat(remove_first_word(m_context, ""), is(""));
        assertThat(remove_first_word(m_context, "abc"), is(""));
        assertThat(remove_first_word(m_context, " abc "), is(""));
        assertThat(remove_first_word(m_context, " abc def-ghi "), is("def-ghi ")); // should preserve remainder of text
        assertThat(remove_first_word(m_context, " واحد "), is(""));
        assertThat(remove_first_word(m_context, "واحد اثنين ثلاثة"), is("اثنين ثلاثة"));
    }

    @Test
    public void test_word() {
        assertThat(word(m_context, " abc def ghi", 1, false), is("abc"));
        assertThat(word(m_context, "abc-def  ghi  jkl", 3, false), is("ghi"));
        assertThat(word(m_context, "abc-def  ghi  jkl", 3, true), is("jkl"));
        assertThat(word(m_context, "abc-def  ghi  jkl", "3", "TRUE"), is("jkl")); // string args only
        assertThat(word(m_context, "abc-def  ghi  jkl", -1, false), is("jkl")); // negative index
        assertThat(word(m_context, " abc def   ghi", 6, false), is("")); // out of range
        assertThat(word(m_context, "", 1, false), is(""));
        assertThat(word(m_context, "واحد اثنين ثلاثة", 1, false), is("واحد"));
        assertThat(word(m_context, "واحد اثنين ثلاثة", -1, false), is("ثلاثة"));
    }

    @Test(expected = RuntimeException.class)
    public void test_word_zeroNumber() {
        word(m_context, "abc", 0, false); // number cannot be zero
    }

    @Test
    public void test_word_count() {
        assertThat(word_count(m_context, "", false), is(0));
        assertThat(word_count(m_context, " abc-def  ghi  jkl", false), is(4));
        assertThat(word_count(m_context, " abc-def  ghi  jkl", true), is(3));
        assertThat(word_count(m_context, "واحد اثنين-ثلاثة", false), is(3));
        assertThat(word_count(m_context, "واحد اثنين-ثلاثة", true), is(2));
    }

    @Test
    public void test_word_slice() {
        assertThat(word_slice(m_context, " abc  def ghi-jkl ", 1, 3, false), is("abc def"));
        assertThat(word_slice(m_context, " abc  def ghi-jkl ", 3, 0, false), is("ghi jkl"));
        assertThat(word_slice(m_context, " abc  def ghi-jkl ", 3, 0, true), is("ghi-jkl"));
        assertThat(word_slice(m_context, " abc  def ghi-jkl ", "3", "0", "false"), is("ghi jkl")); // string args only
        assertThat(word_slice(m_context, " abc  def ghi-jkl ", 2, -1, false), is("def ghi"));
        assertThat(word_slice(m_context, " abc  def ghi-jkl ", -1, 0, false), is("jkl"));
        assertThat(word_slice(m_context, " abc  def ghi-jkl ", 2, -1, true), is("def"));
        assertThat(word_slice(m_context, "واحد اثنين ثلاثة", 1, 3, false), is("واحد اثنين"));
    }

    @Test(expected = RuntimeException.class)
    public void test_word_slice_zeroStart() {
        word_slice(m_context, " abc  def ghi-jkl ", 0, 0, false);  // start can't be zero
    }
}
