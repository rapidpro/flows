from __future__ import absolute_import, unicode_literals

import regex

from abc import ABCMeta, abstractmethod
from decimal import Decimal
from temba_expressions.utils import tokenize
from . import TranslatableText, FlowParseException
from ..utils import edit_distance


class Test(object):
    """
    A test which can be evaluated to true or false on a given string
    """
    __metaclass__ = ABCMeta

    CLASS_BY_TYPE = None

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
                # EqualTest.TYPE: EqualTest,
                # LessThanTest.TYPE: LessThanTest,
                # LessThanOrEqualTest.TYPE: LessThanOrEqualTest,
                # GreaterThanTest.TYPE: GreaterThanTest,
                # GreaterThanOrEqualTest.TYPE: GreaterThanOrEqualTest,
                # BetweenTest.TYPE: BetweenTest,
                # HasDateTest.TYPE: HasDateTest,
                # DateEqualTest.TYPE: DateEqualTest,
                # DateAfterTest.TYPE: DateAfterTest,
                # DateBeforeTest.TYPE: DateBeforeTest,
                # HasPhoneTest.TYPE: HasPhoneTest,
                # HasDistrictTest.TYPE: HasDistrictTest,
                # HasStateTest.TYPE: HasStateTest
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
        Holds the result of a test evaluation (matched + the text matched + the value matched)
        """
        def __init__(self, matched, text, value):
            self.matched = matched
            self.text = text
            self.value = value

        @classmethod
        def match(cls, text, value=None):
            if value is None:
                value = text
            return cls(True, text, value)


Test.Result.NO_MATCH = Test.Result(False, None, None)


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
        return Test.Result(False, text, text)


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
                matches.append(result.text)
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
                return Test.Result.match(result.text)

        return Test.Result.NO_MATCH


class NotEmptyTest(Test):
    """
    Test that returns whether the input is non-empty (and non-blank)
    """
    TYPE = 'not_empty'

    @classmethod
    def from_json(cls, json_org, context):
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
    def from_json(cls, json_object, context):
        return cls(TranslatableText.from_json(json_object['test']))

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
    Test that returns whether the text starts with the given string
    """
    TYPE = 'starts'

    @classmethod
    def from_json(cls, json_object, context):
        return cls(TranslatableText.from_json(json_object['test']))

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
    def from_json(cls, json_object, context):
        return cls(TranslatableText.from_json(json_object['test']))

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
                run.extra.update(group_dict)

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
        :return: the decimal value and the parse-able matching text (i.e. after substitutions)
        """
        # common substitutions
        original_text = text
        text = text.replace('l', '1').replace('o', '0').replace('O', '0')

        try:
            return Decimal(text), original_text
        except Exception as e:
            # we only try this hard if we haven't already substituted characters
            if original_text == text:
                # does this start with a number? just use that part if so
                match = regex.match(r'^(\d+).*$', text, flags=regex.UNICODE | regex.V0)
                if match:
                    return Decimal(match.group(1)), match.group(1)
            raise e

    def evaluate(self, runner, run, context, text):
        text = text.replace(',', '')  # so that 1,234 is parsed as 1234

        # test every word in the message against our test
        for word in regex.split(r'\s+', text, flags=regex.UNICODE | regex.V0):
            try:
                decimal, word = self.extract_decimal(word)
                if self.evaluate_for_decimal(runner, context, decimal):
                    return Test.Result.match(word, decimal)
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
