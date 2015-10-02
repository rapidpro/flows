from __future__ import absolute_import, unicode_literals

from enum import Enum
from . import TranslatableText
from .actions import Action, MessageAction
from .tests import Test


class Flow(object):
    """
    A flow definition, typically loaded from JSON
    """
    class Type(Enum):
        FLOW = 'F'
        MESSAGE = 'M'
        VOICE = 'V'
        SURVEY = 'S'

        def __init__(self, code):
            self.code = code

        @classmethod
        def from_code(cls, code):
            for name, val in cls.__members__.iteritems():
                if code == val.code:
                    return val
            return None

    def __init__(self, flow_type, base_language):
        self.flow_type = flow_type
        self.languages = None
        self.base_language = base_language
        self.entry = None
        self.elements_by_uuid = {}

    @classmethod
    def from_json(cls, json_obj):
        flow_type = Flow.Type.from_code(json_obj['flow_type'])
        base_language = json_obj.get('base_language', None)
        flow = Flow(flow_type, base_language)

        definition = json_obj['definition']

        # keep an exhaustive list of all languages in our flow definition
        languages = []

        context = Flow.DeserializationContext(flow)

        for as_obj in definition['action_sets']:
            action_set = ActionSet.from_json(as_obj, context)
            flow.elements_by_uuid[action_set.uuid] = action_set

            # see what translations are set on this actionset
            for action in action_set.actions:
                if isinstance(action, MessageAction):
                    languages += action.msg.get_languages()

        for rs_obj in definition['rule_sets']:
            rule_set = RuleSet.from_json(rs_obj, context)
            flow.elements_by_uuid[rule_set.uuid] = rule_set

            for rule in rule_set.rules:
                flow.elements_by_uuid[rule.uuid] = rule
                languages += rule.category.get_languages()

        # lookup and set destination nodes
        for start, destination_uuid in context.destinations_to_set.iteritems():
            start.destination = flow.elements_by_uuid[destination_uuid]

        # only accept languages that are ISO 639-2 (alpha3)
        flow.languages = {l for l in languages if len(l) == 3}

        flow.entry = flow.get_element_by_uuid(definition.get("entry", None))

        return flow

    class DeserializationContext(object):
        def __init__(self, flow):
            self.flow = flow
            self.destinations_to_set = {}

        def needs_destination(self, start, destination_uuid):
            self.destinations_to_set[start] = destination_uuid

    def get_element_by_uuid(self, uuid):
        return self.elements_by_uuid.get(uuid, None)


class ActionSet(object):
    """
    A flow node which is a set of actions to be performed
    """
    def __init__(self, uuid):
        self.uuid = uuid
        self.actions = []
        self.destination = None  # set later

    @classmethod
    def from_json(cls, json_obj, context):
        action_set = ActionSet(json_obj['uuid'])

        destination_uuid = json_obj.get('destination', None)
        if destination_uuid:
            context.needs_destination(action_set, destination_uuid)

        for action_obj in json_obj['actions']:
            action = Action.from_json(action_obj, context)
            action_set.actions.append(action)

        return action_set


class RuleSet(object):
    """
    A flow node which is a set of rules, each with its own destination node
    """
    class Type(Enum):
        WAIT_MESSAGE = 1
        WAIT_RECORDING = 2
        WAIT_DIGIT = 3
        WAIT_DIGITS = 4
        WEBHOOK = 5
        FLOW_FIELD = 6
        FORM_FIELD = 7
        CONTACT_FIELD = 8
        EXPRESSION = 9

    def __init__(self, uuid, ruleset_type, label, operand):
        self.uuid = uuid
        self.ruleset_type = ruleset_type
        self.label = label
        self.operand = operand
        self.rules = []

    @classmethod
    def from_json(cls, json_obj, context):
        rule_set_type = RuleSet.Type[json_obj['ruleset_type'].upper()]
        label = json_obj['label']
        operand = json_obj.get('operand', None)
        rule_set = RuleSet(json_obj['uuid'], rule_set_type, label, operand)

        for rule_obj in json_obj["rules"]:
            rule_set.rules.append(Rule.from_json(rule_obj, context))

        return rule_set


class Rule(object):
    """
    A matchable rule in a rule set
    """
    def __init__(self, uuid, test, category):
        self.uuid = uuid
        self.test = test
        self.category = category
        self.destination = None  # set later

    @classmethod
    def from_json(cls, json_obj, context):
        test = Test.from_json(json_obj['test'], context)
        category = TranslatableText.from_json(json_obj['category'])
        rule = Rule(json_obj['uuid'], test, category)

        destination_uuid = json_obj.get('destination', None)
        if destination_uuid:
            context.needs_destination(rule, destination_uuid)

        return rule
