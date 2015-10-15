from __future__ import absolute_import, unicode_literals

from abc import ABCMeta, abstractmethod
from temba_expressions.evaluator import EvaluationContext
from . import TranslatableText, ContactRef, GroupRef, LabelRef, VariableRef
from ..exceptions import FlowParseException


class Action(object):
    """
    An action which can be performed inside an action set
    """
    __metaclass__ = ABCMeta

    CLASS_BY_TYPE = None  # lazily initialized below

    @classmethod
    def from_json(cls, json_obj, context):
        if not cls.CLASS_BY_TYPE:
            cls.CLASS_BY_TYPE = {
                ReplyAction.TYPE: ReplyAction,
                SendAction.TYPE: SendAction,
                EmailAction.TYPE: EmailAction,
                SaveToContactAction.TYPE: SaveToContactAction,
                SetLanguageAction.TYPE: SetLanguageAction,
                AddToGroupsAction.TYPE: AddToGroupsAction,
                RemoveFromGroupsAction.TYPE: RemoveFromGroupsAction,
                AddLabelsAction.TYPE: AddLabelsAction,
            }

        action_type = json_obj['type']
        action_cls = cls.CLASS_BY_TYPE.get(action_type)
        if not action_cls:
            raise FlowParseException("Unknown action type: %s" % action_type)

        return action_cls.from_json(json_obj, context)

    @abstractmethod
    def execute(self, runner, run, input):
        """
        Executes this action
        :param runner: the flow runner
        :param run: the current run state
        :param input: the current input
        :return: the action result (action that was actually performed and any errors)
        """
        pass

    class Result(object):
        """
        Holds the result of an action execution
        """
        def __init__(self, performed, errors):
            self.performed = performed
            self.errors = errors if errors is not None else []

        @classmethod
        def performed(cls, performed, errors=None):
            return cls(performed, errors)

        @classmethod
        def errors(cls, errors):
            return cls(None, errors)


Action.Result.NOOP = Action.Result(None, ())


class MessageAction(Action):
    """
    Base class for actions which send a message
    """
    __metaclass__ = ABCMeta

    def __init__(self, msg):
        self.msg = msg

    def execute(self, runner, run, input):
        msg = self.msg.get_localized(run)
        if msg:
            context = run.build_context(runner, input)
            return self.execute_with_message(runner, context, msg)
        else:
            return Action.Result.NOOP

    @abstractmethod
    def execute_with_message(self, runner, context, msg):
        pass


class ReplyAction(MessageAction):
    """
    Sends a message to the contact
    """
    TYPE = 'reply'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(TranslatableText.from_json(json_obj.get('msg')))

    def to_json(self):
        return {'type': self.TYPE, 'msg': self.msg.to_json()}

    def execute_with_message(self, runner, context, msg):
        template, errors = runner.substitute_variables(msg, context)

        performed = ReplyAction(TranslatableText(template))
        return Action.Result.performed(performed, errors)


class SendAction(MessageAction):
    """
    Sends a message to people other than the contact
    """
    TYPE = 'send'

    def __init__(self, msg, contacts, groups, variables):
        super(SendAction, self).__init__(msg)
        self.contacts = contacts
        self.groups = groups
        self.variables = variables

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(TranslatableText.from_json(json_obj.get('msg')),
                   [ContactRef.from_json(c, context) for c in json_obj.get('contacts')],
                   [GroupRef.from_json(g, context) for g in json_obj.get('groups')],
                   [VariableRef.from_json(v, context) for v in json_obj.get('variables', [])])

    def to_json(self):
        return {'type': self.TYPE,
                'msg': self.msg.to_json(),
                'contacts': [c.to_json() for c in self.contacts],
                'groups': [g.to_json() for g in self.groups],
                'variables': [v.to_json() for v in self.variables]}

    def execute_with_message(self, runner, context, msg):
        errors = []

        # variables should evaluate to group names or phone numbers
        variables = []
        for variable in self.variables:
            if not variable.is_new_contact():
                var, var_errors = runner.substitute_variables(variable.value, context)
                if not var_errors:
                    variables.append(VariableRef(var))
                else:
                    errors += var_errors
            else:
                variables.append(VariableRef(variable.value))

        # create a new context without the @contact.* variables which will remain unresolved for now
        new_vars = context.variables.copy()
        del new_vars['contact']
        context_for_other_contacts = EvaluationContext(new_vars, context.timezone, context.date_style)

        template, errors = runner.substitute_variables_if_available(msg, context_for_other_contacts)

        performed = SendAction(TranslatableText(template), self.contacts, self.groups, variables)
        return Action.Result.performed(performed, errors)


class EmailAction(Action):
    """
    Sends an email to someone
    """
    TYPE = 'email'

    def __init__(self, addresses, subject, msg):
        self.addresses = addresses
        self.subject = subject
        self.msg = msg

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj.get('emails'), json_obj.get('subject'), json_obj.get('msg'))

    def to_json(self):
        return {'type': self.TYPE,
                'emails': self.addresses,
                'subject': self.subject,
                'msg': self.msg}

    def execute(self, runner, run, input):
        context = run.build_context(runner, input)

        subject, subject_errors = runner.substitute_variables(self.subject, context)
        message, message_errors = runner.substitute_variables(self.msg, context)

        errors = subject_errors + message_errors

        addresses = []
        for address in self.addresses:
            addr, addr_errors = runner.substitute_variables(address, context)
            addresses.append(addr)
            errors += addr_errors

        performed = EmailAction(addresses, subject, message)
        return Action.Result.performed(performed, errors)


class SaveToContactAction(Action):
    """
    Saves an evaluated expression to the contact as a field or their name
    """
    TYPE = 'save'

    def __init__(self, field, label, value):
        self.field = field
        self.label = label
        self.value = value

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj.get('field'), json_obj.get('label'), json_obj.get('value'))

    def to_json(self):
        return {'type': self.TYPE, 'field': self.field, 'label': self.label, 'value': self.value}

    def execute(self, runner, run, input):
        value, errors = runner.substitute_variables(self.value, run.build_context(runner, input))
        if not errors:
            field = self.field
            value = value.strip()

            if self.field == 'name':
                value = value[:128]
                label = "Contact Name"
                run.contact.name = value

            elif self.field == 'first_name':
                value = value[:128]
                label = "First Name"
                run.contact.set_first_name(value)

            elif self.field == 'tel_e164':
                value = value[:128]
                label = "Phone Number"

                from ..runner import ContactUrn
                urn = ContactUrn(ContactUrn.Scheme.TEL, value).normalized(run.org)
                run.contact.urns.append(urn)

            else:
                value = value[:640]
                label = self.label
                try:
                    field_obj = runner.update_contact_field(run, self.field, value, label)
                    field = field_obj.key
                    label = field_obj.label
                except ValueError, e:
                    return Action.Result.errors([e.message])

            return Action.Result.performed(SaveToContactAction(field, label, value))
        else:
            return Action.Result.errors(errors)


class SetLanguageAction(Action):
    """
    Sets the contact's language
    """
    TYPE = 'lang'

    def __init__(self, lang, name):
        self.lang = lang
        self.name = name

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj.get('lang'), json_obj.get('name'))

    def to_json(self):
        return {'type': self.TYPE, 'lang': self.lang, 'name': self.name}

    def execute(self, runner, run, input):
        run.contact.language = self.lang
        return Action.Result.performed(SetLanguageAction(self.lang, self.name))


class GroupMembershipAction(Action):
    """
    Base class for actions which operate on a list of groups
    """
    __metaclass__ = ABCMeta

    def __init__(self, groups):
        self.groups = groups

    def execute(self, runner, run, input):
        context = run.build_context(runner, input)
        groups = []
        errors = []

        for group in self.groups:
            if not group.id:
                name, name_errors = runner.substitute_variables(group.name, context)
                if not name_errors:
                    groups.append(GroupRef(None, name))
                else:
                    errors += name_errors
            else:
                groups.append(group)

        return self.execute_with_groups(runner, run, groups, errors)

    @abstractmethod
    def execute_with_groups(self, runner, run, groups, errors):
        pass


class AddToGroupsAction(GroupMembershipAction):
    """
    Adds the contact to one or more groups
    """
    TYPE = 'add_group'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls([GroupRef.from_json(g, context) for g in json_obj['groups']])

    def to_json(self):
        return {'type': self.TYPE, 'groups': [g.to_json() for g in self.groups]}

    def execute_with_groups(self, runner, run, groups, errors):
        if groups:
            for group in groups:
                run.contact.groups.add(group.name)
            return Action.Result.performed(AddToGroupsAction(groups), errors)
        else:
            return Action.Result.errors(errors)


class RemoveFromGroupsAction(GroupMembershipAction):
    """
    Removes the contact from one or more groups
    """
    TYPE = 'del_group'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls([GroupRef.from_json(g, context) for g in json_obj['groups']])

    def to_json(self):
        return {'type': self.TYPE, 'groups': [g.to_json() for g in self.groups]}

    def execute_with_groups(self, runner, run, groups, errors):
        if groups:
            for group in groups:
                if group.name in run.contact.groups:
                    run.contact.groups.remove(group.name)
            return Action.Result.performed(AddToGroupsAction(groups), errors)
        else:
            return Action.Result.errors(errors)


class AddLabelsAction(Action):
    """
    Adds one or more labels to the incoming message
    """
    TYPE = 'add_label'

    def __init__(self, labels):
        self.labels = labels

    @classmethod
    def from_json(cls, json_obj, context):
        return cls([LabelRef.from_json(l, context) for l in json_obj['labels']])

    def to_json(self):
        return {'type': self.TYPE, 'groups': [l.to_json() for l in self.labels]}

    def execute(self, runner, run, input):
        context = run.build_context(runner, input)
        labels = []
        errors = []

        for label in self.labels:
            if not label.id:
                name, name_errors = runner.substitute_variables(label.name, context)
                if not name_errors:
                    labels.append(LabelRef(None, name))
                else:
                    errors += name_errors
            else:
                labels.append(label)

        if labels:
            return Action.Result.performed(AddLabelsAction(labels), errors)
        else:
            return Action.Result.errors(errors)
