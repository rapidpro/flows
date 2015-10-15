from __future__ import absolute_import, unicode_literals

import phonenumbers
import regex

from abc import ABCMeta, abstractmethod
from decimal import Decimal
from temba_expressions import conversions, EvaluationError
from temba_expressions.utils import tokenize
from . import TranslatableText, FlowParseException
from ..utils import edit_distance


class Test(object):
    """
    A test which can be evaluated to true or false on a given string
    """
    __metaclass__ = ABCMeta

    CLASS_BY_TYPE = None  # lazily initialized below

    @classmethod
    def from_json(cls, json_obj, context):
        if not cls.CLASS_BY_TYPE:
            cls.CLASS_BY_TYPE = {
                TrueTest.TYPE: TrueTest,
                FalseTest.TYPE: FalseTest,
                AndTest.TYPE: AndTest,
                OrTest.TYPE: OrTest,
                NotEmptyTest.TYPE: NotEmptyTest,
                ContainsTest.TYPE: ContainsTest,
                ContainsAnyTest.TYPE: ContainsAnyTest,
                StartsWithTest.TYPE: StartsWithTest,
                RegexTest.TYPE: RegexTest,
                HasNumberTest.TYPE: HasNumberTest,
                EqualTest.TYPE: EqualTest,
                LessThanTest.TYPE: LessThanTest,
                LessThanOrEqualTest.TYPE: LessThanOrEqualTest,
                GreaterThanTest.TYPE: GreaterThanTest,
                GreaterThanOrEqualTest.TYPE: GreaterThanOrEqualTest,
                BetweenTest.TYPE: BetweenTest,
                HasDateTest.TYPE: HasDateTest,
                DateEqualTest.TYPE: DateEqualTest,
                DateAfterTest.TYPE: DateAfterTest,
                DateBeforeTest.TYPE: DateBeforeTest,
                HasPhoneTest.TYPE: HasPhoneTest,
                HasStateTest.TYPE: HasStateTest,
                HasDistrictTest.TYPE: HasDistrictTest
            }

        test_type = json_obj['type']
        test_cls = cls.CLASS_BY_TYPE.get(test_type, None)
        if not test_cls:
            raise FlowParseException("Unknown test type: %s" % test_type)

        return test_cls.from_json(json_obj, context)

    @abstractmethod
    def evaluate(self, runner, run, context, text):
        """
        Evaluates this test. Subclasses must implement this.
        """
        pass

    class Result(object):
        """
        Holds the result of a test evaluation (matched + the value matched)
        """
        def __init__(self, matched, value):
            self.matched = matched
            self.value = value

        @classmethod
        def match(cls, value):
            return cls(True, value)


Test.Result.NO_MATCH = Test.Result(False, None)


class TrueTest(Test):
    """
    Test that always returns true
    """
    TYPE = 'true'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def evaluate(self, runner, run, context, text):
        return Test.Result.match(text)


class FalseTest(Test):
    """
    Test that always returns false
    """
    TYPE = 'false'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def evaluate(self, runner, run, context, text):
        return Test.Result(False, text)


class AndTest(Test):
    """
    Test which returns the AND'ed result of other tests
    """
    TYPE = 'and'

    def __init__(self, tests):
        self.tests = tests

    @classmethod
    def from_json(cls, json_obj, context):
        return AndTest([Test.from_json(t, context) for t in json_obj['tests']])

    def evaluate(self, runner, run, context, text):
        matches = []
        for test in self.tests:
            result = test.evaluate(runner, run, context, text)
            if result.matched:
                matches.append(conversions.to_string(result.value, context))
            else:
                return Test.Result.NO_MATCH

        # all came out true, we are true
        return Test.Result.match(" ".join(matches))


class OrTest(Test):
    """
    Test which returns the OR'ed result of other tests
    """
    TYPE = 'or'

    def __init__(self, tests):
        self.tests = tests

    @classmethod
    def from_json(cls, json_obj, context):
        return OrTest([Test.from_json(t, context) for t in json_obj['tests']])

    def evaluate(self, runner, run, context, text):
        for test in self.tests:
            result = test.evaluate(runner, run, context, text)
            if result.matched:
                return Test.Result.match(result.value)

        return Test.Result.NO_MATCH


class NotEmptyTest(Test):
    """
    Test that returns whether the input is non-empty (and non-blank)
    """
    TYPE = 'not_empty'

    @classmethod
    def from_json(cls, json_obj, context):
        return NotEmptyTest()

    def evaluate(self, runner, run, context, text):
        text = text.strip()

        if len(text):
            return Test.Result.match(text)
        else:
            return Test.Result.NO_MATCH


class TranslatableTest(Test):
    """
    Base class for tests that have a translatable test argument
    """
    __metaclass__ = ABCMeta

    def __init__(self, test):
        self.test = test

    def evaluate(self, runner, run, context, text):
        localized_test = self.test.get_localized(run)

        return self.evaluate_for_localized(runner, run, context, text, localized_test)

    @abstractmethod
    def evaluate_for_localized(self, runner, run, context, text, localized_test):
        """
        Evaluates the test against the given localized text value. Subclasses must implement this.
        """
        pass


class ContainsTest(TranslatableTest):
    """
    Test that returns whether the text contains the given words
    """
    TYPE = 'contains'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(TranslatableText.from_json(json_obj['test']))

    @staticmethod
    def test_in_words(test, words, raw_words):
        for index, word in enumerate(words):
            if word == test:
                return raw_words[index]

            # words are over 4 characters and start with the same letter
            if len(word) > 4 and len(test) > 4 and word[0] == test[0]:
                # edit distance of 1 or less is a match
                if edit_distance(word, test) <= 1:
                    return raw_words[index]

        return None

    def evaluate_for_localized(self, runner, run, context, text, localized_test):
        localized_test, errors = runner.substitute_variables(localized_test, context)

        # tokenize our test
        tests = tokenize(localized_test.lower())

        # tokenize our input
        words = tokenize(text.lower())
        raw_words = tokenize(text)

        # run through each of our tests
        matches = []
        for test in tests:
            match = self.test_in_words(test, words, raw_words)
            if match:
                matches.append(match)

        # we are a match only if every test matches
        if len(matches) == len(tests):
            return Test.Result.match(" ".join(matches))
        else:
            return Test.Result.NO_MATCH


class ContainsAnyTest(ContainsTest):
    """
    Test that returns whether the text contains any of the given words
    """
    TYPE = 'contains_any'

    def evaluate_for_localized(self, runner, run, context, text, localized_test):
        localized_test, errors = runner.substitute_variables(localized_test, context)

        # tokenize our test
        tests = tokenize(localized_test.lower())

        # tokenize our input
        words = tokenize(text.lower())
        raw_words = tokenize(text)

        # run through each of our tests
        matches = []
        for test in tests:
            match = self.test_in_words(test, words, raw_words)
            if match:
                matches.append(match)

        # we are a match if at least one test matches
        if len(matches) > 0:
            return Test.Result.match(" ".join(matches))
        else:
            return Test.Result.NO_MATCH


class StartsWithTest(TranslatableTest):
    """
    Test that returns whether the text starts with the given text
    """
    TYPE = 'starts'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(TranslatableText.from_json(json_obj['test']))

    def evaluate_for_localized(self, runner, run, context, text, localized_test):
        localized_test, errors = runner.substitute_variables(localized_test, context)

        # strip leading and trailing whitespace
        text = text.strip()

        # see whether we start with our test
        if text.lower().startswith(localized_test.lower()):
            return Test.Result.match(text[0:len(localized_test)])
        else:
            return Test.Result.NO_MATCH


class RegexTest(TranslatableTest):
    """
    Test that returns whether the input matches a regular expression
    """
    TYPE = 'regex'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(TranslatableText.from_json(json_obj['test']))

    def evaluate_for_localized(self, runner, run, context, text, localized_test):
        try:
            # check whether we match
            rexp = regex.compile(localized_test, regex.UNICODE | regex.IGNORECASE | regex.MULTILINE | regex.V0)
            match = rexp.search(text)

            # if so, $0 will be what we return
            if match:
                return_match = match.group(0)

                # build up a dictionary that contains indexed values
                group_dict = match.groupdict()
                for idx in range(rexp.groups + 1):
                    group_dict[str(idx)] = match.group(idx)

                # update @extra
                runner.update_extra(run, group_dict)

                # return all matched values
                return Test.Result.match(return_match)

        except Exception:
            pass

        return Test.Result.NO_MATCH


class NumericTest(Test):
    """
    Base class for tests that are numerical
    """
    __metaclass__ = ABCMeta

    @staticmethod
    def extract_decimal(text):
        """
        A very flexible decimal parser
        :param text: the text to be parsed
        :return: the decimal value
        """
        # common substitutions
        original_text = text
        text = text.replace('l', '1').replace('o', '0').replace('O', '0')

        try:
            return Decimal(text)
        except Exception as e:
            # we only try this hard if we haven't already substituted characters
            if original_text == text:
                # does this start with a number? just use that part if so
                match = regex.match(r'^(\d+).*$', text, flags=regex.UNICODE | regex.V0)
                if match:
                    return Decimal(match.group(1))
            raise e

    def evaluate(self, runner, run, context, text):
        text = text.replace(',', '')  # so that 1,234 is parsed as 1234

        # test every word in the message against our test
        for word in regex.split(r'\s+', text, flags=regex.UNICODE | regex.V0):
            try:
                decimal = self.extract_decimal(word)
                if self.evaluate_for_decimal(runner, context, decimal):
                    return Test.Result.match(decimal)
            except Exception:
                pass

        return Test.Result.NO_MATCH

    @abstractmethod
    def evaluate_for_decimal(self, runner, context, decimal):
        """
        Evaluates the test against the given decimal value. Subclasses must implement this.
        """
        pass


class HasNumberTest(NumericTest):
    """
    Test which returns whether input has a number
    """
    TYPE = 'number'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def evaluate_for_decimal(self, runner, context, decimal):
        return True  # this method is only called on decimals parsed from the input


class NumericComparisonTest(NumericTest):
    """
    Base class for numeric tests which compare the input against a value
    """
    __metaclass__ = ABCMeta

    def __init__(self, test):
        self.test = test

    def evaluate_for_decimal(self, runner, context, input):
        test, errors = runner.substitute_variables(self.test, context)

        if not errors:
            try:
                test_val = Decimal(test.strip())
                return self.do_comparison(input, test_val)
            except Exception:
                pass
        return False

    @abstractmethod
    def do_comparison(self, input, test):
        pass


class EqualTest(NumericComparisonTest):
    """
    Test which returns whether input is numerically equal a value
    """
    TYPE = 'eq'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj['test'])

    def do_comparison(self, input, test):
        return input == test


class LessThanTest(NumericComparisonTest):
    """
    Test which returns whether input is numerically less than a value
    """
    TYPE = 'lt'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj['test'])

    def do_comparison(self, input, test):
        return input < test


class LessThanOrEqualTest(NumericComparisonTest):
    """
    Test which returns whether input is numerically less than or equal to a value
    """
    TYPE = 'lte'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj['test'])

    def do_comparison(self, input, test):
        return input <= test


class GreaterThanTest(NumericComparisonTest):
    """
    Test which returns whether input is numerically greater than a value
    """
    TYPE = 'gt'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj['test'])

    def do_comparison(self, input, test):
        return input > test


class GreaterThanOrEqualTest(NumericComparisonTest):
    """
    Test which returns whether input is numerically greater than or equal to a value
    """
    TYPE = 'gte'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj['test'])

    def do_comparison(self, input, test):
        return input >= test


class BetweenTest(NumericTest):
    """
    Test which returns whether input is a number between two numbers (inclusive)
    """
    TYPE = "between"

    def __init__(self, min_val, max_val):
        self.min = min_val
        self.max = max_val

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj['min'], json_obj['max'])

    def evaluate_for_decimal(self, runner, context, decimal):
        min_val, min_errors = runner.substitute_variables(self.min, context)
        max_val, max_errors = runner.substitute_variables(self.max, context)

        if not min_errors and not max_errors:
            try:
                return Decimal(min_val) <= decimal <= Decimal(max_val)
            except Exception:
                pass

        return False


class DateTest(Test):
    """
    Base class for tests that are date based
    """
    __metaclass__ = ABCMeta

    def evaluate(self, runner, run, context, text):
        try:
            date = conversions.to_date(text, context)
            if self.evaluate_for_date(runner, context, date):
                return Test.Result.match(date)
        except EvaluationError:
            pass

        return Test.Result.NO_MATCH

    @abstractmethod
    def evaluate_for_date(self, runner, context, date):
        pass


class HasDateTest(DateTest):
    """
    Test which returns whether input contains a valid date
    """
    TYPE = 'date'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def evaluate_for_date(self, runner, context, date):
        return True  # this method is only called on dates parsed from the input


class DateComparisonTest(DateTest):
    """
    Base class for date tests which compare the input against a value
    """
    __metaclass__ = ABCMeta

    def __init__(self, test):
        self.test = test

    def evaluate_for_date(self, runner, context, date):
        test, errors = runner.substitute_variables(self.test, context)

        if not errors:
            try:
                test_val = conversions.to_date(test, context)
                return self.do_comparison(date, test_val)
            except Exception:
                pass

        return False

    @abstractmethod
    def do_comparison(self, date, test):
        pass


class DateEqualTest(DateComparisonTest):
    """
    Test which returns whether input is a date equal to the given value
    """
    TYPE = 'date_equal'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj['test'])

    def do_comparison(self, date, test):
        return date == test


class DateAfterTest(DateComparisonTest):
    """
    Test which returns whether input is a date after the given value
    """
    TYPE = 'date_after'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj['test'])

    def do_comparison(self, date, test):
        return date >= test


class DateBeforeTest(DateComparisonTest):
    """
    Test which returns whether input is a date before the given value
    """
    TYPE = 'date_before'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj['test'])

    def do_comparison(self, date, test):
        return date <= test


class HasPhoneTest(Test):
    """
    Test that returns whether the text contains a valid phone number
    """
    TYPE = 'phone'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def evaluate(self, runner, run, context, text):
        country = run.org.country

        # try to find a phone number in the text we have been sent
        matches = phonenumbers.PhoneNumberMatcher(text, country)

        # try it as an international number if we failed
        if not matches.has_next():
            matches = phonenumbers.PhoneNumberMatcher('+' + text, country)

        if matches.has_next():
            number = next(matches).number
            number = phonenumbers.format_number(number, phonenumbers.PhoneNumberFormat.E164)
            return Test.Result.match(number)
        else:
            return Test.Result.NO_MATCH


class HasStateTest(Test):
    """
    Test that returns whether the text contains a valid state
    """
    TYPE = "state"

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def evaluate(self, runner, run, context, text):
        from ..runner import Location

        country = run.org.country
        if country:
            state = runner.parse_location(text, country, Location.Level.STATE, None)
            if state:
                return Test.Result.match(state.name)

        return Test.Result.NO_MATCH


class HasDistrictTest(Test):
    """
    Test that returns whether the text contains a valid district in the given state
    """
    TYPE = "district"

    def __init__(self, state):
        self.state = state

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj.get('test', None))

    def evaluate(self, runner, run, context, text):
        from ..runner import Location

        country = run.org.country
        if country:
            # state might be an expression
            state_tpl, errors = runner.substitute_variables(self.state, context)

            if not errors:
                state = runner.parse_location(state_tpl, country, Location.Level.STATE, None)
                if state:
                    district = runner.parse_location(text, country, Location.Level.DISTRICT, state)
                    if district:
                        return Test.Result.match(district.name)

        return Test.Result.NO_MATCH
