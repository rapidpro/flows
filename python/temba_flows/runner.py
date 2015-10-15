from __future__ import absolute_import, unicode_literals

import datetime
import phonenumbers
import pytz
import regex

from abc import ABCMeta, abstractmethod
from datetime import timedelta
from enum import Enum
from ordered_set import OrderedSet
from temba_expressions import conversions
from temba_expressions.dates import DateStyle
from temba_expressions.evaluator import Evaluator, EvaluationContext, EvaluationStrategy
from temba_expressions.utils import format_json_date, parse_json_date
from .definition.flow import Action, Flow, RuleSet
from .exceptions import FlowRunException, FlowLoopException
from .utils import normalize_number


DEFAULT_EVALUATOR = Evaluator(expression_prefix='@',
                              allowed_top_levels=('channel', 'contact', 'date', 'extra', 'flow', 'step'))


class Org(object):
    """
    An organization - used to provide additional information about how a flow should be run
    """
    def __init__(self, country, primary_language, timezone, date_style, is_anon):
        self.country = country
        self.primary_language = primary_language
        self.timezone = timezone
        self.date_style = date_style
        self.is_anon = is_anon

    @classmethod
    def from_json(cls, json_obj):
        return cls(json_obj['country'],
                   json_obj['primary_language'],
                   pytz.timezone(json_obj['timezone']),
                   DateStyle[json_obj['date_style'].upper()],
                   json_obj['anon'])

    def to_json(self):
        return {
            'country': self.country,
            'primary_language': self.primary_language,
            'timezone': unicode(self.timezone),
            'date_style': self.date_style.name.lower(),
            'anon': self.is_anon
        }


class Field(object):
    """
    A contact field
    """
    # can't create contact fields with these keys
    RESERVED_KEYS = ('name', 'first_name', 'phone', 'language', 'created_by', 'modified_by', 'org', 'uuid', 'groups')

    class ValueType(Enum):
        TEXT = 'T'
        DECIMAL = 'N'
        DATETIME = 'D'
        STATE = 'S'
        DISTRICT = 'I'

        def __init__(self, code):
            self.code = code

        @classmethod
        def from_code(cls, code):
            for name, val in cls.__members__.iteritems():
                if code == val.code:
                    return val
            return None

    def __init__(self, key, label, value_type, is_new=False):
        if not self.is_valid_key(key):
            raise ValueError("Field key '%s' is invalid or reserved" % key)

        if not self.is_valid_label(label):
            raise ValueError("Field label '%s' is invalid" % label)

        self.key = key
        self.label = label
        self.value_type = value_type
        self.is_new = is_new

    @classmethod
    def from_json(cls, json_obj):
        return cls(json_obj['key'], json_obj['label'], Field.ValueType.from_code(json_obj['value_type']))

    def to_json(self):
        return {'key': self.key, 'label': self.label, 'value_type': self.value_type.code}

    @classmethod
    def make_key(cls, label):
        key = regex.sub(r'([^a-z0-9]+)', ' ', label.lower(), regex.V0).strip()
        return regex.sub(r'([^a-z0-9]+)', '_', key, regex.V0)

    @classmethod
    def is_valid_key(cls, key):
        return regex.match(r'^[a-z][a-z0-9_]*$', key, regex.V0) and key not in cls.RESERVED_KEYS

    @classmethod
    def is_valid_label(cls, label):
        return regex.match(r'^[A-Za-z0-9\- ]+$', label, regex.V0)

    def __eq__(self, other):
        return self.key == other.key and self.label == other.label and self.value_type == other.value_type

    def __ne__(self, other):
        return not (self == other)

    def __hash__(self):
        return hash(self.key)


class Contact(object):
    """
    A contact that can participate in a flow
    """
    def __init__(self, uuid, name, urns, groups, fields, language):
        self.uuid = uuid
        self.name = name
        self.urns = urns
        self.groups = groups
        self.fields = fields
        self.language = language

    @classmethod
    def from_json(cls, json_obj):
        return cls(json_obj.get('uuid', None),
                   json_obj['name'],
                   [ContactUrn.from_string(u) for u in json_obj['urns']],
                   OrderedSet(json_obj['groups']),
                   json_obj['fields'],
                   json_obj.get('language', None))

    def to_json(self):
        return {'uuid': self.uuid,
                'name': self.name,
                'urns': [unicode(u) for u in self.urns],
                'groups': list(self.groups),
                'fields': self.fields,
                'language': self.language}

    def get_first_name(self, org):
        if not self.name:
            return self.get_urn_display(org)
        else:
            names = self.name.split()
            if len(names) > 1:
                return names[0]
            else:
                return self.name

    def set_first_name(self, first_name):
        if not self.name:
            self.name = first_name
        else:
            names = self.name.split()
            names = [first_name] + names[1:]
            self.name = " ".join(names)

    def get_display(self, org, full=False):
        """
        Gets a displayable name or URN for the contact. If available, org can be provided to avoid having to fetch it
        again based on the contact.
        """
        if self.name:
            return self.name
        elif org.is_anon:
            return self.get_anon_identifier()
        else:
            return self.get_urn_display(org=org, full=full)

    def get_urn(self, schemes=None):
        """
        Gets the highest priority matching URN for this contact
        """
        if schemes is not None:
            for urn in self.urns:
                if urn.scheme in schemes:
                    return urn
            return None
        else:
            # otherwise return highest priority of any scheme
            return self.urns[0] if self.urns else None

    def get_urn_display(self, org, scheme=None, full=False):
        """
        Gets a displayable URN for the contact. If available, org can be provided to avoid having to fetch it again
        based on the contact.
        """
        if org.is_anon:
            return self.get_anon_identifier()

        schemes = [scheme] if scheme else None
        urn = self.get_urn(schemes)
        return urn.get_display(org=org, full=full) if urn else ''

    def get_anon_identifier(self):
        # TODO where can we get the usual anon identifier from? Is UUID an ok substitute?
        return self.uuid

    def build_context(self, run, container):
        """
        Builds the evaluation context for this contact
        :param run: the current run state
        :param container: the containing evaluation context
        :return: the context
        """
        context = {
            '*': self.get_display(run.org, False),
            'name': self.name,
            'first_name': self.get_first_name(run.org),
            'tel_e164': self.get_urn_display(run.org, ContactUrn.Scheme.TEL, True),
            'groups': ",".join(self.groups),
            'uuid': self.uuid,
            'language': self.language
        }

        # add all URNs
        for scheme in ContactUrn.Scheme.__members__.values():
            context[unicode(scheme.name).lower()] = self.get_urn_display(run.org, scheme, False)

        # add all fields
        for key, raw_value in self.fields.iteritems():
            field = run.get_or_create_field(key)

            if field and field.value_type == Field.ValueType.DATETIME:
                as_datetime = conversions.to_datetime(raw_value, container)
                value = conversions.to_string(as_datetime, container)
            else:
                value = raw_value

            context[key] = value

        return context


class ContactUrn(object):
    """
    A URN for a contact (e.g. a telephone number or twitter handle)
    """
    class Scheme(Enum):
        TEL = 1
        TWITTER = 2

    ANON_MASK = '********'

    def __init__(self, scheme, path):
        self.scheme = scheme
        self.path = path

    @classmethod
    def from_string(cls, urn):
        """
        Parses a URN from a string
        :param urn: the string, e.g. tel:+260964153686, twitter:joe
        :return: the parsed URN
        """
        parts = urn.split(':', 2)
        scheme = ContactUrn.Scheme[parts[0].upper()]
        return ContactUrn(scheme, parts[1])

    def normalized(self, org):
        """
        Returns a normalized version of this URN
        :param org: the org
        :return: the normalized URN
        """
        if self.scheme == ContactUrn.Scheme.TWITTER:
            norm_path = self.path.strip()
            if norm_path[0] == '@':
                norm_path = norm_path[1:]
        else:
            norm_path, is_valid = normalize_number(self.path, org.country)

        return ContactUrn(self.scheme, norm_path)

    def get_display(self, org, full=False):
        """
        Gets a representation of the URN for display
        """
        if org.is_anon:
            return self.ANON_MASK

        if self.scheme == ContactUrn.Scheme.TEL and not full:
            # if we don't want a full tell, see if we can show the national format instead
            try:
                if self.path and self.path[0] == '+':
                    return phonenumbers.format_number(phonenumbers.parse(self.path, None),
                                                      phonenumbers.PhoneNumberFormat.NATIONAL)
            except Exception:
                pass

        return self.path

    def __eq__(self, other):
        return self.scheme == other.scheme and self.path == other.path

    def __unicode__(self):
        return '%s:%s' % (unicode(self.scheme.name).lower(), self.path)


class Input(object):

    def __init__(self, value, time=None):
        self.value = value
        self.time = time if time else datetime.datetime.now(tz=pytz.UTC)
        self.consumed = False

    def build_context(self, container, contact_context):
        """
        Builds the evaluation context for this input
        :param container: the evaluation context
        :param contact_context: the context
        :return:
        """
        as_text = self.get_value_as_text(container)

        return {
            '*': as_text,
            'value': as_text,
            'time': conversions.to_string(self.time, container),
            'contact': contact_context
        }

    def get_value_as_text(self, context):
        """
        Gets the input value as text which can be matched by rules
        :param context: the evaluation context
        :return: the text value
        """
        return conversions.to_string(self.value, context)

    def consume(self):
        self.consumed = True


class Location(object):
    """
    Simple location model
    """
    class Level(Enum):
        STATE = 1
        DISTRICT = 2

    def __init__(self, osm_id, name, level):
        self.osm_id = osm_id
        self.name = name
        self.level = level

    class Resolver(object):
        __metaclass__ = ABCMeta

        @abstractmethod
        def resolve(self, text, country, level, parent):
            """
            Resolves a location name from the given input
            :param text: the text to parse
            :param country: the 2-digit country code
            :param level: the level
            :param parent: the parent location (may be null)
            :return: the location or null if no such location exists
            """
            pass


class Step(object):
    """
    A step taken by a contact or surveyor in a flow run
    """
    def __init__(self, node, arrived_on, left_on=None, rule_result=None, actions=None, errors=None):
        self.node = node
        self.arrived_on = arrived_on
        self.left_on = left_on
        self.rule_result = rule_result
        self.actions = actions if actions else []
        self.errors = errors if errors else []

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(context.flow.get_element_by_uuid(json_obj['node']),
                   parse_json_date(json_obj['arrived_on']),
                   parse_json_date(json_obj['left_on']),
                   RuleSet.Result.from_json(json_obj['rule'], context) if json_obj.get('rule') else None,
                   [Action.from_json(a, context) for a in json_obj['actions']],
                   json_obj['errors'])

    def to_json(self):
        return {
            'node': self.node.uuid,
            'arrived_on': format_json_date(self.arrived_on),
            'left_on': format_json_date(self.left_on),
            'rule': self.rule_result.to_json() if self.rule_result else None,
            'actions': [a.to_json() for a in self.actions],
            'errors': self.errors
        }

    def add_action_result(self, action_result):
        if action_result.performed:
            self.actions.append(action_result.performed)

        if action_result.errors:
            self.errors += action_result.errors

    def is_completed(self):
        return self.left_on is not None


class Value(object):
    """
    Holds the result of a contact's last visit to a ruleset
    """
    def __init__(self, value, category, text, time):
        self.value = value
        self.category = category
        self.text = text
        self.time = time

    @classmethod
    def from_json(cls, json_object):
        return cls(json_object['value'],
                   json_object['category'],
                   json_object['text'],
                   parse_json_date(json_object['time']))

    def to_json(self):
        return {
            'value': self.value,
            'category': self.category,
            'text': self.text,
            'time': format_json_date(self.time)
        }

    def build_context(self, container):
        return {
            '*': self.value,
            'value': self.value,
            'category': self.category,
            'text': self.text,
            'time': conversions.to_string(self.time, container)
        }


class RunState(object):
    """
    Represents state of a flow run after visiting one or more nodes in the flow
    """
    class State(Enum):
        IN_PROGRESS = 1
        COMPLETED = 2
        WAIT_MESSAGE = 3

    def __init__(self, org, fields, contact, flow):
        self.org = org
        self.fields = {f.key: f for f in fields}
        self.contact = contact
        self.started = datetime.datetime.now(tz=pytz.UTC)
        self.steps = []
        self.values = {}
        self.extra = {}
        self.state = RunState.State.IN_PROGRESS
        self.flow = flow

    @classmethod
    def from_json(cls, json_obj, flow):
        """
        Restores a run state from JSON
        :param json_obj: the JSON containing a serialized run state
        :param flow: the flow the run state is for
        :return: the run state
        """
        deserialization_context = Flow.DeserializationContext(flow)

        run = cls(Org.from_json(json_obj['org']),
                  [Field.from_json(f) for f in json_obj['fields']],
                  Contact.from_json(json_obj['contact']),
                  flow)

        run.started = parse_json_date(json_obj['started'])
        run.steps = [Step.from_json(s, deserialization_context) for s in json_obj['steps']]
        run.values = {k: Value.from_json(v) for k, v in json_obj['values'].iteritems()}
        run.extra = json_obj['extra']
        run.state = RunState.State[json_obj['state'].upper()]
        return run

    def to_json(self):
        """
        Serializes this run state to JSON
        """
        return {
            'org': self.org.to_json(),
            'fields': [f.to_json() for f in self.fields.values()],
            'contact': self.contact.to_json(),
            'started': format_json_date(self.started),
            'steps': [s.to_json() for s in self.steps],
            'values': {k: v.to_json() for k, v in self.values.iteritems()},
            'extra': self.extra,
            'state': self.state.name.lower()
        }

    def build_context(self, runner, input):
        # our concept of now may be overridden by the runner
        now = runner.now if runner.now else datetime.datetime.now(tz=self.org.timezone)

        context = EvaluationContext({}, self.org.timezone, self.org.date_style, now)

        contact_context = self.contact.build_context(self, context)

        if input is not None:
            context.put_variable("step", input.build_context(context, contact_context))

        context.put_variable("date", self.build_date_context(context))
        context.put_variable("contact", contact_context)
        context.put_variable("extra", self.extra)

        flow_context = {}
        values = []
        for key, value in self.values.iteritems():
            flow_context[key] = value.build_context(context)
            values.append("%s: %s" % (key, value))
        flow_context['*'] = "\n".join(values)

        context.put_variable("flow", flow_context)

        return context

    def update_value(self, rule_set, result, time):
        """
        Updates a value in response to a rule match
        :param rule_set: the rule set
        :param result: the rule match result
        :param time: the time from the input
        :return:
        """
        key = regex.sub(r'[^a-z0-9]+', '_', rule_set.label.lower())
        self.values[key] = Value(result.value, result.category, result.text, time)

    @staticmethod
    def build_date_context(container):
        """
        Builds the date context (i.e. @date.now, @date.today, ...)
        """
        as_date = container.now.date()
        as_datetime_str = conversions.to_string(container.now, container)
        as_date_str = conversions.to_string(as_date, container)

        return {
            '*': as_datetime_str,
            'now': as_datetime_str,
            'today': as_date_str,
            'tomorrow': conversions.to_string(as_date + timedelta(days=1), container),
            'yesterday': conversions.to_string(as_date - timedelta(days=1), container)
        }

    def get_completed_steps(self):
        """
        Gets the completed steps, i.e. those where the contact left the node or a terminal node
        """
        completed = []
        for step in self.steps:
            if step.is_completed() or self.state == RunState.State.COMPLETED:
                completed.append(step)
        return completed

    def get_or_create_field(self, key, label=None, value_type=Field.ValueType.TEXT):
        """
        Gets or creates a contact field
        """
        if not key and not label:
            raise ValueError("Must provide either key or label")

        if key:
            field = self.fields.get(key)
            if field:
                return field
        else:
            key = Field.make_key(label)

        if not label:
            label = regex.sub(r'([^A-Za-z0-9\- ]+)', ' ', key, regex.V0).title()

        field = Field(key, label, value_type, is_new=True)
        self.fields[key] = field
        return field

    def get_created_fields(self):
        return [f for f in self.fields.values() if f.is_new]


class Runner(object):
    """
    The flow runner
    """
    def __init__(self, template_evaluator=DEFAULT_EVALUATOR, location_resolver=None, now=None):
        self.template_evaluator = template_evaluator
        self.location_resolver = location_resolver
        self.now = now

    def start(self, org, fields, contact, flow):
        """
        Starts a new run
        :param org: the org
        :param fields: the contact fields
        :param contact: the contact
        :param flow: the flow
        :return: the run state
        """
        run = RunState(org, fields, contact, flow)
        return self.resume(run, None)

    def resume(self, run, input):
        """
        Resumes an existing run with new input
        :param run: the previous run state
        :param input: the new input
        :return: the updated run state
        """
        if run.state == RunState.State.COMPLETED:
            raise FlowRunException("Cannot resume a completed run state")

        last_step = run.steps[-1] if len(run.steps) > 0 else None

        # reset steps list so that it doesn't grow forever in a never-ending flow
        run.steps = []

        if last_step:
            current_node = last_step.node  # we're resuming an existing run
        else:
            current_node = run.flow.entry  # we're starting a new run
            if not current_node:
                raise FlowRunException("Flow has no entry point")

        # tracks nodes visited so we can detect loops
        nodes_visited = OrderedSet()

        while current_node:
            # if we're resuming a previously paused step, then use its arrived on value
            if last_step and len(nodes_visited) == 0:
                arrived_on = last_step.arrived_on
            else:
                arrived_on = datetime.datetime.now(tz=pytz.UTC)

            # create new step for this node
            step = Step(current_node, arrived_on)
            run.steps.append(step)

            # should we pause at this node?
            if isinstance(current_node, RuleSet):
                if current_node.is_pause() and (not input or input.consumed):
                    run.state = RunState.State.WAIT_MESSAGE
                    return run

            # check for an non-pausing loop
            if current_node in nodes_visited:
                raise FlowLoopException(nodes_visited)
            else:
                nodes_visited.add(current_node)

            next_node = current_node.visit(self, run, step, input)

            if next_node:
                # if we have a next node, then record leaving this one
                step.left_on = datetime.datetime.now(tz=pytz.UTC)
            else:
                # if not then we've completed this flow
                run.state = RunState.State.COMPLETED

            current_node = next_node

        return run

    def substitute_variables(self, text, context):
        """
        Performs variable substitution on the the given text
        :param text: the text, e.g. "Hi @contact.name"
        :param context: the evaluation context
        :return: the evaluated template, e.g. "Hi Joe"
        """
        return self.template_evaluator.evaluate_template(text, context)

    def substitute_variables_if_available(self, text, context):
        """
        Performs partial variable substitution on the the given text
        :param text: the text, e.g. "Hi @contact.name"
        :param context: the evaluation context
        :return: the evaluated template, e.g. "Hi Joe"
        """
        return self.template_evaluator.evaluate_template(text, context, False, EvaluationStrategy.RESOLVE_AVAILABLE)

    def parse_location(self, text, country, level, parent=None):
        """
        Parses a location from the given text
        :param text: the text containing a location name
        :param country: the 2-digit country code
        :param level: the level
        :param parent: the parent location (may be null)
        :return: the location or null if no such location exists
        """
        if self.location_resolver:
            return self.location_resolver.resolve(text, country, level, parent)
        return None

    def update_contact_field(self, run, key, value, label=None):
        """
        Updates a field on the contact for the given run
        :param run: the current run state
        :param key: the field key
        :param value: the field value
        :return the field which may have been created
        """
        field = run.get_or_create_field(key, label)
        actual_value = None

        if field.value_type in (Field.ValueType.TEXT, Field.ValueType.DECIMAL, Field.ValueType.DATETIME):
            actual_value = value
        elif field.value_type == Field.ValueType.STATE:
            state = self.location_resolver.resolve(value, run.org.country, Location.Level.STATE, None)
            if state:
                actual_value = state.name
        elif field.value_type == Field.ValueType.DISTRICT:
            state_field = self.get_state_field(run)
            if state_field:
                state_name = run.contact.fields.get(state_field.key, None)
                if state_name:
                    state = self.location_resolver.resolve(state_name, run.org.country, Location.Level.STATE, None)
                    if state:
                        district = self.location_resolver.resolve(value, run.org.country, Location.Level.DISTRICT, state)
                        if district:
                            actual_value = district.name

        run.contact.fields[field.key] = actual_value
        return field

    def update_extra(self, run, values):
        """
        Updates the extra key values for the given run state
        :param run: the run state
        :param values: the key values
        """
        run.extra.update(values)

    def get_state_field(self, run):
        # TODO this mimics what we currently do in RapidPro but needs changed
        for field in run.fields.values():
            if field.value_type == Field.ValueType.STATE:
                return field
        return None
