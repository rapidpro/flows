# coding=utf-8
from __future__ import absolute_import, unicode_literals

import codecs
import datetime
import json
import pytz
import unittest

from decimal import Decimal
from temba_expressions.dates import DateStyle
from temba_expressions.evaluator import EvaluationContext
from .definition.flow import Flow, ActionSet, RuleSet
from .definition.actions import ReplyAction
from .definition.tests import *
from .runner import Contact, ContactUrn, Input, Org, Runner, RunState
from .utils import edit_distance


class BaseFlowsTest(unittest.TestCase):
    def setUp(self):
        self.org = Org("RW", "eng", pytz.timezone("Africa/Kigali"), DateStyle.DAY_FIRST, False)

        self.contact = Contact('1234-1234',
                               "Joe Flow",
                               [ContactUrn.from_string("tel:+260964153686"),
                                ContactUrn.from_string("twitter:realJoeFlow")],
                               ["Testers", "Developers"],
                               {"gender": "M", "age": "34"},
                               'eng')

    @staticmethod
    def read_resource(path):
        with codecs.open('test_files/%s' % path, encoding='utf-8') as f:
            return f.read()


class ContactTest(BaseFlowsTest):
    def test_to_and_from_json(self):
        json_str = json.dumps(self.contact.to_json())

        contact = Contact.from_json(json.loads(json_str))

        self.assertEqual(contact.uuid, '1234-1234')
        self.assertEqual(contact.name, "Joe Flow")
        self.assertEqual(contact.urns, [ContactUrn(ContactUrn.Scheme.TEL, "+260964153686"),
                                        ContactUrn(ContactUrn.Scheme.TWITTER, "realJoeFlow")])
        self.assertEqual(contact.groups, {"Testers", "Developers"})
        self.assertEqual(contact.fields, {"age": "34", "gender": "M"})
        self.assertEqual(contact.language, 'eng')

    def test_get_first_name(self):
        self.assertEqual(self.contact.get_first_name(self.org), "Joe")
        self.contact.name = "Joe"
        self.assertEqual(self.contact.get_first_name(self.org), "Joe")
        self.contact.name = ""
        self.assertEqual(self.contact.get_first_name(self.org), "096 4153686")
        self.contact.name = None
        self.assertEqual(self.contact.get_first_name(self.org), "096 4153686")
        self.org.is_anon = True
        self.assertEqual(self.contact.get_first_name(self.org), "1234-1234")

    def test_set_first_name(self):
        self.contact.set_first_name("Bob")
        self.assertEqual(self.contact.name, "Bob Flow")
        self.contact.name = "Joe McFlow Jr III"
        self.contact.set_first_name("Bob")
        self.assertEqual(self.contact.name, "Bob McFlow Jr III")
        self.contact.name = ""
        self.contact.set_first_name("Bob")
        self.assertEqual(self.contact.name, "Bob")
        self.contact.name = None
        self.contact.set_first_name("Bob")
        self.assertEqual(self.contact.name, "Bob")

    def test_build_context(self):
        context = self.contact.build_context(self.org)
        self.assertEqual(context, {'*': "Joe Flow",
                                   'name': "Joe Flow",
                                   'first_name': "Joe",
                                   'tel_e164': "+260964153686",
                                   'groups': "Testers,Developers",
                                   'uuid': "1234-1234",
                                   'language': "eng",
                                   'tel': "096 4153686",
                                   'twitter': "realJoeFlow",
                                   'gender': "M",
                                   'age': "34"})
        self.org.is_anon = True
        context = self.contact.build_context(self.org)
        self.assertEqual(context, {'*': "Joe Flow",
                                   'name': "Joe Flow",
                                   'first_name': "Joe",
                                   'tel_e164': "1234-1234",
                                   'groups': "Testers,Developers",
                                   'uuid': "1234-1234",
                                   'language': "eng",
                                   'tel': "1234-1234",
                                   'twitter': "1234-1234",
                                   'gender': "M",
                                   'age': "34"})


class FlowTest(BaseFlowsTest):
    def test_from_json(self):
        flow = Flow.from_json(json.loads(self.read_resource('test_flows/mushrooms.json')))

        self.assertEqual(flow.base_language, 'eng')
        self.assertEqual(flow.flow_type, Flow.Type.FLOW)
        self.assertEqual(flow.languages, {'eng', 'fre'})

        as1 = flow.entry

        self.assertIsInstance(as1, ActionSet)
        self.assertEqual(as1.uuid, '32cf414b-35e3-4c75-8a78-d5f4de925e13')
        self.assertEqual(len(as1.actions), 1)
        self.assertIsInstance(as1.actions[0], ReplyAction)

        rs1 = as1.destination

        self.assertIsInstance(rs1, RuleSet)
        self.assertEqual(rs1.uuid, '1e318293-4730-481c-b455-daaaf86b2e6c')
        self.assertEqual(rs1.ruleset_type, RuleSet.Type.WAIT_MESSAGE)
        self.assertEqual(rs1.label, "Response 1")
        self.assertEqual(rs1.operand, "@step.value")

    def test_from_json_with_empty_flow(self):
        flow = Flow.from_json(json.loads(self.read_resource('test_flows/empty.json')))

        self.assertEqual(flow.base_language, 'eng')
        self.assertEqual(flow.entry, None)


class InputTest(BaseFlowsTest):
    def test_build_context(self):
        time = datetime.datetime(2015, 9, 30, 14, 31, 30, 0, pytz.UTC)
        _input = Input("Hello", time)

        container = EvaluationContext({}, pytz.timezone("Africa/Kigali"), DateStyle.DAY_FIRST)

        contact_context = self.contact.build_context(self.org)

        context = _input.build_context(container, contact_context)
        self.assertEqual(context, {'*': "Hello",
                                   'value': "Hello",
                                   'time': "30-09-2015 16:31",
                                   'contact': contact_context})

        _input = Input(Decimal("123.456"), time)

        context = _input.build_context(container, contact_context)
        self.assertEqual(context, {'*': "123.456",
                                   'value': "123.456",
                                   'time': "30-09-2015 16:31",
                                   'contact': contact_context})

        _input = Input(datetime.date(2015, 9, 21), time)

        context = _input.build_context(container, contact_context)
        self.assertEqual(context, {'*': "21-09-2015",
                                   'value': "21-09-2015",
                                   'time': "30-09-2015 16:31",
                                   'contact': contact_context})

        _input = Input(datetime.datetime(2015, 9, 21, 13, 30, 0, 0, pytz.UTC), time)

        context = _input.build_context(container, contact_context)
        self.assertEqual(context, {'*': "21-09-2015 15:30",
                                   'value': "21-09-2015 15:30",
                                   'time': "30-09-2015 16:31",
                                   'contact': contact_context})


class RunStateTest(BaseFlowsTest):

    def test_build_date_context(self):
        container = EvaluationContext({}, pytz.timezone("Africa/Kigali"), DateStyle.DAY_FIRST)
        now = datetime.datetime(2015, 8, 24, 9, 44, 5, 0, pytz.timezone("Africa/Kigali"))

        context = RunState.build_date_context(container, now)

        self.assertEqual(context, {'*': "24-08-2015 09:44",
                                   'now': "24-08-2015 09:44",
                                   'today': "24-08-2015",
                                   'tomorrow': "25-08-2015",
                                   'yesterday': "23-08-2015"})

        container = EvaluationContext({}, pytz.timezone("Africa/Kigali"), DateStyle.MONTH_FIRST)

        context = RunState.build_date_context(container, now)

        self.assertEqual(context, {'*': "08-24-2015 09:44",
                                   'now': "08-24-2015 09:44",
                                   'today': "08-24-2015",
                                   'tomorrow': "08-25-2015",
                                   'yesterday': "08-23-2015"})

    def test_to_and_from_json(self):
        # TODO
        pass
        # flow = Flow.fromJson(readResource("test_flows/mushrooms.json"));
        # runner = new RunnerBuilder().build();
        # run = runner.start(getOrg(), getContact(), flow);

        # send our first message through so we have references to rules
        # runner.resume(run, Input.of("Yes"));

        # export to json and reimport
        # String json = run.toJson();
        # RunState restored = RunState.fromJson(json, flow);

        # json should be the same
        # assertThat(restored.toJson(), is(json));


class TestsTest(BaseFlowsTest):
    def setUp(self):
        super(TestsTest, self).setUp()

        flow = Flow.from_json(json.loads(self.read_resource("test_flows/mushrooms.json")))

        self.deserialization_context = Flow.DeserializationContext(flow)

        self.runner = Runner()
        self.run = self.runner.start(self.org, self.contact, flow)
        self.context = self.run.build_context(None)

    def assertTest(self, test, input, expected_matched, expected_text, expected_value=None):
        if expected_value is None:
            expected_value = expected_text

        result = test.evaluate(self.runner, self.run, self.context, input)
        self.assertEqual(result.matched, expected_matched)
        self.assertEqual(result.text, expected_text)
        self.assertEqual(result.value, expected_value)

    def test_true_test(self):
        test = TrueTest()
        self.assertTest(test, "huh?", True, "huh?")

    def test_false_test(self):
        test = FalseTest()
        self.assertTest(test, "huh?", False, "huh?")

    def test_and_test(self):
        test = AndTest([ContainsTest(TranslatableText("upon")), StartsWithTest(TranslatableText("once"))])
        self.assertTest(test, "Once upon a time", True, "upon Once")
        self.assertTest(test, "Once a time", False, None)
        self.assertTest(test, "upon this rock I once", False, None)

    def test_or_test(self):
        test = OrTest([ContainsTest(TranslatableText("upon")), StartsWithTest(TranslatableText("once"))])
        self.assertTest(test, "Once upon a time", True, "upon")
        self.assertTest(test, "Once a time", True, "Once")
        self.assertTest(test, "upon this rock I once", True, "upon")
        self.assertTest(test, "huh", False, None)

    def test_not_empty_test(self):
        test = NotEmptyTest()
        self.assertTest(test, " ok  ", True, "ok")
        self.assertTest(test, "  ", False, None)
        self.assertTest(test, "", False, None)

    def test_contains_test(self):
        test = ContainsTest.from_json({"test": "Hello"}, self.deserialization_context)
        self.assertEqual(test.test, TranslatableText("Hello"))

        test = ContainsTest(TranslatableText("north,east"))

        self.assertTest(test, "go north east", True, "north east")
        self.assertTest(test, "EAST then NORRTH", True, "NORRTH EAST")

        self.assertTest(test, "go north", False, None)
        self.assertTest(test, "east", False, None)

    def test_contains_any_test(self):
        test = ContainsAnyTest(TranslatableText({'eng': "yes,affirmative", 'fre': "non"}))

        self.assertTest(test, "yes", True, "yes")
        self.assertTest(test, "AFFIRMATIVE SIR", True, "AFFIRMATIVE")
        self.assertTest(test, "affirmative yes", True, "yes affirmative")
        self.assertTest(test, "afirmative!", True, "afirmative")  # edit distance

        # edit distance doesn't apply for words shorter than 4 chars
        self.assertTest(test, "Ok YEES I will", False, None)

        self.assertTest(test, "no", False, None)
        self.assertTest(test, "NO way jose", False, None)

        test = ContainsAnyTest(TranslatableText("klab Kacyiru good"))
        self.assertTest(test, "kLab is awesome", True, "kLab")
        self.assertTest(test, "telecom is located at Kacyiru", True, "Kacyiru")
        self.assertTest(test, "good morning", True, "good")
        self.assertTest(test, "kLab is good", True, "kLab good")
        self.assertTest(test, "kigali city", False, None)

        # have the same behaviour when we have commas even a trailing one
        test = ContainsAnyTest(TranslatableText("klab, kacyiru, good, "))
        self.assertTest(test, "kLab is awesome", True, "kLab")
        self.assertTest(test, "telecom is located at Kacyiru", True, "Kacyiru")
        self.assertTest(test, "good morning", True, "good")
        self.assertTest(test, "kLab is good", True, "kLab good")
        self.assertTest(test, "kigali city", False, None)

    def test_starts_with_test(self):
        test = StartsWithTest(TranslatableText("once"))

        self.assertTest(test, "  ONCE", True, "ONCE")
        self.assertTest(test, "Once upon a time", True, "Once")

        self.assertTest(test, "Hey once", False, None)

    def test_regex_test(self):
        test = RegexTest(TranslatableText("(?P<first_name>\\w+) (\\w+)"))

        self.assertTest(test, "Isaac Newton", True, "Isaac Newton")
        self.assertTest(test, "Isaac", False, None)

        self.assertEqual(self.run.extra, {'0': "Isaac Newton", '1': "Isaac", '2': "Newton", 'first_name': "Isaac"})

    def test_numeric_test(self):
        self.assertEqual(NumericTest.extract_decimal("120"), (Decimal(120), "120"))
        self.assertEqual(NumericTest.extract_decimal("l2O"), (Decimal(120), "l2O"))
        self.assertEqual(NumericTest.extract_decimal("123C"), (Decimal(123), "123"))

        # text is NaN
        self.assertRaises(Exception, NumericTest.extract_decimal, "abc")

        # text has alpha subsitutions and suffix
        self.assertRaises(Exception, NumericTest.extract_decimal, "I23C")

    def test_has_number_test(self):
        test = HasNumberTest()

        self.assertTest(test, "32 cats", True, "32", Decimal(32))
        self.assertTest(test, "4l dogs", True, "4l", Decimal(41))
        self.assertTest(test, "cats", False, None)
        self.assertTest(test, "dogs", False, None)


class TranslatableTextTest(unittest.TestCase):

    def test_from_json(self):
        text = TranslatableText.from_json("test")
        self.assertEqual(text.value, "test")

        text = TranslatableText.from_json({'eng': "Hello", 'fra': "Bonjour"})
        self.assertEqual(text.value, {'eng': "Hello", 'fra': "Bonjour"})

    def test_get_localized_by_preferred(self):
        text = TranslatableText("Hello")
        self.assertEqual(text.get_localized_by_preferred([], "default"), "Hello")
        self.assertEqual(text.get_localized_by_preferred(['eng', 'fra'], "default"), "Hello")

        text = TranslatableText("")
        self.assertEqual(text.get_localized_by_preferred([], "default"), "default")
        self.assertEqual(text.get_localized_by_preferred(['eng', 'fra'], "default"), "default")

        text = TranslatableText({})
        self.assertEqual(text.get_localized_by_preferred([], "default"), "default")
        self.assertEqual(text.get_localized_by_preferred(['eng', 'fra'], "default"), "default")

        text = TranslatableText({'eng': "Hello", 'fra': "Bonjour"})
        self.assertEqual(text.get_localized_by_preferred([], "default"), "default")
        self.assertEqual(text.get_localized_by_preferred(['kin', 'run'], "default"), "default")
        self.assertEqual(text.get_localized_by_preferred(['eng', 'fra'], "default"), "Hello")
        self.assertEqual(text.get_localized_by_preferred(['fra', 'eng'], "default"), "Bonjour")

    def test_eq(self):
        self.assertEqual(TranslatableText("abc"), TranslatableText("abc"))
        self.assertNotEqual(TranslatableText("abc"), TranslatableText("cde"))

        self.assertEqual(TranslatableText({'eng': "Hello", 'fra': "Bonjour"}), TranslatableText({'eng': "Hello", 'fra': "Bonjour"}))
        self.assertNotEqual(TranslatableText({'eng': "Hello", 'fra': "Salut"}), TranslatableText({'eng': "Hello", 'fra': "Bonjour"}))


class UtilsTest(unittest.TestCase):
    def test_edit_distance(self):
        self.assertEqual(edit_distance("", ""), 0)
        self.assertEqual(edit_distance("abcd", "abcd"), 0)   # 0 differences
        self.assertEqual(edit_distance("abcd", "abc"), 1)    # 1 deletion
        self.assertEqual(edit_distance("abcd", "ad"), 2)     # 2 deletions
        self.assertEqual(edit_distance("abcd", "axbcd"), 1)  # 1 addition
        self.assertEqual(edit_distance("abcd", "acbd"), 1)   # 1 transposition
