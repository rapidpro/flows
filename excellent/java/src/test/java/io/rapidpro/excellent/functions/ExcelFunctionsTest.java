package io.rapidpro.excellent.functions;

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

    @Test
    public void constructor() {
        new ExcelFunctions();
    }

    /************************************************************************************
     * Text Functions
     ************************************************************************************/

    @Test
    public void test_char() {
        assertThat(_char(9), is("\t"));
        assertThat(_char(10), is("\n"));
        assertThat(_char(13), is("\r"));
        assertThat(_char(32), is(" "));
        assertThat(_char(65), is("A"));
    }

    @Test
    public void test_clean() {
        assertThat(clean("Hello \nwo\trl\rd"), is("Hello world"));
    }

    @Test
    public void test_code() {
        assertThat(code("\t"), is(9));
        assertThat(code("\n"), is(10));
    }

    @Test
    public void test_concatenate() {
        assertThat(concatenate("Hello", 4, "\n"), is("Hello4\n"));
        assertThat(concatenate("واحد", " ", "اثنان", " ", "ثلاثة"), is("واحد اثنان ثلاثة"));
    }

    @Test
    public void test_fixed() {
        assertThat(fixed("1234.5678", 1, false), is("1,234.6"));
        assertThat(fixed("1234.5678", 2, false), is("1,234.57"));
        assertThat(fixed("1234.5678", 3, false), is("1,234.568"));
        assertThat(fixed("1234.5678", 4, false), is("1,234.5678"));
        assertThat(fixed("1234.5678", 0, false), is("1,235"));
        assertThat(fixed("1234.5678", -1, false), is("1,230"));
        assertThat(fixed("1234.5678", -2, false), is("1,200"));
        assertThat(fixed("1234.5678", -3, false), is("1,000"));
        assertThat(fixed("1234.5678", -4, false), is("0"));

        assertThat(fixed("1234.1234", 2, false), is("1,234.12"));
        assertThat(fixed("1234.5678", 3, true), is("1234.568"));
    }

    @Test
    public void test_left() {
        assertThat(left("abcdef", 0), is(""));
        assertThat(left("abcdef", 2), is("ab"));
        assertThat(left("واحد", 2), is("وا"));
    }

    @Test(expected = RuntimeException.class)
    public void test_left_negativeChars() {
        left("abcd", -1);
    }

    @Test
    public void test_len() {
        assertThat(len(""), is(0));
        assertThat(len(" "), is(1));
        assertThat(len("qwérty"), is(6));
        assertThat(len("سلام"), is(4));
    }

    @Test
    public void test_lower() {
        assertThat(lower(""), is(""));
        assertThat(lower("aBcD"), is("abcd"));
        assertThat(lower("A واحد"), is("a واحد"));
    }

    @Test
    public void test_proper() {
        assertThat(proper(""), is(""));
        assertThat(proper("f1rst  sécOND-third_fourth"), is("F1Rst  Sécond-Third_Fourth"));
        assertThat(proper("واحد abc ثلاثة"), is("واحد Abc ثلاثة"));
    }

    @Test
    public void test_rept() {
        assertThat(rept("abc", 3), is("abcabcabc"));
        assertThat(rept("واحد", 3), is("واحدواحدواحد"));
    }

    @Test(expected = RuntimeException.class)
    public void test_rept_negativeTimes() {
        rept("abc", -1);
    }

    @Test
    public void test_right() {
        assertThat(right("abcdef", 0), is(""));
        assertThat(right("abcdef", 2), is("ef"));
        assertThat(right("واحد", 2), is("حد"));
    }

    @Test(expected = RuntimeException.class)
    public void test_right_negativeChars() {
        right("abcd", -1);
    }

    @Test
    public void test_substitute() {
        assertThat(substitute("hello Hello world", "hello", "bonjour", -1), is("bonjour Hello world"));  // case-sensitive
        assertThat(substitute("hello hello world", "hello", "bonjour", -1), is("bonjour bonjour world"));  // all instances
        assertThat(substitute("hello hello world", "hello", "bonjour", 2), is("hello bonjour world"));  // specific instance
        assertThat(substitute("واحد اثنين ثلاثة", "واحد", "اثنين", -1), is("اثنين اثنين ثلاثة"));
    }

    @Test
    public void test_unichar() {
        assertThat(unichar(65), is("A"));
        assertThat(unichar(1575), is("ا"));
    }

    @Test
    public void test_unicode() {
        assertThat(unicode("\t"), is(9));
        assertThat(unicode("\u04d2"), is(1234));
        assertThat(unicode("ا"), is(1575));
    }

    @Test(expected = RuntimeException.class)
    public void test_unicode_emptyString() {
        unicode("");
    }

    @Test
    public void test_upper() {
        assertThat(upper(""), is(""));
        assertThat(upper("aBcD"), is("ABCD"));
        assertThat(upper("a واحد"), is("A واحد"));
    }

    /************************************************************************************
     * Date and Time Functions
     ************************************************************************************/

    @Test
    public void test_date() {
        assertThat(date(1900, 1, 1), is(LocalDate.of(1900, 1, 1)));
        assertThat(date(2012, "3", new BigDecimal(2.0)), is(LocalDate.of(2012, 3, 2)));
    }

    /************************************************************************************
     * Math Functions
     ************************************************************************************/

    @Test
    public void test_abs() {
        assertThat(abs(1), is(new BigDecimal(1)));
        assertThat(abs(new BigDecimal(1)), is(new BigDecimal(1)));
        assertThat(abs(new BigDecimal(-1)), is(new BigDecimal(1)));
    }

    @Test
    public void test_max() {
        assertThat(max(1), is(new BigDecimal(1)));
        assertThat(max(1, 3, 2, -5), is(new BigDecimal(3)));
        assertThat(max(-2, -5), is(new BigDecimal(-2)));
    }

    @Test(expected = RuntimeException.class)
    public void test_max_noArgs() {
        max();
    }

    @Test
    public void test_min() {
        assertThat(min(1), is(new BigDecimal(1)));
        assertThat(min(-1, -3, -2, 5), is(new BigDecimal(-3)));
        assertThat(min(-2, -5), is(new BigDecimal(-5)));
    }

    @Test(expected = RuntimeException.class)
    public void test_min_noArgs() {
        min();
    }

    @Test
    public void test_power() {
        assertThat(power("4", 2), is(new BigDecimal(16)));
        assertThat(power(4, "0.5"), is(new BigDecimal(2)));
    }

    @Test
    public void test_rand() {
        assertThat(rand(), instanceOf(BigDecimal.class));
        assertThat(rand().compareTo(BigDecimal.ZERO), greaterThan(0));
        assertThat(rand().compareTo(BigDecimal.ONE), lessThan(0));
    }

    @Test
    public void test_randbetween() {
        assertThat(randbetween("2", 4), is(both(greaterThanOrEqualTo(2)).and(lessThanOrEqualTo(4))));
    }

    @Test
    public void test_sum() {
        assertThat(sum(1), is(new BigDecimal(1)));
        assertThat(sum(1, 2), is(new BigDecimal(3)));
        assertThat(sum(1, 2, "3"), is(new BigDecimal(6)));
    }

    @Test(expected = RuntimeException.class)
    public void test_sum_noArgs() {
        sum();
    }

    /************************************************************************************
     * Logical Functions
     ************************************************************************************/

    @Test
    public void test_and() {
        assertThat(and(false), is(false));
        assertThat(and(true), is(true));
        assertThat(and(1, true, "true"), is(true));
        assertThat(and(1, true, "true", 0), is(false));
    }

    @Test
    public void test_false() {
        assertThat(_false(), is(false));
    }

    @Test
    public void test_if() {
        assertThat(_if(true, 0, false), is(0));
        assertThat(_if(true, "x", "y"), is("x"));
        assertThat(_if("true", "x", "y"), is("x"));
        assertThat(_if(false, 0, false), is(false));
        assertThat(_if(false, "x", "y"), is("y"));
        assertThat(_if(0, "x", "y"), is("y"));
    }

    @Test
    public void test_or() {
        assertThat(or(false), is(false));
        assertThat(or(true), is(true));
        assertThat(or(1, false, "false"), is(true));
        assertThat(or(0, false, "FALSE"), is(false));
        assertThat(or(0, true, "false"), is(true));
    }

    @Test
    public void test_true() {
        assertThat(_true(), is(true));
    }
}
