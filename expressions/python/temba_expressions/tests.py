# coding=utf-8
from __future__ import absolute_import, unicode_literals

import codecs
import json
import pytz
import regex
import unittest

from datetime import datetime, date, time
from decimal import Decimal
from time import clock
from . import conversions, EvaluationError
from .dates import DateParser, DateStyle
from .evaluator import Evaluator, EvaluationContext, EvaluationStrategy
from .functions import excel, custom
from .utils import urlquote, decimal_pow


class DateParserTests(unittest.TestCase):

    def test_auto(self):
        tz = pytz.timezone('Africa/Kigali')
        parser = DateParser(date(2015, 8, 12), tz, DateStyle.DAY_FIRST)

        tests = (
            ("1/2/34", date(2034, 2, 1)),
            ("1-2-34", date(2034, 2, 1)),
            ("01 02 34", date(2034, 2, 1)),
            ("1 Feb 34", date(2034, 2, 1)),
            ("1. 2 '34", date(2034, 2, 1)),
            ("1st february 2034", date(2034, 2, 1)),
            ("1er février 2034", date(2034, 2, 1)),
            ("2/25-70", date(1970, 2, 25)),  # date style should be ignored when it doesn't make sense
            ("1 feb", date(2015, 2, 1)),  # year can be omitted
            ("Feb 1st", date(2015, 2, 1)),
            ("1 feb 9999999", date(2015, 2, 1)),  # ignore invalid values
            ("1/2/34 14:55", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("1-2-34 2:55PM", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("01 02 34 1455", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("1 Feb 34 02:55 PM", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("1. 2 '34 02:55pm", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("1st february 2034 14.55", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("1er février 2034 1455h", datetime(2034, 2, 1, 14, 55, 0, 0, tz))
        )
        for test in tests:
            self.assertEqual(parser.auto(test[0]), test[1], "Parser error for %s" % test[0])

    def test_time(self):
        tz = pytz.timezone('Africa/Kigali')
        parser = DateParser(date(2015, 8, 12), tz, DateStyle.DAY_FIRST)

        tests = (
            ("2:55", time(2, 55, 0)),
            ("2:55 AM", time(2, 55, 0)),
            ("14:55", time(14, 55, 0)),
            ("2:55PM", time(14, 55, 0)),
            ("1455", time(14, 55, 0)),
            ("02:55 PM", time(14, 55, 0)),
            ("02:55pm", time(14, 55, 0)),
            ("14.55", time(14, 55, 0)),
            ("1455h", time(14, 55, 0)),
            ("14:55:30", time(14, 55, 30)),
            ("14:55.30PM", time(14, 55, 30))
        )
        for test in tests:
            self.assertEqual(parser.time(test[0]), test[1], "Parser error for %s" % test[0])

    def test_year_from_2digits(self):
        self.assertEqual(DateParser._year_from_2digits(1, 2015), 2001)
        self.assertEqual(DateParser._year_from_2digits(64, 2015), 2064)
        self.assertEqual(DateParser._year_from_2digits(65, 2015), 1965)
        self.assertEqual(DateParser._year_from_2digits(99, 2015), 1999)

        self.assertEqual(DateParser._year_from_2digits(1, 1990), 2001)
        self.assertEqual(DateParser._year_from_2digits(40, 1990), 2040)
        self.assertEqual(DateParser._year_from_2digits(41, 1990), 1941)
        self.assertEqual(DateParser._year_from_2digits(99, 1990), 1999)


class ConversionsTests(unittest.TestCase):

    def setUp(self):
        self.context = EvaluationContext({}, timezone=pytz.timezone("Africa/Kigali"))

    def test_to_boolean(self):
        self.assertEqual(conversions.to_boolean(True, self.context), True)
        self.assertEqual(conversions.to_boolean(False, self.context), False)

        self.assertEqual(conversions.to_boolean(1, self.context), True)
        self.assertEqual(conversions.to_boolean(0, self.context), False)
        self.assertEqual(conversions.to_boolean(-1, self.context), True)

        self.assertEqual(conversions.to_boolean(Decimal(0.5), self.context), True)
        self.assertEqual(conversions.to_boolean(Decimal(0.0), self.context), False)
        self.assertEqual(conversions.to_boolean(Decimal(-0.5), self.context), True)

        self.assertEqual(conversions.to_boolean("trUE", self.context), True)
        self.assertEqual(conversions.to_boolean("faLSE", self.context), False)
        self.assertEqual(conversions.to_boolean("faLSE", self.context), False)

        self.assertEqual(conversions.to_boolean(date(2012, 3, 4), self.context), True)
        self.assertEqual(conversions.to_boolean(time(12, 34, 0), self.context), True)
        self.assertEqual(conversions.to_boolean(datetime(2012, 3, 4, 5, 6, 7, 8, pytz.UTC), self.context), True)

        self.assertRaises(EvaluationError, conversions.to_boolean, 'x', self.context)

    def test_to_integer(self):
        self.assertEqual(conversions.to_integer(True, self.context), 1)
        self.assertEqual(conversions.to_integer(False, self.context), 0)

        self.assertEqual(conversions.to_integer(1234567890, self.context), 1234567890)

        self.assertEqual(conversions.to_integer(Decimal("1234"), self.context), 1234)
        self.assertEqual(conversions.to_integer(Decimal("1234.5678"), self.context), 1235)
        self.assertEqual(conversions.to_integer(Decimal("0.001"), self.context), 0)

        self.assertEqual(conversions.to_integer("1234", self.context), 1234)

        self.assertRaises(EvaluationError, conversions.to_integer, 'x', self.context)
        self.assertRaises(EvaluationError, conversions.to_integer, Decimal("12345678901234567890"), self.context)

    def test_to_decimal(self):
        self.assertEqual(conversions.to_decimal(True, self.context), Decimal(1))
        self.assertEqual(conversions.to_decimal(False, self.context), Decimal(0))

        self.assertEqual(conversions.to_decimal(123, self.context), Decimal(123))
        self.assertEqual(conversions.to_decimal(-123, self.context), Decimal(-123))

        self.assertEqual(conversions.to_decimal(Decimal("1234.5678"), self.context), Decimal("1234.5678"))

        self.assertEqual(conversions.to_decimal("1234.5678", self.context), Decimal("1234.5678"))

        self.assertRaises(EvaluationError, conversions.to_decimal, 'x', self.context)

    def test_to_string(self):
        self.assertEqual(conversions.to_string(True, self.context), "TRUE")
        self.assertEqual(conversions.to_string(False, self.context), "FALSE")

        self.assertEqual(conversions.to_string(-1, self.context), "-1")
        self.assertEqual(conversions.to_string(1234567890, self.context), "1234567890")

        self.assertEqual(conversions.to_string(Decimal("0.4440000"), self.context), "0.444")
        self.assertEqual(conversions.to_string(Decimal("1234567890.5"), self.context), "1234567891")
        self.assertEqual(conversions.to_string(Decimal("33.333333333333"), self.context), "33.33333333")
        self.assertEqual(conversions.to_string(Decimal("66.666666666666"), self.context), "66.66666667")

        self.assertEqual(conversions.to_string("hello", self.context), "hello")

        self.assertEqual(conversions.to_string(date(2012, 3, 4), self.context), "04-03-2012")
        self.assertEqual(conversions.to_string(time(12, 34, 0), self.context), "12:34")
        self.assertEqual(conversions.to_string(datetime(2012, 3, 4, 5, 6, 7, 8, pytz.timezone("Africa/Kigali")), self.context), "04-03-2012 05:06")

        self.context.date_style = DateStyle.MONTH_FIRST

        self.assertEqual(conversions.to_string(date(2012, 3, 4), self.context), "03-04-2012")
        self.assertEqual(conversions.to_string(datetime(2012, 3, 4, 5, 6, 7, 8, pytz.timezone("Africa/Kigali")), self.context), "03-04-2012 05:06")

    def test_to_date(self):
        self.assertEqual(conversions.to_date("14th Aug 2015", self.context), date(2015, 8, 14))
        self.assertEqual(conversions.to_date("14/8/15", self.context), date(2015, 8, 14))

        self.assertEqual(conversions.to_date(date(2015, 8, 14), self.context), date(2015, 8, 14))

        self.assertEqual(conversions.to_date(datetime(2015, 8, 14, 9, 12, 0, 0, pytz.timezone("Africa/Kigali")), self.context), date(2015, 8, 14))

        self.context.date_style = DateStyle.MONTH_FIRST

        self.assertEqual(conversions.to_date("12/8/15", self.context), date(2015, 12, 8))
        self.assertEqual(conversions.to_date("14/8/15", self.context), date(2015, 8, 14))  # ignored because doesn't make sense

    def test_to_datetime(self):
        self.assertEqual(conversions.to_datetime("14th Aug 2015 09:12", self.context), datetime(2015, 8, 14, 9, 12, 0, 0, pytz.timezone("Africa/Kigali")))

        self.assertEqual(conversions.to_datetime(date(2015, 8, 14), self.context), datetime(2015, 8, 14, 0, 0, 0, 0, pytz.timezone("Africa/Kigali")))

        self.assertEqual(conversions.to_datetime(datetime(2015, 8, 14, 9, 12, 0, 0, pytz.timezone("Africa/Kigali")), self.context), datetime(2015, 8, 14, 9, 12, 0, 0, pytz.timezone("Africa/Kigali")))

    def test_to_time(self):
        self.assertEqual(conversions.to_time("9:12", self.context), time(9, 12, 0))
        self.assertEqual(conversions.to_time("0912", self.context), time(9, 12, 0))
        self.assertEqual(conversions.to_time("09.12am", self.context), time(9, 12, 0))

        self.assertEqual(conversions.to_time(time(9, 12, 0), self.context), time(9, 12, 0))

        self.assertEqual(conversions.to_time(datetime(2015, 8, 14, 9, 12, 0, 0, pytz.timezone("Africa/Kigali")), self.context), time(9, 12, 0))

    def test_to_repr(self):
        self.assertEqual(conversions.to_repr(False, self.context), 'FALSE')
        self.assertEqual(conversions.to_repr(True, self.context), 'TRUE')

        self.assertEqual(conversions.to_repr(Decimal("123.45"), self.context), '123.45')

        self.assertEqual(conversions.to_repr('x"y', self.context), '"x""y"')

        self.assertEqual(conversions.to_repr(time(9, 12, 0), self.context), '"09:12"')

        self.assertEqual(conversions.to_repr(datetime(2015, 8, 14, 9, 12, 0, 0, pytz.timezone("Africa/Kigali")), self.context), '"14-08-2015 09:12"')


class EvaluatorTests(unittest.TestCase):

    def setUp(self):
        self.evaluator = Evaluator()

    def test_evaluate_template(self):
        output, errors = self.evaluator.evaluate_template("Answer is @(2 + 3)", EvaluationContext())
        self.assertEqual(output, "Answer is 5")
        self.assertEqual(errors, [])

        # with unbalanced expression
        output, errors = self.evaluator.evaluate_template("Answer is @(2 + 3", EvaluationContext())
        self.assertEqual(output, "Answer is @(2 + 3")
        self.assertEqual(errors, [])

        # with illegal char
        output, errors = self.evaluator.evaluate_template("@('x')", EvaluationContext())
        self.assertEqual(output, "@('x')")
        self.assertEqual(errors, ["Expression error at: '"])

    def test_evaluate_template_with_resolve_available_strategy(self):
        context = EvaluationContext()
        context.put_variable("foo", 5)
        context.put_variable("bar", "x")

        output, errors = self.evaluator.evaluate_template("@(1 + 2)", context, False, EvaluationStrategy.RESOLVE_AVAILABLE)
        self.assertEqual(output, "3")

        output, errors = self.evaluator.evaluate_template("Hi @contact.name", context, False, EvaluationStrategy.RESOLVE_AVAILABLE)
        self.assertEqual(output, "Hi @contact.name")

        output, errors = self.evaluator.evaluate_template("@(foo + contact.name + bar)", context, False, EvaluationStrategy.RESOLVE_AVAILABLE)
        self.assertEqual(output, "@(5+contact.name+\"x\")")

    def test_evaluate_expression(self):
        context = EvaluationContext()
        context.put_variable("foo", 5)
        context.put_variable("bar", 3)

        self.assertEqual(self.evaluator.evaluate_expression("true", context), True)
        self.assertEqual(self.evaluator.evaluate_expression("FALSE", context), False)

        self.assertEqual(self.evaluator.evaluate_expression("10", context), Decimal(10))
        self.assertEqual(self.evaluator.evaluate_expression("1234.5678", context), Decimal("1234.5678"))

        self.assertEqual(self.evaluator.evaluate_expression("\"\"", context), "")
        self.assertEqual(self.evaluator.evaluate_expression("\"سلام\"", context), "سلام")
        self.assertEqual(self.evaluator.evaluate_expression("\"He said \"\"hi\"\" \"", context), "He said \"hi\" ")

        self.assertEqual(self.evaluator.evaluate_expression("-10", context), Decimal(-10))
        self.assertEqual(self.evaluator.evaluate_expression("1 + 2", context), Decimal(3))
        self.assertEqual(self.evaluator.evaluate_expression("1.3 + 2.2", context), Decimal("3.5"))
        self.assertEqual(self.evaluator.evaluate_expression("1.3 - 2.2", context), Decimal("-0.9"))
        self.assertEqual(self.evaluator.evaluate_expression("4 * 2", context), Decimal(8))
        self.assertEqual(self.evaluator.evaluate_expression("4 / 2", context), Decimal("2.0000000000"))
        self.assertEqual(self.evaluator.evaluate_expression("4 ^ 2", context), Decimal(16))
        self.assertEqual(self.evaluator.evaluate_expression("4 ^ 0.5", context), Decimal(2))
        self.assertEqual(self.evaluator.evaluate_expression("4 ^ -1", context), Decimal("0.25"))

        self.assertEqual(self.evaluator.evaluate_expression("\"foo\" & \"bar\"", context), "foobar")
        self.assertEqual(self.evaluator.evaluate_expression("2 & 3 & 4", context), "234")

        # check precedence
        self.assertEqual(self.evaluator.evaluate_expression("2 + 3 / 4 - 5 * 6", context), Decimal("-27.2500000000"))
        self.assertEqual(self.evaluator.evaluate_expression("2 & 3 + 4 & 5", context), "275")

        # check associativity
        self.assertEqual(self.evaluator.evaluate_expression("2 - -2 + 7", context), Decimal(11))
        self.assertEqual(self.evaluator.evaluate_expression("2 ^ 3 ^ 4", context), Decimal(4096))

        self.assertEqual(self.evaluator.evaluate_expression("FOO", context), 5)
        self.assertEqual(self.evaluator.evaluate_expression("foo + bar", context), Decimal(8))

        self.assertEqual(self.evaluator.evaluate_expression("len(\"abc\")", context), 3)
        self.assertEqual(self.evaluator.evaluate_expression("SUM(1, 2, 3)", context), Decimal(6))

        self.assertEqual(self.evaluator.evaluate_expression("FIXED(1234.5678)", context), "1,234.57")
        self.assertEqual(self.evaluator.evaluate_expression("FIXED(1234.5678, 1)", context), "1,234.6")
        self.assertEqual(self.evaluator.evaluate_expression("FIXED(1234.5678, 1, True)", context), "1234.6")

        
class FunctionsTests(unittest.TestCase):
    
    def test_excel(self):
        variables = {'date': {'now': '01-02-2014 03:55', 'today': '01-02-2014'}}
        context = EvaluationContext(variables, pytz.timezone("Africa/Kigali"), DateStyle.DAY_FIRST)

        # text functions
        self.assertEqual(excel.char(context, 9), '\t')
        self.assertEqual(excel.char(context, 10), '\n')
        self.assertEqual(excel.char(context, 13), '\r')
        self.assertEqual(excel.char(context, 32), ' ')
        self.assertEqual(excel.char(context, 65), 'A')

        self.assertEqual(excel.clean(context, 'Hello \nwo\trl\rd'), 'Hello world')

        self.assertEqual(excel.code(context, '\t'), 9)
        self.assertEqual(excel.code(context, '\n'), 10)

        self.assertEqual(excel.concatenate(context, 'Hello', 4, '\n'), 'Hello4\n')
        self.assertEqual(excel.concatenate(context, 'واحد', ' ', 'إثنان', ' ', 'ثلاثة'), 'واحد إثنان ثلاثة')

        self.assertEqual(excel.fixed(context, Decimal('1234.5678')), '1,234.57')  # default is 2 decimal places w/ comma
        self.assertEqual(excel.fixed(context, '1234.5678', 1), '1,234.6')
        self.assertEqual(excel.fixed(context, '1234.5678', 2), '1,234.57')
        self.assertEqual(excel.fixed(context, '1234.5678', 3), '1,234.568')
        self.assertEqual(excel.fixed(context, '1234.5678', 4), '1,234.5678')
        self.assertEqual(excel.fixed(context, '1234.5678', 0), '1,235')
        self.assertEqual(excel.fixed(context, '1234.5678', -1), '1,230')
        self.assertEqual(excel.fixed(context, '1234.5678', -2), '1,200')
        self.assertEqual(excel.fixed(context, '1234.5678', -3), '1,000')
        self.assertEqual(excel.fixed(context, '1234.5678', -4), '0')
        self.assertEqual(excel.fixed(context, '1234.5678', 3, True), '1234.568')
        self.assertEqual(excel.fixed(context, '1234.5678', -2, True), '1200')

        self.assertEqual(excel.left(context, 'abcdef', 0), '')
        self.assertEqual(excel.left(context, 'abcdef', 2), 'ab')
        self.assertEqual(excel.left(context, 'واحد', 2), 'وا')
        self.assertRaises(ValueError, excel.left, context, 'abcd', -1)  # exception for negative char count

        self.assertEqual(excel._len(context, ''), 0)
        self.assertEqual(excel._len(context, 'abc'), 3)
        self.assertEqual(excel._len(context, 'واحد'), 4)

        self.assertEqual(excel.lower(context, 'aBcD'), 'abcd')
        self.assertEqual(excel.lower(context, 'A واحد'), 'a واحد')

        self.assertEqual(excel.proper(context, 'first-second third'), 'First-Second Third')
        self.assertEqual(excel.proper(context, 'واحد abc ثلاثة'), 'واحد Abc ثلاثة')

        self.assertEqual(excel.rept(context, 'abc', 3), 'abcabcabc')
        self.assertEqual(excel.rept(context, 'واحد', 3), 'واحدواحدواحد')

        self.assertEqual(excel.right(context, 'abcdef', 0), '')
        self.assertEqual(excel.right(context, 'abcdef', 2), 'ef')
        self.assertEqual(excel.right(context, 'واحد', 2), 'حد')
        self.assertRaises(ValueError, excel.right, context, 'abcd', -1)  # exception for negative char count

        self.assertEqual(excel.substitute(context, 'hello Hello world', 'hello', 'bonjour'), 'bonjour Hello world')  # case-sensitive
        self.assertEqual(excel.substitute(context, 'hello hello world', 'hello', 'bonjour'), 'bonjour bonjour world')  # all instances
        self.assertEqual(excel.substitute(context, 'hello hello world', 'hello', 'bonjour', 2), 'hello bonjour world')  # specific instance
        self.assertEqual(excel.substitute(context, 'واحد إثنان ثلاثة', 'واحد', 'إثنان'), 'إثنان إثنان ثلاثة')

        self.assertEqual(excel.unichar(context, 65), 'A')
        self.assertEqual(excel.unichar(context, 1575), 'ا')

        self.assertEqual(excel._unicode(context, '\t'), 9)
        self.assertEqual(excel._unicode(context, '\u04d2'), 1234)
        self.assertEqual(excel._unicode(context, 'ا'), 1575)
        self.assertRaises(ValueError, excel._unicode, context, '')  # exception for empty string

        self.assertEqual(excel.upper(context, 'aBcD'), 'ABCD')
        self.assertEqual(excel.upper(context, 'a واحد'), 'A واحد')

        # date functions
        self.assertEqual(excel.date(context, 2012, "3", Decimal(2.0)), date(2012, 3, 2))

        self.assertEqual(excel.datevalue(context, "2-3-13"), date(2013, 3, 2))

        self.assertEqual(excel.day(context, date(2012, 3, 2)), 2)

        self.assertEqual(excel.edate(context, date(2013, 3, 2), 1), date(2013, 4, 2))
        self.assertEqual(excel.edate(context, '01-02-2014', -2), date(2013, 12, 1))

        self.assertEqual(excel.hour(context, '01-02-2014 03:55'), 3)

        self.assertEqual(excel.minute(context, '01-02-2014 03:55'), 55)

        self.assertEqual(excel.now(context), datetime(2014, 2, 1, 3, 55, 0, 0, pytz.timezone("Africa/Kigali")))

        self.assertEqual(excel.second(context, '01-02-2014 03:55:30'), 30)

        self.assertEqual(excel.time(context, 1, 30, 15), time(1, 30, 15))

        self.assertEqual(excel.timevalue(context, '1:30:15'), time(1, 30, 15))

        self.assertEqual(excel.today(context), date(2014, 2, 1))

        self.assertEqual(excel.weekday(context, date(2015, 8, 15)), 7)  # Sat = 7
        self.assertEqual(excel.weekday(context, "16th Aug 2015"), 1)  # Sun = 1

        self.assertEqual(excel.year(context, date(2012, 3, 2)), 2012)

        # math functions
        self.assertEqual(excel._abs(context, 1), 1)
        self.assertEqual(excel._abs(context, -1), 1)

        self.assertEqual(excel._int(context, Decimal('8.9')), 8)
        self.assertEqual(excel._int(context, Decimal('-8.9')), -9)

        self.assertEqual(excel._max(context, 1), 1)
        self.assertEqual(excel._max(context, 1, 3, 2, -5), 3)
        self.assertEqual(excel._max(context, -2, -5), -2)

        self.assertEqual(excel._min(context, 1), 1)
        self.assertEqual(excel._min(context, -1, -3, -2, 5), -3)
        self.assertEqual(excel._min(context, -2, -5), -5)

        self.assertEqual(excel.mod(context, Decimal(3), 2), 1)
        self.assertEqual(excel.mod(context, Decimal(-3), Decimal(2)), 1)
        self.assertEqual(excel.mod(context, Decimal(3), Decimal(-2)), -1)
        self.assertEqual(excel.mod(context, Decimal(-3), Decimal(-2)), -1)

        self.assertEqual(excel._power(context, '4', '2'), Decimal('16'))
        self.assertEqual(excel._power(context, '4', '0.5'), Decimal('2'))

        self.assertEqual(excel._sum(context, 1), 1)
        self.assertEqual(excel._sum(context, 1, 2, 3), 6)

        # logical functions
        self.assertEqual(excel._and(context, False), False)
        self.assertEqual(excel._and(context, True), True)
        self.assertEqual(excel._and(context, 1, True, "true"), True)
        self.assertEqual(excel._and(context, 1, True, "true", 0), False)

        self.assertEqual(excel.false(), False)

        self.assertEqual(excel._if(context, True), 0)
        self.assertEqual(excel._if(context, True, 'x', 'y'), 'x')
        self.assertEqual(excel._if(context, 'true', 'x', 'y'), 'x')
        self.assertEqual(excel._if(context, False), False)
        self.assertEqual(excel._if(context, False, 'x', 'y'), 'y')
        self.assertEqual(excel._if(context, 0, 'x', 'y'), 'y')

        self.assertEqual(excel._or(context, False), False)
        self.assertEqual(excel._or(context, True), True)
        self.assertEqual(excel._or(context, 1, False, "false"), True)
        self.assertEqual(excel._or(context, 0, True, "false"), True)

        self.assertEqual(excel.true(), True)

    def test_custom(self):
        context = EvaluationContext({}, pytz.timezone("Africa/Kigali"), DateStyle.DAY_FIRST)

        self.assertEqual(custom.field(context, '15+M+Seattle', 1, '+'), '15')
        self.assertEqual(custom.field(context, '15 M Seattle', 1), '15')
        self.assertEqual(custom.field(context, '15+M+Seattle', 2, '+'), 'M')
        self.assertEqual(custom.field(context, '15+M+Seattle', 3, '+'), 'Seattle')
        self.assertEqual(custom.field(context, '15+M+Seattle', 4, '+'), '')
        self.assertEqual(custom.field(context, '15    M  Seattle', 2), 'M')
        self.assertEqual(custom.field(context, ' واحد إثنان-ثلاثة ', 1), 'واحد')
        self.assertRaises(ValueError, custom.field, context, '15+M+Seattle', 0)

        self.assertEqual('', custom.first_word(context, '  '))
        self.assertEqual('abc', custom.first_word(context, ' abc '))
        self.assertEqual('abc', custom.first_word(context, ' abc '))
        self.assertEqual('abc', custom.first_word(context, ' abc def ghi'))
        self.assertEqual('واحد', custom.first_word(context, ' واحد '))
        self.assertEqual('واحد', custom.first_word(context, ' واحد إثنان ثلاثة '))

        self.assertEqual('25%', custom.percent(context, '0.25321'))
        self.assertEqual('33%', custom.percent(context, Decimal('0.33')))

        self.assertEqual('1 2 3 4 , 5 6 7 8 , 9 0 1 2 , 3 4 5 6', custom.read_digits(context, '1234567890123456'))  # credit card
        self.assertEqual('1 2 3 , 4 5 6 , 7 8 9 , 0 1 2', custom.read_digits(context, '+123456789012'))  # phone number
        self.assertEqual('1 2 3 , 4 5 6', custom.read_digits(context, '123456'))  # triplets
        self.assertEqual('1 2 3 , 4 5 , 6 7 8 9', custom.read_digits(context, '123456789'))  # soc security
        self.assertEqual('1,2,3,4,5', custom.read_digits(context, '12345'))  # regular number, street address, etc
        self.assertEqual('1,2,3', custom.read_digits(context, '123'))  # regular number, street address, etc
        self.assertEqual('', custom.read_digits(context, ''))  # empty

        self.assertEqual('', custom.remove_first_word(context, 'abc'))
        self.assertEqual('', custom.remove_first_word(context, ' abc '))
        self.assertEqual('def-ghi ', custom.remove_first_word(context, ' abc def-ghi '))  # should preserve remainder of text
        self.assertEqual('', custom.remove_first_word(context, ' واحد '))
        self.assertEqual('إثنان ثلاثة ', custom.remove_first_word(context, ' واحد إثنان ثلاثة '))

        self.assertEqual('abc', custom.word(context, ' abc def ghi', 1))
        self.assertEqual('ghi', custom.word(context, 'abc-def  ghi  jkl', 3))
        self.assertEqual('jkl', custom.word(context, 'abc-def  ghi  jkl', 3, True))
        self.assertEqual('jkl', custom.word(context, 'abc-def  ghi  jkl', '3', 'TRUE'))  # string args only
        self.assertEqual('jkl', custom.word(context, 'abc-def  ghi  jkl', -1))  # negative index
        self.assertEqual('', custom.word(context, ' abc def   ghi', 6))  # out of range
        self.assertEqual('', custom.word(context, '', 1))
        self.assertEqual('واحد', custom.word(context, ' واحد إثنان ثلاثة ', 1))
        self.assertEqual('ثلاثة', custom.word(context, ' واحد إثنان ثلاثة ', -1))
        self.assertRaises(ValueError, custom.word, context, '', 0)  # number cannot be zero

        self.assertEqual(0, custom.word_count(context, ''))
        self.assertEqual(4, custom.word_count(context, ' abc-def  ghi  jkl'))
        self.assertEqual(4, custom.word_count(context, ' abc-def  ghi  jkl', False))
        self.assertEqual(3, custom.word_count(context, ' abc-def  ghi  jkl', True))
        self.assertEqual(3, custom.word_count(context, ' واحد إثنان-ثلاثة ', False))
        self.assertEqual(2, custom.word_count(context, ' واحد إثنان-ثلاثة ', True))

        self.assertEqual('abc def', custom.word_slice(context, ' abc  def ghi-jkl ', 1, 3))
        self.assertEqual('ghi jkl', custom.word_slice(context, ' abc  def ghi-jkl ', 3, 0))
        self.assertEqual('ghi-jkl', custom.word_slice(context, ' abc  def ghi-jkl ', 3, 0, True))
        self.assertEqual('ghi jkl', custom.word_slice(context, ' abc  def ghi-jkl ', '3', '0', 'false'))  # string args only
        self.assertEqual('ghi jkl', custom.word_slice(context, ' abc  def ghi-jkl ', 3))
        self.assertEqual('def ghi', custom.word_slice(context, ' abc  def ghi-jkl ', 2, -1))
        self.assertEqual('jkl', custom.word_slice(context, ' abc  def ghi-jkl ', -1))
        self.assertEqual('def', custom.word_slice(context, ' abc  def ghi-jkl ', 2, -1, True))
        self.assertEqual('واحد إثنان', custom.word_slice(context, ' واحد إثنان ثلاثة ', 1, 3))
        self.assertRaises(ValueError, custom.word_slice, context, ' abc  def ghi-jkl ', 0)  # start can't be zero


class UtilsTests(unittest.TestCase):

    def test_urlquote(self):
        self.assertEqual(urlquote(""), "")
        self.assertEqual(urlquote("?!=Jow&Flow"), "%3F%21%3DJow%26Flow")

    def test_decimal_pow(self):
        self.assertEqual(decimal_pow(Decimal(4), Decimal(2)), Decimal(16))
        self.assertEqual(decimal_pow(Decimal(4), Decimal('0.5')), Decimal(2))
        self.assertEqual(decimal_pow(Decimal(2), Decimal(-2)), Decimal('0.25'))


class TemplateTests(unittest.TestCase):

    def test_templates(self):
        evaluator = Evaluator(allowed_top_levels=("channel", "contact", "date", "extra", "flow", "step"))

        with codecs.open('test_files/template_tests.json', 'r', 'utf-8') as tests_file:
            tests_json = json_strip_comments(tests_file.read())
            tests_json = json.loads(tests_json, parse_float=Decimal)
            tests = []
            for test_json in tests_json:
                tests.append(TemplateTest(test_json))

        failures = []
        start = int(round(clock() * 1000))

        for test in tests:
            if not test.run(evaluator):
                failures.append(test)

        duration = int(round(clock() * 1000)) - start

        print("Completed %d template tests in %dms (failures=%d)" % (len(tests), duration, len(failures)))

        if failures:
            print("Failed tests:")

            for test in failures:
                print("========================================\n")
                print("Template: " + test.template)
                if test.expected_output is not None:
                    print("Expected output: " + test.expected_output)
                else:
                    print("Expected output regex: " + test.expected_output_regex)
                print("Actual output: " + test.actual_output)
                print("Expected errors: " + ', '.join(test.expected_errors))
                print("Actual errors: " + ', '.join(test.actual_errors))

            self.fail("There were failures in the template tests")  # fail unit test if there were any errors


class TemplateTest(object):

    def __init__(self, json_obj):
        self.template = json_obj['template']
        self.context = EvaluationContext.from_json(json_obj['context'])
        self.url_encode = json_obj['url_encode']
        self.expected_output = json_obj.get('output', None)
        self.expected_output_regex = json_obj.get('output_regex', None)
        self.expected_errors = json_obj['errors']

        self.actual_output = None
        self.actual_errors = None

    def run(self, evaluator):
        output, errors = evaluator.evaluate_template(self.template, self.context, self.url_encode)
        self.actual_output = output
        self.actual_errors = errors

        if self.expected_output is not None:
            if self.expected_output != self.actual_output:
                return False
        else:
            if not regex.compile(self.expected_output_regex).fullmatch(self.actual_output):
                return False

        return self.expected_errors == self.actual_errors


def json_strip_comments(text):
    """
    Strips /* ... */ style comments from JSON
    """
    pattern = regex.compile(r'/\*[^\*]+\*/', regex.DOTALL|regex.MULTILINE|regex.UNICODE)
    match = pattern.search(text)
    while match:
        text = text[:match.start()] + text[match.end():]
        match = pattern.search(text)
    return text
