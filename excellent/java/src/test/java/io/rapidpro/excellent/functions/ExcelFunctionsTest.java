package io.rapidpro.excellent.functions;

import io.rapidpro.excellent.EvaluationContext;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static io.rapidpro.excellent.functions.ExcelFunctions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link ExcelFunctions}
 */
public class ExcelFunctionsTest {

    private EvaluationContext m_context;

    @Before
    public void setup() {
        m_context = new EvaluationContext();
    }

    @Test
    public void constructor() {
        new ExcelFunctions();
    }

    /************************************************************************************
     * Text Functions
     ************************************************************************************/

    @Test
    public void test_char() {
        assertThat(_char(m_context, 9), is("\t"));
        assertThat(_char(m_context, 10), is("\n"));
        assertThat(_char(m_context, 13), is("\r"));
        assertThat(_char(m_context, 32), is(" "));
        assertThat(_char(m_context, 65), is("A"));
    }

    @Test
    public void test_clean() {
        assertThat(clean(m_context, "Hello \nwo\trl\rd"), is("Hello world"));
    }

    @Test
    public void test_code() {
        assertThat(code(m_context, "\t"), is(9));
        assertThat(code(m_context, "\n"), is(10));
    }

    @Test
    public void test_concatenate() {
        assertThat(concatenate(m_context, "Hello", 4, "\n"), is("Hello4\n"));
        assertThat(concatenate(m_context, "واحد", " ", "اثنان", " ", "ثلاثة"), is("واحد اثنان ثلاثة"));
    }

    @Test
    public void test_fixed() {
        assertThat(fixed(m_context, "1234.5678", 1, false), is("1,234.6"));
        assertThat(fixed(m_context, "1234.5678", 2, false), is("1,234.57"));
        assertThat(fixed(m_context, "1234.5678", 3, false), is("1,234.568"));
        assertThat(fixed(m_context, "1234.5678", 4, false), is("1,234.5678"));
        assertThat(fixed(m_context, "1234.5678", 0, false), is("1,235"));
        assertThat(fixed(m_context, "1234.5678", -1, false), is("1,230"));
        assertThat(fixed(m_context, "1234.5678", -2, false), is("1,200"));
        assertThat(fixed(m_context, "1234.5678", -3, false), is("1,000"));
        assertThat(fixed(m_context, "1234.5678", -4, false), is("0"));

        assertThat(fixed(m_context, "1234.1234", 2, false), is("1,234.12"));
        assertThat(fixed(m_context, "1234.5678", 3, true), is("1234.568"));
    }

    @Test
    public void test_left() {
        assertThat(left(m_context, "abcdef", 0), is(""));
        assertThat(left(m_context, "abcdef", 2), is("ab"));
        assertThat(left(m_context, "واحد", 2), is("وا"));
    }

    @Test(expected = RuntimeException.class)
    public void test_left_negativeChars() {
        left(m_context, "abcd", -1);
    }

    @Test
    public void test_len() {
        assertThat(len(m_context, ""), is(0));
        assertThat(len(m_context, " "), is(1));
        assertThat(len(m_context, "qwérty"), is(6));
        assertThat(len(m_context, "سلام"), is(4));
    }

    @Test
    public void test_lower() {
        assertThat(lower(m_context, ""), is(""));
        assertThat(lower(m_context, "aBcD"), is("abcd"));
        assertThat(lower(m_context, "A واحد"), is("a واحد"));
    }

    @Test
    public void test_proper() {
        assertThat(proper(m_context, ""), is(""));
        assertThat(proper(m_context, "f1rst  sécOND-third_fourth"), is("F1Rst  Sécond-Third_Fourth"));
        assertThat(proper(m_context, "واحد abc ثلاثة"), is("واحد Abc ثلاثة"));
    }

    @Test
    public void test_rept() {
        assertThat(rept(m_context, "abc", 3), is("abcabcabc"));
        assertThat(rept(m_context, "واحد", 3), is("واحدواحدواحد"));
    }

    @Test(expected = RuntimeException.class)
    public void test_rept_negativeTimes() {
        rept(m_context, "abc", -1);
    }

    @Test
    public void test_right() {
        assertThat(right(m_context, "abcdef", 0), is(""));
        assertThat(right(m_context, "abcdef", 2), is("ef"));
        assertThat(right(m_context, "واحد", 2), is("حد"));
    }

    @Test(expected = RuntimeException.class)
    public void test_right_negativeChars() {
        right(m_context, "abcd", -1);
    }

    @Test
    public void test_substitute() {
        assertThat(substitute(m_context, "hello Hello world", "hello", "bonjour", -1), is("bonjour Hello world"));  // case-sensitive
        assertThat(substitute(m_context, "hello hello world", "hello", "bonjour", -1), is("bonjour bonjour world"));  // all instances
        assertThat(substitute(m_context, "hello hello world", "hello", "bonjour", 2), is("hello bonjour world"));  // specific instance
        assertThat(substitute(m_context, "واحد اثنين ثلاثة", "واحد", "اثنين", -1), is("اثنين اثنين ثلاثة"));
    }

    @Test
    public void test_unichar() {
        assertThat(unichar(m_context, 65), is("A"));
        assertThat(unichar(m_context, 1575), is("ا"));
    }

    @Test
    public void test_unicode() {
        assertThat(unicode(m_context, "\t"), is(9));
        assertThat(unicode(m_context, "\u04d2"), is(1234));
        assertThat(unicode(m_context, "ا"), is(1575));
    }

    @Test(expected = RuntimeException.class)
    public void test_unicode_emptyString() {
        unicode(m_context, "");
    }

    @Test
    public void test_upper() {
        assertThat(upper(m_context, ""), is(""));
        assertThat(upper(m_context, "aBcD"), is("ABCD"));
        assertThat(upper(m_context, "a واحد"), is("A واحد"));
    }

    /************************************************************************************
     * Date and Time Functions
     ************************************************************************************/

    @Test
    public void test_date() {
        assertThat(date(m_context, 1900, 1, 1), is(LocalDate.of(1900, 1, 1)));
        assertThat(date(m_context, 2012, "3", new BigDecimal(2.0)), is(LocalDate.of(2012, 3, 2)));
    }

    /************************************************************************************
     * Math Functions
     ************************************************************************************/

    @Test
    public void test_abs() {
        assertThat(abs(m_context, 1), is(new BigDecimal(1)));
        assertThat(abs(m_context, new BigDecimal(1)), is(new BigDecimal(1)));
        assertThat(abs(m_context, new BigDecimal(-1)), is(new BigDecimal(1)));
    }

    @Test
    public void test_max() {
        assertThat(max(m_context, 1), is(new BigDecimal(1)));
        assertThat(max(m_context, 1, 3, 2, -5), is(new BigDecimal(3)));
        assertThat(max(m_context, -2, -5), is(new BigDecimal(-2)));
    }

    @Test(expected = RuntimeException.class)
    public void test_max_noArgs() {
        max(m_context);
    }

    @Test
    public void test_min() {
        assertThat(min(m_context, 1), is(new BigDecimal(1)));
        assertThat(min(m_context, -1, -3, -2, 5), is(new BigDecimal(-3)));
        assertThat(min(m_context, -2, -5), is(new BigDecimal(-5)));
    }

    @Test(expected = RuntimeException.class)
    public void test_min_noArgs() {
        min(m_context);
    }

    @Test
    public void test_power() {
        assertThat(power(m_context, "4", 2), is(new BigDecimal(16)));
        assertThat(power(m_context, 4, "0.5"), is(new BigDecimal(2)));
    }

    @Test
    public void test_rand() {
        assertThat(rand(), instanceOf(BigDecimal.class));
        assertThat(rand().compareTo(BigDecimal.ZERO), greaterThan(0));
        assertThat(rand().compareTo(BigDecimal.ONE), lessThan(0));
    }

    @Test
    public void test_randbetween() {
        assertThat(randbetween(m_context, "2", 4), is(both(greaterThanOrEqualTo(2)).and(lessThanOrEqualTo(4))));
    }

    @Test
    public void test_sum() {
        assertThat(sum(m_context, 1), is(new BigDecimal(1)));
        assertThat(sum(m_context, 1, 2), is(new BigDecimal(3)));
        assertThat(sum(m_context, 1, 2, "3"), is(new BigDecimal(6)));
    }

    @Test(expected = RuntimeException.class)
    public void test_sum_noArgs() {
        sum(m_context);
    }

    /************************************************************************************
     * Logical Functions
     ************************************************************************************/

    @Test
    public void test_and() {
        assertThat(and(m_context, false), is(false));
        assertThat(and(m_context, true), is(true));
        assertThat(and(m_context, 1, true, "true"), is(true));
        assertThat(and(m_context, 1, true, "true", 0), is(false));
    }

    @Test
    public void test_false() {
        assertThat(_false(), is(false));
    }

    @Test
    public void test_if() {
        assertThat(_if(m_context, true, 0, false), is(0));
        assertThat(_if(m_context, true, "x", "y"), is("x"));
        assertThat(_if(m_context, "true", "x", "y"), is("x"));
        assertThat(_if(m_context, false, 0, false), is(false));
        assertThat(_if(m_context, false, "x", "y"), is("y"));
        assertThat(_if(m_context, 0, "x", "y"), is("y"));
    }

    @Test
    public void test_or() {
        assertThat(or(m_context, false), is(false));
        assertThat(or(m_context, true), is(true));
        assertThat(or(m_context, 1, false, "false"), is(true));
        assertThat(or(m_context, 0, false, "FALSE"), is(false));
        assertThat(or(m_context, 0, true, "false"), is(true));
    }

    @Test
    public void test_true() {
        assertThat(_true(), is(true));
    }
}
