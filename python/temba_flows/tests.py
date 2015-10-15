# coding=utf-8
from __future__ import absolute_import, unicode_literals

import codecs
import datetime
import json
import pytz
import unittest

from ordered_set import OrderedSet
from temba_expressions.dates import DateStyle
from temba_expressions.evaluator import EvaluationContext
from .definition import ContactRef, GroupRef, LabelRef, VariableRef
from .definition.flow import Flow, Action, ActionSet, RuleSet
from .definition.actions import ReplyAction, SendAction, EmailAction, SaveToContactAction, SetLanguageAction
from .definition.actions import AddToGroupsAction, RemoveFromGroupsAction, AddLabelsAction
from .definition.tests import *
from .exceptions import FlowRunException
from .runner import Contact, ContactUrn, Field, Input, Location, Org, Runner, RunState, Step, Value
from .utils import edit_distance, normalize_number


class BaseFlowsTest(unittest.TestCase):
    """
    Base class for all unit tests
    """
    __metaclass__ = ABCMeta

    def setUp(self):
        self.org = Org("RW", "eng", pytz.timezone("Africa/Kigali"), DateStyle.DAY_FIRST, False)

        self.fields = [
            Field("gender", "Gender", Field.ValueType.TEXT),
            Field("age", "Age", Field.ValueType.DECIMAL),
            Field("joined", "Joined", Field.ValueType.DATETIME)
        ]

        self.contact = Contact('1234-1234',
                               "Joe Flow",
                               [ContactUrn.from_string("tel:+260964153686"),
                                ContactUrn.from_string("twitter:realJoeFlow")],
                               OrderedSet(["Testers", "Developers"]),
                               {"gender": "M", "age": "34", "joined": "2015-10-06T11:30:01.123Z"},
                               'eng')

    @staticmethod
    def read_resource(path):
        with codecs.open('test_files/%s' % path, encoding='utf-8') as f:
            return f.read()

    def assertReply(self, action, msg):
        self.assertIsInstance(action, ReplyAction)
        self.assertEqual(action.msg, TranslatableText(msg))

    def assertAddToGroup(self, action, *group_names):
        self.assertIsInstance(action, AddToGroupsAction)

        names = [g.name for g in action.groups]
        self.assertEqual(names, list(group_names))

    class TestLocationResolver(Location.Resolver):
        """
        Location resolver for testing which has one state (Kigali) and one district (Gasabo)
        """
        def __init__(self):
            self.kigali = Location("S0001", "Kigali", Location.Level.STATE)
            self.gasabo = Location("D0001", "Gasabo", Location.Level.DISTRICT)

        def resolve(self, text, country, level, parent):
            if level == Location.Level.STATE and text.strip().lower() == "kigali":
                return self.kigali
            elif level == Location.Level.DISTRICT and text.strip().lower() == "gasabo" and parent == self.kigali:
                return self.gasabo
            else:
                return None


class ActionsTest(BaseFlowsTest):

    def setUp(self):
        super(ActionsTest, self).setUp()

        flow = Flow.from_json(json.loads(self.read_resource("test_flows/mushrooms.json")))

        self.deserialization_context = Flow.DeserializationContext(flow)

        self.runner = Runner(location_resolver=BaseFlowsTest.TestLocationResolver())
        self.run = self.runner.start(self.org, self.fields, self.contact, flow)
        self.context = self.run.build_context(self.runner, None)

    def test_reply_action(self):
        action = ReplyAction.from_json({"type": "reply", "msg": {"fre": "Bonjour"}}, self.deserialization_context)

        self.assertEqual(action.msg, TranslatableText({"fre": "Bonjour"}))

        action = ReplyAction(TranslatableText("Hi @contact.first_name you said @step.value"))

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.errors, [])

        performed = result.performed
        self.assertEqual(performed.msg, TranslatableText("Hi Joe you said Yes"))

        # still send if message has errors
        action = ReplyAction(TranslatableText("@(badexpression)"))

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.errors, ["Undefined variable: badexpression"])

        performed = result.performed
        self.assertEqual(performed.msg, TranslatableText("@(badexpression)"))

    def test_send_action(self):
        action = SendAction.from_json({
            "type": "send",
            "msg": {"fre": "Bonjour"},
            "contacts": [{"id": 234, "name": "Mr Test"}],
            "groups": [{"id": 123, "name": "Testers"}],
            "variables": [{"id": "@new_contact"}, {"id": "group-@contact.gender"}]
        }, self.deserialization_context)

        self.assertEqual(action.msg, TranslatableText({"fre": "Bonjour"}))
        self.assertEqual(action.contacts[0].id, 234)
        self.assertEqual(action.contacts[0].name, "Mr Test")
        self.assertEqual(action.groups[0].id, 123)
        self.assertEqual(action.groups[0].name, "Testers")
        self.assertEqual(action.variables[0].value, "@new_contact")
        self.assertEqual(action.variables[1].value, "group-@contact.gender")

        action = SendAction(TranslatableText("Hi @(\"Dr\" & contact) @contact.first_name. @step.contact said @step.value"),
                            [ContactRef(234, "Mr Test")],
                            [GroupRef(123, "Testers")],
                            [VariableRef("@new_contact"), VariableRef("group-@contact.gender")])

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.errors, [])

        performed = result.performed
        self.assertEqual(performed.msg, TranslatableText("Hi @(\"Dr\"&contact) @contact.first_name. Joe Flow said Yes"))
        self.assertEqual(len(performed.contacts), 1)
        self.assertEqual(performed.contacts[0].id, 234)
        self.assertEqual(performed.contacts[0].name, "Mr Test")
        self.assertEqual(len(performed.groups), 1)
        self.assertEqual(performed.groups[0].id, 123)
        self.assertEqual(performed.groups[0].name, "Testers")
        self.assertEqual(len(performed.variables), 2)
        self.assertEqual(performed.variables[0].value, "@new_contact")
        self.assertEqual(performed.variables[1].value, "group-M")

    def test_email_action(self):
        action = EmailAction.from_json({"type": "email",
                                        "emails": ["code@nyaruka.com", "@contact.chw_email"],
                                        "subject": "Salut",
                                        "msg": "Ça va?"}, self.deserialization_context)

        self.assertEqual(action.addresses, ["code@nyaruka.com", "@contact.chw_email"])
        self.assertEqual(action.subject, "Salut")
        self.assertEqual(action.msg, "Ça va?")

        action = EmailAction(["rowan@nyaruka.com", "@(LOWER(contact.gender))@chws.org"],
                             "Update from @contact",
                             "This is to notify you that @contact did something")

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.errors, [])

        performed = result.performed
        self.assertEqual(performed.addresses, ["rowan@nyaruka.com", "m@chws.org"])
        self.assertEqual(performed.subject, "Update from Joe Flow")
        self.assertEqual(performed.msg, "This is to notify you that Joe Flow did something")

    def test_set_language_action(self):
        action = SetLanguageAction.from_json({"type": "lang", "lang": "fre", "name": "Français"}, self.deserialization_context)

        self.assertEqual(action.lang, "fre")
        self.assertEqual(action.name, "Français")

        action = SetLanguageAction("fre", "Français")

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.errors, [])

        performed = result.performed
        self.assertEqual(performed.lang, "fre")
        self.assertEqual(performed.name, "Français")

        self.assertEqual(self.run.contact.language, "fre")

    def test_save_to_contact_action(self):
        action = SaveToContactAction.from_json({"type": "save", "field": "age", "label": "Age", "value": "@extra.age"},
                                               self.deserialization_context)

        self.assertEqual(action.field, "age")
        self.assertEqual(action.label, "Age")
        self.assertEqual(action.value, "@extra.age")

        # update existing field
        action = SaveToContactAction("age", "Age", "@extra.age")
        self.run.extra["age"] = "64"

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.errors, [])

        performed = result.performed
        self.assertEqual(performed.field, "age")
        self.assertEqual(performed.label, "Age")
        self.assertEqual(performed.value, "64")

        self.assertEqual(self.run.contact.fields["age"], "64")

        # update new field (no key provided)
        action = SaveToContactAction(None, "Is OK", "Yes")

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.errors, [])

        performed = result.performed
        self.assertEqual(performed.field, "is_ok")
        self.assertEqual(performed.label, "Is OK")
        self.assertEqual(performed.value, "Yes")

        self.assertEqual(self.run.contact.fields["is_ok"], "Yes")

        # NOOP for invalid expression
        action = SaveToContactAction("age", "Age", "@(badexpression)")
        result = action.execute(self.runner, self.run, Input("Yes"))

        self.assertEqual(result.performed, None)
        self.assertEqual(result.errors, ['Undefined variable: badexpression'])

        self.assertEqual(self.run.contact.fields["age"], "64")

        # try one that updates the phone number
        action = SaveToContactAction("tel_e164", "Phone Number", "@step.value")
        action.execute(self.runner, self.run, Input("0788382382"))
        self.assertEqual(len(self.run.contact.urns), 3)
        self.assertEqual(self.run.contact.urns[2], ContactUrn(ContactUrn.Scheme.TEL, "+250788382382"))

    def test_add_to_groups_action(self):
        action = AddToGroupsAction([GroupRef(123, "Testers"), GroupRef(None, "People who say @step.value")])

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.errors, [])

        performed = result.performed
        self.assertEqual(performed.groups, [GroupRef(123, "Testers"), GroupRef(None, "People who say Yes")])

        # don't add to group name which is an invalid expression
        action = AddToGroupsAction([GroupRef(None, "@(badexpression)")])

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.performed, None)
        self.assertEqual(result.errors, ["Undefined variable: badexpression"])

    def test_remove_from_groups_action(self):
        action = RemoveFromGroupsAction([GroupRef(123, "Testers"), GroupRef(None, "People who say @step.value")])

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.errors, [])

        performed = result.performed
        self.assertEqual(performed.groups, [GroupRef(123, "Testers"), GroupRef(None, "People who say Yes")])

    def test_add_labels_action(self):
        action = AddLabelsAction([LabelRef(123, "Testing"), LabelRef(None, "Messages with @step.value")])

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.errors, [])

        performed = result.performed
        self.assertEqual(performed.labels, [LabelRef(123, "Testing"), LabelRef(None, "Messages with Yes")])

        # don't add label which is an invalid expression
        action = AddLabelsAction([LabelRef(None, "@(badexpression)")])

        result = action.execute(self.runner, self.run, Input("Yes"))
        self.assertEqual(result.performed, None)
        self.assertEqual(result.errors, ["Undefined variable: badexpression"])


class ContactTest(BaseFlowsTest):
    def setUp(self):
        super(ContactTest, self).setUp()

        flow = Flow.from_json(json.loads(self.read_resource("test_flows/mushrooms.json")))
        self.runner = Runner(location_resolver=BaseFlowsTest.TestLocationResolver())
        self.run = self.runner.start(self.org, self.fields, self.contact, flow)
        self.context = self.run.build_context(self.runner, None)

    def test_to_and_from_json(self):
        json_obj = self.contact.to_json()

        self.assertEqual(json_obj, {'uuid': "1234-1234",
                                    'name': "Joe Flow",
                                    'urns': ["tel:+260964153686", "twitter:realJoeFlow"],
                                    'groups': ["Testers", "Developers"],
                                    'fields': {"age": "34", "gender": "M", "joined": "2015-10-06T11:30:01.123Z"},
                                    'language': 'eng'})

        contact = Contact.from_json(json_obj)

        self.assertEqual(contact.uuid, "1234-1234")
        self.assertEqual(contact.name, "Joe Flow")
        self.assertEqual(contact.urns, [ContactUrn(ContactUrn.Scheme.TEL, "+260964153686"),
                                        ContactUrn(ContactUrn.Scheme.TWITTER, "realJoeFlow")])
        self.assertEqual(contact.groups, {"Testers", "Developers"})
        self.assertEqual(contact.fields, {"age": "34", "gender": "M", "joined": "2015-10-06T11:30:01.123Z"})
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
        context = self.contact.build_context(self.run, self.context)
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
                                   'age': "34",
                                   'joined': "06-10-2015 13:30"})
        self.org.is_anon = True
        self.context.date_style = DateStyle.MONTH_FIRST
        context = self.contact.build_context(self.run, self.context)
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
                                   'age': "34",
                                   'joined': "10-06-2015 13:30"})


class ContactUrnTest(BaseFlowsTest):

    def test_from_string(self):
        urn = ContactUrn.from_string("tel:+260964153686")

        self.assertEqual(urn.scheme, ContactUrn.Scheme.TEL)
        self.assertEqual(urn.path, "+260964153686")

    def test_normalized(self):
        raw = ContactUrn(ContactUrn.Scheme.TEL, " 078-383-5665 ")
        self.assertEqual(raw.normalized(self.org), ContactUrn(ContactUrn.Scheme.TEL, "+250783835665"))

        raw = ContactUrn(ContactUrn.Scheme.TWITTER, "  @bob ")
        self.assertEqual(raw.normalized(self.org), ContactUrn(ContactUrn.Scheme.TWITTER, "bob"))


class FieldTest(BaseFlowsTest):

    def test_to_and_from_json(self):
        json_obj = self.fields[0].to_json()

        self.assertEqual(json_obj, {"key": "gender",
                                    "label": "Gender",
                                    "value_type": "T"})

        field = Field.from_json(json_obj)

        self.assertEqual(field.key, "gender")
        self.assertEqual(field.label, "Gender")
        self.assertEqual(field.value_type, Field.ValueType.TEXT)

    def test_make_key(self):
        self.assertEquals(Field.make_key("First Name"), "first_name")
        self.assertEquals(Field.make_key("Second   Name  "), "second_name")
        self.assertEquals(Field.make_key("  ^%$# %$$ $##323 ffsn slfs ksflskfs!!!! fk$%%%$$$anfaDDGAS ))))))))) "), "323_ffsn_slfs_ksflskfs_fk_anfaddgas")

    def test_is_valid_key(self):
        self.assertTrue(Field.is_valid_key("age"))
        self.assertTrue(Field.is_valid_key("age_now_2"))
        self.assertFalse(Field.is_valid_key("Age"))   # must be lowercase
        self.assertFalse(Field.is_valid_key("age!"))  # can't have punctuation
        self.assertFalse(Field.is_valid_key("âge"))   # a-z only
        self.assertFalse(Field.is_valid_key("2up"))   # can't start with a number
        self.assertFalse(Field.is_valid_key("name"))  # can't be a reserved name
        self.assertFalse(Field.is_valid_key("uuid"))

    def test_is_valid_label(self):
        self.assertTrue(Field.is_valid_label("Age"))
        self.assertTrue(Field.is_valid_label("Age Now 2"))
        self.assertFalse(Field.is_valid_label("Age_Now"))  # can't have punctuation
        self.assertFalse(Field.is_valid_label("âge"))      # a-z only


class FlowTest(BaseFlowsTest):

    def test_from_json(self):
        flow = Flow.from_json(json.loads(self.read_resource('test_flows/mushrooms.json')))

        self.assertEqual(flow.flow_type, Flow.Type.FLOW)
        self.assertEqual(flow.base_language, 'eng')
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

        rs1_rule1 = rs1.rules[0]

        self.assertIsInstance(rs1_rule1.test, ContainsAnyTest)
        self.assertEqual(rs1_rule1.category, TranslatableText({"base": "Yes", "eng": "Yes", "fre": "Oui"}))

        as2 = rs1_rule1.destination

        self.assertEqual(as2.uuid, '6d12cde9-dbbf-4673-acd7-afa1776d382b')
        self.assertEqual(len(as2.actions), 2)
        self.assertIsInstance(as2.actions[0], ReplyAction)
        self.assertIsInstance(as2.actions[1], RemoveFromGroupsAction)
        self.assertEqual(as2.destination.uuid, '6891e592-1e29-426b-b227-e3ae466662ab')

        rs1_rule2 = rs1.rules[1]

        self.assertIsInstance(rs1_rule2.test, ContainsAnyTest)
        self.assertEqual(rs1_rule2.category, TranslatableText({"base": "No", "eng": "No", "fre": "Non"}))

        as3 = rs1_rule2.destination

        self.assertEqual(as3.uuid, '4ef2b232-1484-4db7-b470-98af1a2349d3')
        self.assertEqual(len(as3.actions), 2)
        self.assertIsInstance(as3.actions[0], ReplyAction)
        self.assertIsInstance(as3.actions[1], AddToGroupsAction)
        self.assertEqual(as3.destination.uuid, '6891e592-1e29-426b-b227-e3ae466662ab')

        rs1_rule3 = rs1.rules[2]

        self.assertIsInstance(rs1_rule3.test, TrueTest)
        self.assertEqual(rs1_rule3.category, TranslatableText({"base": "Other", "eng": "Other", "fre": "Autre"}))

        as4 = rs1_rule3.destination

        self.assertEqual(as4.uuid, 'e277932e-d546-4e0c-a483-ce6cce06b929')
        self.assertEqual(len(as4.actions), 1)
        self.assertIsInstance(as4.actions[0], ReplyAction)
        self.assertEqual(as4.destination, rs1)

    def test_from_json_with_empty_flow(self):
        flow = Flow.from_json(json.loads(self.read_resource('test_flows/empty.json')))

        self.assertEqual(flow.flow_type, Flow.Type.FLOW)
        self.assertEqual(flow.base_language, 'eng')
        self.assertEqual(flow.entry, None)

    def test_from_json_with_missing_spec_version(self):
        self.assertRaises(FlowParseException, Flow.from_json,
                          json.loads(self.read_resource('test_flows/missing-version.json')))

    def test_from_json_with_unsupported_spec_version(self):
        self.assertRaises(FlowParseException, Flow.from_json,
                          json.loads(self.read_resource('test_flows/unsupported-version.json')))


class InteractionTest(BaseFlowsTest):
    """
    Flow interaction tests loaded from JSON
    """
    def test_interaction_tests(self):
        self._run_interaction_tests('test_flows/mushrooms.json', 'test_runs/mushrooms.runs.json')
        self._run_interaction_tests('test_flows/registration.json', 'test_runs/registration.runs.json')
        self._run_interaction_tests('test_flows/birthdate-check.json', 'test_runs/birthdate-check.runs.json')
        self._run_interaction_tests('test_flows/basic-form.json', 'test_runs/basic-form.runs.json')

    def _run_interaction_tests(self, flow_file, interactions_file):
        print "Running interaction tests from %s" % interactions_file

        flow = Flow.from_json(json.loads(self.read_resource(flow_file)))

        interactions_json = json.loads(self.read_resource(interactions_file))
        tests = [InteractionTest.TestDefinition.from_json(t) for t in interactions_json]
        runner = Runner(location_resolver=InteractionTest.TestLocationResolver(),
                        now=datetime.datetime(2015, 10, 15, 7, 48, 30, 123456, pytz.UTC))

        for test in tests:
            self._run_interaction_test(runner, flow, test)

    def _run_interaction_test(self, runner, flow, test):
        run = None
        while True:
            if not run:
                run = runner.start(test.org, test.fields_initial, test.contact_initial, flow)
            else:
                message = test.messages.pop(0)
                self.assertEqual("input", message.type)

                # print " > Resuming run with input: %s" % message.msg

                runner.resume(run, Input(message.msg))

            for step in run.get_completed_steps():
                for action in step.actions:
                    if isinstance(action, ReplyAction):
                        msg = action.msg.get_localized(run)

                        # print " > Got reply: %s" % msg

                        if len(test.messages) > 0:
                            message = test.messages.pop(0)
                            self.assertEqual("reply", message.type)
                            self.assertEqual(msg, message.msg)
                        else:
                            self.fail("Got un-expected additional reply: \"%s\"" % msg)

            if len(test.messages) == 0:
                break

        self.assertEqual(len(test.messages), 0)

        self.assertEqual(set(run.get_created_fields()), set(test.fields_created))

        self.assertEqual(run.contact.name, test.contact_final.name)
        self.assertEqual(run.contact.groups, test.contact_final.groups)
        self.assertEqual(run.contact.fields, test.contact_final.fields)
        self.assertEqual(run.contact.language, test.contact_final.language)

    class TestDefinition(object):
        def __init__(self, org, fields_initial, contact_initial, messages, fields_created, contact_final):
            self.org = org
            self.fields_initial = fields_initial
            self.contact_initial = contact_initial
            self.messages = messages
            self.fields_created = fields_created
            self.contact_final = contact_final

        @classmethod
        def from_json(cls, json_obj):
            return cls(Org.from_json(json_obj['org']),
                       [Field.from_json(f) for f in json_obj['fields_initial']],
                       Contact.from_json(json_obj['contact_initial']),
                       [InteractionTest.TestDefinition.Message.from_json(m) for m in json_obj['messages']],
                       [Field.from_json(f) for f in json_obj['fields_created']],
                       Contact.from_json(json_obj['contact_final']))

        class Message(object):
            def __init__(self, _type, msg):
                self.type = _type
                self.msg = msg

            @classmethod
            def from_json(cls, json_object):
                return cls(json_object['type'], json_object['msg'])

    class TestLocationResolver(Location.Resolver):
        def resolve(self, input, country, level, parent):
            # for testing, accept any location that doesn't begin with the letter X
            if not input.strip().lower().startswith("x"):
                return Location("S0001", input, Location.Level.STATE)
            else:
                return None


class InputTest(BaseFlowsTest):
    def setUp(self):
        super(InputTest, self).setUp()

        flow = Flow.from_json(json.loads(self.read_resource("test_flows/mushrooms.json")))
        self.runner = Runner(location_resolver=BaseFlowsTest.TestLocationResolver())
        self.run = self.runner.start(self.org, self.fields, self.contact, flow)
        self.context = self.run.build_context(self.runner, None)

    def test_build_context(self):
        time = datetime.datetime(2015, 9, 30, 14, 31, 30, 0, pytz.UTC)
        _input = Input("Hello", time)

        container = EvaluationContext({}, pytz.timezone("Africa/Kigali"), DateStyle.DAY_FIRST)

        contact_context = self.contact.build_context(self.run, self.context)

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


class OrgTest(BaseFlowsTest):

    def test_to_and_from_json(self):
        json_obj = self.org.to_json()

        self.assertEqual(json_obj, {"country": "RW",
                                    "primary_language": "eng",
                                    "timezone": "Africa/Kigali",
                                    "date_style": "day_first",
                                    "anon": False})

        org = Org.from_json(json_obj)

        self.assertEqual(org.country, "RW")
        self.assertEqual(org.primary_language, "eng")
        self.assertEqual(org.timezone, pytz.timezone("Africa/Kigali"))
        self.assertEqual(org.date_style, DateStyle.DAY_FIRST)
        self.assertEqual(org.is_anon, False)


class RunnerTest(BaseFlowsTest):

    def setUp(self):
        super(RunnerTest, self).setUp()

        self.runner = Runner()

    def test_start_and_resume_mushrooms(self):
        flow = Flow.from_json(json.loads(self.read_resource("test_flows/mushrooms.json")))

        run = self.runner.start(self.org, self.fields, self.contact, flow)

        self.assertEqual(run.org.primary_language, "eng")
        self.assertEqual(run.org.timezone, pytz.timezone("Africa/Kigali"))
        self.assertEqual(run.org.date_style, DateStyle.DAY_FIRST)
        self.assertFalse(run.org.is_anon)

        self.assertEqual(run.contact.uuid, "1234-1234")
        self.assertEqual(run.contact.name, "Joe Flow")
        self.assertEqual(run.contact.urns, [ContactUrn(ContactUrn.Scheme.TEL, "+260964153686"), ContactUrn(ContactUrn.Scheme.TWITTER, "realJoeFlow")])
        self.assertEqual(run.contact.groups, {"Testers", "Developers"})
        self.assertEqual(len(run.contact.fields), 3)
        self.assertEqual(run.contact.language, "eng")

        self.assertEqual(len(run.steps), 2)
        self.assertEqual(run.steps[0].node.uuid, "32cf414b-35e3-4c75-8a78-d5f4de925e13")
        self.assertIsNotNone(run.steps[0].arrived_on)
        self.assertIsNotNone(run.steps[0].left_on)
        self.assertEqual(len(run.steps[0].actions), 1)
        self.assertReply(run.steps[0].actions[0], "Hi Joe. Do you like mushrooms?")
        self.assertEqual(run.steps[1].node.uuid, "1e318293-4730-481c-b455-daaaf86b2e6c")
        self.assertIsNotNone(run.steps[1].arrived_on)
        self.assertIsNone(run.steps[1].left_on)
        self.assertIsNone(run.steps[1].rule_result)
        self.assertEqual(len(run.get_completed_steps()), 1)

        self.assertEqual(len(run.values), 0)

        self.assertEqual(run.state, RunState.State.WAIT_MESSAGE)

        last_step_left_on = run.steps[1].arrived_on

        self.runner.resume(run, Input("YUCK!"))

        self.assertEqual(run.contact.groups, {"Testers", "Developers"}) # unchanged

        self.assertEqual(len(run.steps), 3)
        self.assertEqual(run.steps[0].node.uuid, "1e318293-4730-481c-b455-daaaf86b2e6c")
        self.assertEqual(run.steps[0].arrived_on, last_step_left_on)
        self.assertIsNotNone(run.steps[0].left_on)
        self.assertEqual(run.steps[0].rule_result.rule.uuid, "366fb919-7e0b-48be-8f5b-baa14b2a65aa")
        self.assertEqual(run.steps[0].rule_result.category, "Other")
        self.assertEqual(run.steps[0].rule_result.value, "YUCK!")
        self.assertEqual(run.steps[1].node.uuid, "e277932e-d546-4e0c-a483-ce6cce06b929")
        self.assertIsNotNone(run.steps[1].arrived_on)
        self.assertIsNotNone(run.steps[1].left_on)
        self.assertIsNone(run.steps[1].rule_result)
        self.assertEqual(len(run.steps[1].actions), 1)
        self.assertReply(run.steps[1].actions[0], "We didn't understand your answer. Please reply with yes/no.")
        self.assertEqual(run.steps[2].node.uuid, "1e318293-4730-481c-b455-daaaf86b2e6c")
        self.assertIsNotNone(run.steps[2].arrived_on)
        self.assertIsNone(run.steps[2].left_on)
        self.assertEqual(len(run.get_completed_steps()), 2)

        self.assertEqual(len(run.values), 1)
        self.assertEqual(run.values["response_1"].value, "YUCK!")
        self.assertEqual(run.values["response_1"].category, "Other")
        self.assertEqual(run.values["response_1"].text, "YUCK!")
        self.assertIsNotNone(run.values["response_1"].time)

        self.assertEqual(run.state, RunState.State.WAIT_MESSAGE)

        last_step_left_on = run.steps[2].arrived_on

        self.runner.resume(run, Input("no way"))

        self.assertEqual(run.contact.groups, {"Testers", "Developers", "Approved"})  # added to group

        self.assertEqual(len(run.steps), 3)
        self.assertEqual(run.steps[0].node.uuid, "1e318293-4730-481c-b455-daaaf86b2e6c")
        self.assertEqual(run.steps[0].arrived_on, last_step_left_on)
        self.assertIsNotNone(run.steps[0].left_on)
        self.assertEqual(run.steps[0].rule_result.rule.uuid, "d638e042-3f5c-4f03-a6c1-2031bd8971b2")
        self.assertEqual(run.steps[0].rule_result.category, "No")
        self.assertEqual(run.steps[0].rule_result.value, "no")
        self.assertEqual(run.steps[1].node.uuid, "4ef2b232-1484-4db7-b470-98af1a2349d3")
        self.assertIsNotNone(run.steps[1].arrived_on)
        self.assertIsNotNone(run.steps[1].left_on)
        self.assertIsNone(run.steps[1].rule_result)
        self.assertEqual(len(run.steps[1].actions), 2)
        self.assertReply(run.steps[1].actions[0], "That was the right answer.")
        self.assertAddToGroup(run.steps[1].actions[1], "Approved")
        self.assertEqual(run.steps[2].node.uuid, "6891e592-1e29-426b-b227-e3ae466662ab")
        self.assertIsNotNone(run.steps[2].arrived_on)
        self.assertIsNone(run.steps[2].left_on)
        self.assertIsNone(run.steps[2].rule_result)
        self.assertEqual(len(run.steps[2].actions), 1)
        self.assertEqual(len(run.get_completed_steps()), 3)

        self.assertEqual(len(run.values), 1)
        self.assertEqual(run.values["response_1"].value, "no")
        self.assertEqual(run.values["response_1"].category, "No")
        self.assertEqual(run.values["response_1"].text, "no way")
        self.assertIsNotNone(run.values["response_1"].time)

        self.assertEqual(run.state, RunState.State.COMPLETED)

    def test_start_and_resume_mushrooms_in_french(self):
        flow = Flow.from_json(json.loads(self.read_resource("test_flows/mushrooms.json")))

        jean = Contact("1234-1234", "Jean D'Amour", [ContactUrn.from_string("tel:+260964153686")], OrderedSet(), {}, "fre")

        run = self.runner.start(self.org, self.fields, jean, flow)

        self.assertEqual(run.contact.language, "fre")
        self.assertEqual(len(run.steps), 2)
        self.assertReply(run.steps[0].actions[0], "Salut Jean. Aimez-vous les champignons?")
        self.assertEqual(run.state, RunState.State.WAIT_MESSAGE)

        self.runner.resume(run, Input("EUGH!"))

        self.assertEqual(run.steps[0].rule_result.category, "Other")
        self.assertEqual(run.steps[0].rule_result.value, "EUGH!")
        self.assertReply(run.steps[1].actions[0], "Nous ne comprenions pas votre réponse. S'il vous plaît répondre par oui/non.")

        self.assertEqual(run.values["response_1"].value, "EUGH!")
        self.assertEqual(run.values["response_1"].category, "Other")
        self.assertEqual(run.values["response_1"].text, "EUGH!")

        self.assertEqual(run.state, RunState.State.WAIT_MESSAGE)

        self.runner.resume(run, Input("non!!"))

        self.assertEqual(run.contact.groups, ["Approved"])  # added to group

        self.assertEqual(run.steps[0].rule_result.category, "No")
        self.assertEqual(run.steps[0].rule_result.value, "non")
        self.assertReply(run.steps[1].actions[0], "Ce fut la bonne réponse.")

        self.assertEqual(run.values["response_1"].value, "non")
        self.assertEqual(run.values["response_1"].category, "No")
        self.assertEqual(run.values["response_1"].text, "non!!")

        self.assertEqual(run.state, RunState.State.COMPLETED)

    def test_start_and_resume_greatwall(self):
        flow = Flow.from_json(json.loads(self.read_resource("test_flows/greatwall.json")))

        run = self.runner.start(self.org, self.fields, self.contact, flow)

        self.assertEqual(run.steps[0].node.uuid, "8dbb7e1a-43d6-4c5b-a99d-fe3ee8923b65")
        self.assertEqual(len(run.steps[0].actions), 1)
        self.assertReply(run.steps[0].actions[0], "How many people are you?")
        self.assertEqual(run.steps[1].node.uuid, "b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42")

        self.assertEqual(run.state, RunState.State.WAIT_MESSAGE)

        self.runner.resume(run, Input("9"))

        self.assertEqual(run.steps[0].node.uuid, "b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42")
        self.assertEqual(run.steps[0].rule_result.category, "Other")
        self.assertEqual(run.steps[0].rule_result.value, "9")
        self.assertEqual(run.steps[1].node.uuid, "c81af400-a744-499a-9ad5-c90e233e4b92")
        self.assertReply(run.steps[1].actions[0], "Please choose a number between 1 and 8")
        self.assertEqual(run.steps[2].node.uuid, "b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42")

        self.assertEqual(run.values["people"].value, "9")
        self.assertEqual(run.values["people"].category, "Other")
        self.assertEqual(run.values["people"].text, "9")

        self.runner.resume(run, Input("7"))

        self.assertEqual(run.steps[0].node.uuid, "b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42")
        self.assertEqual(run.steps[0].rule_result.category, "1 - 8")
        self.assertEqual(run.steps[0].rule_result.value, "7")
        self.assertEqual(run.steps[1].node.uuid, "fe5ec555-ed5b-4b29-934d-c593f52c5881")
        self.assertEqual(run.steps[1].rule_result.category, "> 2")
        self.assertEqual(run.steps[1].rule_result.value, "7")

        self.assertEqual(run.values["people"].value, "7")
        self.assertEqual(run.values["people"].category, "1 - 8")
        self.assertEqual(run.values["people"].text, "7")
        self.assertEqual(run.values["enough_for_soup"].value, "7")
        self.assertEqual(run.values["enough_for_soup"].category, "> 2")
        self.assertEqual(run.values["enough_for_soup"].text, "7")

    def test_start_with_empty_flow(self):
        flow = Flow.from_json(json.loads(self.read_resource("test_flows/empty.json")))
        self.assertRaises(FlowRunException, self.runner.start, self.org, self.fields, self.contact, flow)

    def test_update_contact_field(self):
        self.fields.append(Field("district", "District", Field.ValueType.DISTRICT))

        runner = Runner(location_resolver=BaseFlowsTest.TestLocationResolver())

        flow = Flow.from_json(json.loads(self.read_resource("test_flows/mushrooms.json")))
        run = runner.start(self.org, self.fields, self.contact, flow)

        runner.update_contact_field(run, "district", "Gasabo")

        # can't set a district field value without a state field value
        self.assertEqual(run.contact.fields["district"], None)

        run.get_or_create_field("state", "State", Field.ValueType.STATE)

        runner.update_contact_field(run, "state", "kigali")
        runner.update_contact_field(run, "district", "gasabo")

        self.assertEqual(run.contact.fields["district"], "Gasabo")


class RunStateTest(BaseFlowsTest):

    def test_build_date_context(self):
        now = datetime.datetime(2015, 8, 24, 9, 44, 5, 0, pytz.timezone("Africa/Kigali"))
        container = EvaluationContext({}, pytz.timezone("Africa/Kigali"), DateStyle.DAY_FIRST, now)

        context = RunState.build_date_context(container)

        self.assertEqual(context, {'*': "24-08-2015 09:44",
                                   'now': "24-08-2015 09:44",
                                   'today': "24-08-2015",
                                   'tomorrow': "25-08-2015",
                                   'yesterday': "23-08-2015"})

        container = EvaluationContext({}, pytz.timezone("Africa/Kigali"), DateStyle.MONTH_FIRST, now)

        context = RunState.build_date_context(container)

        self.assertEqual(context, {'*': "08-24-2015 09:44",
                                   'now': "08-24-2015 09:44",
                                   'today': "08-24-2015",
                                   'tomorrow': "08-25-2015",
                                   'yesterday': "08-23-2015"})

    def test_to_and_from_json(self):
        flow = Flow.from_json(json.loads(self.read_resource("test_flows/mushrooms.json")))
        runner = Runner()
        run = runner.start(self.org, self.fields, self.contact, flow)

        # send our first message through so we have references to rules
        runner.resume(run, Input("Yes"))

        # export to json and re-import
        json_str = json.dumps(run.to_json())
        restored = RunState.from_json(json.loads(json_str), flow)

        # json should be the same
        self.assertEqual(json.dumps(restored.to_json()), json_str)


class StepTest(BaseFlowsTest):

    def test_to_and_from_json(self):
        flow = Flow.from_json(json.loads(self.read_resource("test_flows/mushrooms.json")))
        deserialization_context = Flow.DeserializationContext(flow)
        arrived_on = datetime.datetime(2015, 8, 25, 11, 59, 30, 88000, pytz.UTC)

        step = Step(flow.entry, arrived_on)

        json_obj = step.to_json()

        self.assertEqual(json_obj, {'node': "32cf414b-35e3-4c75-8a78-d5f4de925e13",
                                    'arrived_on': "2015-08-25T11:59:30.088Z",
                                    'left_on': None,
                                    'rule': None,
                                    'actions': [],
                                    'errors': []})

        step.add_action_result(Action.Result.performed(ReplyAction(TranslatableText("Hi Joe"))))
        json_obj = step.to_json()

        self.assertEqual(json_obj, {'node': "32cf414b-35e3-4c75-8a78-d5f4de925e13",
                                    'arrived_on': "2015-08-25T11:59:30.088Z",
                                    'left_on': None,
                                    'rule': None,
                                    'actions': [{'type': "reply", 'msg': "Hi Joe"}],
                                    'errors': []})

        step.add_action_result(Action.Result.performed(None, ["This is an error", "This too"]))
        json_obj = step.to_json()

        self.assertEqual(json_obj, {'node': "32cf414b-35e3-4c75-8a78-d5f4de925e13",
                                    'arrived_on': "2015-08-25T11:59:30.088Z",
                                    'left_on': None,
                                    'rule': None,
                                    'actions': [{'type': "reply", 'msg': "Hi Joe"}],
                                    'errors': ["This is an error", "This too"]})

        step = Step.from_json(json_obj, deserialization_context)

        self.assertEqual(step.node, flow.entry)
        self.assertEqual(step.arrived_on, arrived_on)
        self.assertEqual(step.left_on, None)
        self.assertEqual(step.rule_result, None)
        self.assertReply(step.actions[0], "Hi Joe")
        self.assertEqual(step.errors, ["This is an error", "This too"])

        step.actions = []
        step.errors = []

        yes_rule = flow.entry.destination.rules[0]

        step.rule_result = RuleSet.Result(yes_rule, "yes", "Yes", "yes ok")
        json_obj = step.to_json()

        self.assertEqual(json_obj, {'node': "32cf414b-35e3-4c75-8a78-d5f4de925e13",
                                    'arrived_on': "2015-08-25T11:59:30.088Z",
                                    'left_on': None,
                                    'rule': {'uuid': "a53e3607-ac87-4bee-ab95-30fd4ad8a837", "value": "yes", "category": "Yes", "text": "yes ok"},
                                    'actions': [],
                                    'errors': []})

        step = Step.from_json(json_obj, deserialization_context)

        self.assertEqual(step.node, flow.entry)
        self.assertEqual(step.arrived_on, arrived_on)
        self.assertEqual(step.left_on, None)
        self.assertEqual(step.rule_result.rule, yes_rule)
        self.assertEqual(step.rule_result.value, "yes")
        self.assertEqual(step.rule_result.category, "Yes")
        self.assertEqual(step.actions, [])
        self.assertEqual(step.errors, [])


class TestsTest(BaseFlowsTest):

    def setUp(self):
        super(TestsTest, self).setUp()

        flow = Flow.from_json(json.loads(self.read_resource("test_flows/mushrooms.json")))

        self.deserialization_context = Flow.DeserializationContext(flow)

        self.runner = Runner(location_resolver=BaseFlowsTest.TestLocationResolver())
        self.run = self.runner.start(self.org, self.fields, self.contact, flow)
        self.context = self.run.build_context(self.runner, None)

    def assertTest(self, test, input, expected_matched, expected_value):
        result = test.evaluate(self.runner, self.run, self.context, input)
        self.assertEqual(result.matched, expected_matched)
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
        self.assertEqual(NumericTest.extract_decimal("120"), Decimal(120))
        self.assertEqual(NumericTest.extract_decimal("l2O"), Decimal(120))
        self.assertEqual(NumericTest.extract_decimal("123C"), Decimal(123))

        # text is NaN
        self.assertRaises(Exception, NumericTest.extract_decimal, "abc")

        # text has alpha substitutions and suffix
        self.assertRaises(Exception, NumericTest.extract_decimal, "I23C")

    def test_has_number_test(self):
        test = HasNumberTest()

        self.assertTest(test, "32 cats", True, Decimal(32))
        self.assertTest(test, "4l dogs", True, Decimal(41))
        self.assertTest(test, "cats", False, None)
        self.assertTest(test, "dogs", False, None)

    def test_equal_test(self):
        test = EqualTest("32 ")
        self.assertTest(test, "3l", False, None)
        self.assertTest(test, "32", True, Decimal(32))
        self.assertTest(test, "33", False, None)

        # test can be an expression
        test = EqualTest("@(contact.age - 2)")

        self.assertTest(test, "3l", False, None)
        self.assertTest(test, "32", True, Decimal(32))
        self.assertTest(test, "33", False, None)

    def test_less_than_test(self):
        test = LessThanTest("32 ")
        self.assertTest(test, "3l", True, Decimal(31))
        self.assertTest(test, "32", False, None)
        self.assertTest(test, "33", False, None)

        # test can be an expression
        test = LessThanTest("@(contact.age - 2)")

        self.assertTest(test, "3l", True, Decimal(31))
        self.assertTest(test, "32", False, None)
        self.assertTest(test, "33", False, None)

    def test_less_than_or_equal_test(self):
        test = LessThanOrEqualTest("32 ")
        self.assertTest(test, "3l", True, Decimal(31))
        self.assertTest(test, "32", True, Decimal(32))
        self.assertTest(test, "33", False, None)

        # test can be an expression
        test = LessThanOrEqualTest("@(contact.age - 2)")

        self.assertTest(test, "3l", True, Decimal(31))
        self.assertTest(test, "32", True, Decimal(32))
        self.assertTest(test, "33", False, None)

    def test_greater_than_test(self):
        test = GreaterThanTest("32 ")
        self.assertTest(test, "3l", False, None)
        self.assertTest(test, "32", False, None)
        self.assertTest(test, "33", True, Decimal(33))

        # test can be an expression
        test = GreaterThanTest("@(contact.age - 2)")

        self.assertTest(test, "3l", False, None)
        self.assertTest(test, "32", False, None)
        self.assertTest(test, "33", True, Decimal(33))

    def test_greater_than_or_equal_test(self):
        test = GreaterThanOrEqualTest("32 ")
        self.assertTest(test, "3l", False, None)
        self.assertTest(test, "32", True, Decimal(32))
        self.assertTest(test, "33", True, Decimal(33))

        # test can be an expression
        test = GreaterThanOrEqualTest("@(contact.age - 2)")

        self.assertTest(test, "3l", False, None)
        self.assertTest(test, "32", True, Decimal(32))
        self.assertTest(test, "33", True, Decimal(33))

    def test_has_date_test(self):
        HasDateTest.from_json({}, self.deserialization_context)

        test = HasDateTest()

        self.assertTest(test, "December 14, 1992", True, datetime.date(1992, 12, 14))
        self.assertTest(test, "sometime on 24/8/15", True, datetime.date(2015, 8, 24))

        self.assertTest(test, "no date in this text", False, None)

        # this differs from old implementation which was a bit too flexible regarding dates
        self.assertTest(test, "123", False, None)

    def test_date_equal_test(self):
        test = DateEqualTest("24/8/2015")

        self.assertTest(test, "23-8-15", False, None)
        self.assertTest(test, "it was Aug 24, 2015", True, datetime.date(2015, 8, 24))
        self.assertTest(test, "25th Aug '15", False, None)

        # date can be an expression
        self.context.put_variable("dob", "24-08-2015")
        test = DateEqualTest("@(dob)")

        self.assertTest(test, "23-8-15", False, None)
        self.assertTest(test, "it was Aug 24, 2015", True, datetime.date(2015, 8, 24))
        self.assertTest(test, "25th Aug '15", False, None)

    def test_date_after_test(self):
        test = DateAfterTest.from_json({"test": "December 14, 1892"}, self.deserialization_context)
        self.assertEqual(test.test, "December 14, 1892")

        test = DateAfterTest("24/8/2015")

        self.assertTest(test, "23-8-15", False, None)
        self.assertTest(test, "it was Aug 24, 2015", True, datetime.date(2015, 8, 24))
        self.assertTest(test, "25th Aug '15", True, datetime.date(2015, 8, 25))

        # date can be an expression
        self.context.put_variable("dob", "24-08-2015")
        test = DateAfterTest("@(dob)")

        self.assertTest(test, "23-8-15", False, None)
        self.assertTest(test, "it was Aug 24, 2015", True, datetime.date(2015, 8, 24))
        self.assertTest(test, "25th Aug '15", True, datetime.date(2015, 8, 25))

    def test_date_before_test(self):
        test = DateBeforeTest.from_json({"test": "December 14, 1892"}, self.deserialization_context)
        self.assertEqual(test.test, "December 14, 1892")

        test = DateBeforeTest("24/8/2015")

        self.assertTest(test, "23-8-15", True, datetime.date(2015, 8, 23))
        self.assertTest(test, "it was Aug 24, 2015", True, datetime.date(2015, 8, 24))
        self.assertTest(test, "25th Aug '15", False, None)

        # date can be an expression
        self.context.put_variable("dob", "24-08-2015")
        test = DateBeforeTest("@(dob)")

        self.assertTest(test, "23-8-15", True, datetime.date(2015, 8, 23))
        self.assertTest(test, "it was Aug 24, 2015", True, datetime.date(2015, 8, 24))
        self.assertTest(test, "25th Aug '15", False, None)

    def test_has_phone_test(self):
        HasPhoneTest.from_json({}, self.deserialization_context)

        test = HasPhoneTest()

        self.assertTest(test, "My phone number is 0788 383 383", True, "+250788383383")
        self.assertTest(test, "+250788123123", True, "+250788123123")
        self.assertTest(test, "+12067799294", True, "+12067799294")

        self.assertTest(test, "My phone is 0124515", False, None)

    def test_has_state_test(self):
        HasStateTest.from_json({}, self.deserialization_context)

        test = HasStateTest()

        self.assertTest(test, " kigali", True, "Kigali")
        self.assertTest(test, "Washington", False, None)

    def test_has_district_test(self):
        test = HasDistrictTest.from_json({"test": "kigali"}, self.deserialization_context)
        self.assertEqual(test.state, "kigali")

        test = HasDistrictTest("kigali")

        self.assertTest(test, " gasabo", True, "Gasabo")
        self.assertTest(test, "Nine", False, None)

        self.context.variables["extra"]["homestate"] = "Kigali"
        test = HasDistrictTest("@extra.homestate")

        self.assertTest(test, " gasabo", True, "Gasabo")
        self.assertTest(test, "Nine", False, None)


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

        self.assertEqual(TranslatableText({'eng': "Hello", 'fra': "Bonjour"}),
                         TranslatableText({'eng': "Hello", 'fra': "Bonjour"}))
        self.assertNotEqual(TranslatableText({'eng': "Hello", 'fra': "Salut"}),
                            TranslatableText({'eng': "Hello", 'fra': "Bonjour"}))


class UtilsTest(unittest.TestCase):

    def test_edit_distance(self):
        self.assertEqual(edit_distance("", ""), 0)
        self.assertEqual(edit_distance("abcd", "abcd"), 0)   # 0 differences
        self.assertEqual(edit_distance("abcd", "abc"), 1)    # 1 deletion
        self.assertEqual(edit_distance("abcd", "ad"), 2)     # 2 deletions
        self.assertEqual(edit_distance("abcd", "axbcd"), 1)  # 1 addition
        self.assertEqual(edit_distance("abcd", "acbd"), 1)   # 1 transposition

    def test_normalize_number(self):
        # valid numbers
        self.assertEquals(normalize_number("0788383383", "RW"), ("+250788383383", True))
        self.assertEquals(normalize_number("+250788383383", "KE"), ("+250788383383", True))
        self.assertEquals(normalize_number("+250788383383", None), ("+250788383383", True))
        self.assertEquals(normalize_number("250788383383", None), ("+250788383383", True))
        self.assertEquals(normalize_number("2.50788383383E+11", None), ("+250788383383", True))
        self.assertEquals(normalize_number("2.50788383383E+12", None), ("+250788383383", True))
        self.assertEquals(normalize_number("(917) 992-5253", "US"), ("+19179925253", True))
        self.assertEquals(normalize_number("19179925253", None), ("+19179925253", True))
        self.assertEquals(normalize_number("+62877747666", None), ("+62877747666", True))
        self.assertEquals(normalize_number("62877747666", "ID"), ("+62877747666", True))
        self.assertEquals(normalize_number("0877747666", "ID"), ("+62877747666", True))

        # invalid numbers
        self.assertEquals(normalize_number("12345", "RW"), ("12345", False))
        self.assertEquals(normalize_number("0788383383", None), ("0788383383", False))
        self.assertEquals(normalize_number("0788383383", "ZZ"), ("0788383383", False))
        self.assertEquals(normalize_number("MTN", "RW"), ("mtn", False))


class ValueTest(BaseFlowsTest):

    def test_to_and_from_json(self):
        time = datetime.datetime(2015, 8, 25, 11, 59, 30, 88000, pytz.UTC)
        value = Value("no", "No", "no way!", time)

        json_obj = value.to_json()

        self.assertEqual(json_obj, {'value': "no",
                                    'category': "No",
                                    'text': "no way!",
                                    'time': "2015-08-25T11:59:30.088Z"})

        value = Value.from_json(json_obj)

        self.assertEqual(value.value, "no")
        self.assertEqual(value.category, "No")
        self.assertEqual(value.text, "no way!")
        self.assertEqual(value.time, time)
