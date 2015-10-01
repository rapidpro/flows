from __future__ import absolute_import, unicode_literals

from abc import ABCMeta, abstractmethod
from temba_expressions.utils import tokenize
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
                # NumberTest.TYPE: NumberTest,
                # LtTest.TYPE: LtTest,
                # LteTest.TYPE: LteTest,
                # GtTest.TYPE: GtTest,
                # GteTest.TYPE: GteTest,
                # EqTest.TYPE: EqTest,
                # BetweenTest.TYPE: BetweenTest,
                # StartsWithTest.TYPE: StartsWithTest,
                # HasDateTest.TYPE: HasDateTest,
                # DateEqualTest.TYPE: DateEqualTest,
                # DateAfterTest.TYPE: DateAfterTest,
                # DateBeforeTest.TYPE: DateBeforeTest,
                # PhoneTest.TYPE: PhoneTest,
                # RegexTest.TYPE: RegexTest,
                # HasDistrictTest.TYPE: HasDistrictTest,
                # HasStateTest.TYPE: HasStateTest
            }

        test_type = json_obj['type']
        test_cls = cls.CLASS_BY_TYPE.get(test_type, None)
        if not test_cls:
            from . import FlowParseException
            raise FlowParseException("Unknown test type: %s" % test_type)

        return test_cls.from_json(json_obj, context)

    @abstractmethod
    def evaluate(self, runner, run, context, text):
        """
        Evaluates this test. Subclasses must implement this.
        """
        pass


class TrueTest(Test):
    """
    Test that always returns true
    """
    TYPE = 'true'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def evaluate(self, runner, run, context, text):
        return True, text


class FalseTest(Test):
    """
    Test that always returns false
    """
    TYPE = 'false'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def evaluate(self, runner, run, context, text):
        return False, text


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
            (result, value) = test.evaluate(runner, run, context, text)
            if result:
                matches.append(value)
            else:
                return False, None

        # all came out true, we are true
        return True, " ".join(matches)


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
            (result, value) = test.evaluate(runner, run, context, text)
            if result:
                return result, value

        return False, None


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
            return True, text
        else:
            return False, None


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
        return cls(json_object['test'])

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
            return True, " ".join(matches)
        else:
            return False, None


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
            return True, " ".join(matches)
        else:
            return False, None
