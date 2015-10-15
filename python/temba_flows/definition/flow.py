from __future__ import absolute_import, unicode_literals

import logging

from abc import ABCMeta, abstractmethod
from enum import Enum
from temba_expressions import conversions
from . import TranslatableText
from .actions import Action, MessageAction
from .tests import Test
from ..exceptions import FlowParseException

logger = logging.Logger(__name__)


class Flow(object):
    """
    A flow definition, typically loaded from JSON
    """
    SPEC_VERSIONS = {7, 8}

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
        if 'version' in json_obj:
            version = int(json_obj['version'])
            if version not in cls.SPEC_VERSIONS:
                raise FlowParseException("Unsupported flow spec version: %d" % version)
        else:
            raise FlowParseException("Missing flow spec version")

        flow_type = Flow.Type.from_code(json_obj['flow_type'])
        base_language = json_obj.get('base_language', None)

        flow = Flow(flow_type, base_language)

        # keep an exhaustive record of all languages in our flow definition
        languages = set()

        context = Flow.DeserializationContext(flow)

        for as_obj in json_obj['action_sets']:
            action_set = ActionSet.from_json(as_obj, context)
            flow.elements_by_uuid[action_set.uuid] = action_set

            # see what translations are set on this actionset
            for action in action_set.actions:
                if isinstance(action, MessageAction):
                    languages.update(action.msg.get_languages())

        for rs_obj in json_obj['rule_sets']:
            rule_set = RuleSet.from_json(rs_obj, context)
            flow.elements_by_uuid[rule_set.uuid] = rule_set

            for rule in rule_set.rules:
                flow.elements_by_uuid[rule.uuid] = rule
                languages.update(rule.category.get_languages())

        # lookup and set destination nodes
        for start, destination_uuid in context.destinations_to_set.iteritems():
            start.destination = flow.elements_by_uuid[destination_uuid]

        # only accept languages that are ISO 639-2 (alpha3)
        flow.languages = {l for l in languages if len(l) == 3}

        flow.entry = flow.get_element_by_uuid(json_obj.get("entry", None))

        return flow

    class DeserializationContext(object):
        """
        Allows state to be provided to deserialization methods
        """
        def __init__(self, flow):
            self.flow = flow
            self.destinations_to_set = {}

        def needs_destination(self, start, destination_uuid):
            self.destinations_to_set[start] = destination_uuid

    class Element(object):
        """
        Super class of anything in a flow definition with a UUID
        """
        __metaclass__ = ABCMeta

        def __init__(self, uuid):
            self.uuid = uuid

        def __eq__(self, other):
            return self.uuid == other.uuid

    class Node(Element):
        """
        Super class for ActionSet and RuleSet. Things which can be a destination in a flow graph.
        """
        __metaclass__ = ABCMeta

        @abstractmethod
        def visit(self, runner, run, step, input):
            pass

    def get_element_by_uuid(self, uuid):
        return self.elements_by_uuid.get(uuid, None)


class ActionSet(Flow.Node):
    """
    A flow node which is a set of actions to be performed
    """
    def __init__(self, uuid):
        super(ActionSet, self).__init__(uuid)
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

    def visit(self, runner, run, step, input):
        if logger.isEnabledFor(logging.DEBUG):
            logger.debug("Visiting action set %s with input %s from contact %s"
                         % (self.uuid, unicode(input), run.contact.uuid))

        for action in self.actions:
            result = action.execute(runner, run, input)
            step.add_action_result(result)

        return self.destination


class RuleSet(Flow.Node):
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

    def __init__(self, uuid, ruleset_type, label, operand, config):
        super(RuleSet, self).__init__(uuid)
        self.ruleset_type = ruleset_type
        self.label = label
        self.operand = operand
        self.config = config
        self.rules = []

    @classmethod
    def from_json(cls, json_obj, context):
        rule_set_type = RuleSet.Type[json_obj['ruleset_type'].upper()]
        label = json_obj['label']
        operand = json_obj.get('operand', None)
        config = json_obj.get('config', {})
        rule_set = RuleSet(json_obj['uuid'], rule_set_type, label, operand, config)

        for rule_obj in json_obj["rules"]:
            rule_set.rules.append(Rule.from_json(rule_obj, context))

        return rule_set

    def visit(self, runner, run, step, input):
        if logger.isEnabledFor(logging.DEBUG):
            logger.debug("Visiting rule set %s with input %s from contact %s"
                         % (self.uuid, unicode(input), run.contact.uuid))

        input.consume()

        context = run.build_context(runner, input)

        match = self.find_matching_rule(runner, run, context)
        if not match:
            return None

        rule, test_result = match

        # get category in the flow base language
        category = rule.category.get_localized_by_preferred([run.flow.base_language], "")

        value_as_str = conversions.to_string(test_result.value, context)
        result = RuleSet.Result(rule, value_as_str, category, input.get_value_as_text(context))
        step.rule_result = result

        run.update_value(self, result, input.time)

        return rule.destination

    def find_matching_rule(self, runner, run, context):
        """
        Runs through the rules to find the first one that matches
        :param runner: the flow runner
        :param run: the current run state
        :param context: the evaluation context
        :return: the matching rule and the test result
        """
        # for form fields, construct operand as field expression
        if self.ruleset_type == RuleSet.Type.FORM_FIELD:
            field_delimiter = self.config.get('field_delimiter', ' ')
            field_index = self.config.get('field_index', 0) + 1
            operand = '@(FIELD(%s, %d, "%s"))' % (self.operand[1:], field_index, field_delimiter)
        else:
            operand = self.operand

        operand, errors = runner.substitute_variables(operand, context)

        for rule in self.rules:
            result = rule.matches(runner, run, context, operand)
            if result.matched:
                return rule, result
        return None

    def is_pause(self):
        return self.ruleset_type == RuleSet.Type.WAIT_MESSAGE \
               or self.ruleset_type == RuleSet.Type.WAIT_RECORDING \
               or self.ruleset_type == RuleSet.Type.WAIT_DIGIT \
               or self.ruleset_type == RuleSet.Type.WAIT_DIGITS

    class Result(object):
        """
        Holds the result of a ruleset evaluation
        """
        def __init__(self, rule, value, category, text):
            self.rule = rule
            self.value = value
            self.category = category
            self.text = text

        @classmethod
        def from_json(cls, json_obj, context):
            return cls(context.flow.get_element_by_uuid(json_obj['uuid']),
                       json_obj['value'],
                       json_obj['category'],
                       json_obj['text'])

        def to_json(self):
            return {
                'uuid': self.rule.uuid,
                'value': self.value,
                'category': self.category,
                'text': self.text
            }


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

    def matches(self, runner, run, context, input):
        """
        Checks whether this rule is a match for the given input
        :param runner: the flow runner
        :param run: the current run state
        :param context: the evaluation context
        :param input: the input
        :return: the test result
        """
        return self.test.evaluate(runner, run, context, input)
