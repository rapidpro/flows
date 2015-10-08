from __future__ import absolute_import, unicode_literals

from abc import ABCMeta, abstractmethod
from temba_expressions.evaluator import EvaluationContext
from . import TranslatableText
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


class VariableRef(object):
    """
    A variable reference to a contact group (by name) or a contact (by phone)
    """
    NEW_CONTACT = "@new_contact"

    def __init__(self, value):
        self.value = value

    @classmethod
    def from_json(cls, json_obj, context):
        return cls(json_obj.get('id'))

    def to_json(self):
        return {'id': self.value}

    def is_new_contact(self):
        """
        Returns whether this variable is a placeholder for a new contact
        """
        return self.value.lower() == self.NEW_CONTACT


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
            context = run.build_context(input)
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
        return {'type': self.TYPE, 'msg': self.msg}

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
                   json_obj.get('contacts'),
                   json_obj.get('groups'),
                   [VariableRef.from_json(v, context) for v in json_obj.get('variables', [])])

    def to_json(self):
        return {'type': self.TYPE,
                'msg': self.msg,
                'contacts': self.contacts,
                'groups': self.groups,
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

        performed = SendAction(TranslatableText(template), self.groups, self.contacts, variables)
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
        context = run.build_context(input)

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

    def execute(self, runner, run, input):
        value, errors = runner.substitute_variables(self.value, run.build_context(input))
        if not errors:
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
                runner.update_contact_field(run, self.field, value)

            return Action.Result.performed(SaveToContactAction(self.field, label, value))
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


class AddToGroupsAction(Action):
    """
    Adds the contact to one or more groups
    """
    TYPE = 'add_group'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def execute(self, runner, run, input):
        pass

    # TODO


class RemoveFromGroupsAction(Action):
    """
    Removes the contact from one or more groups
    """
    TYPE = 'del_group'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def execute(self, runner, run, input):
        pass

    # TODO


class AddLabelsAction(Action):
    """
    Adds one or more labels to the incoming message
    """
    TYPE = 'add_label'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def execute(self, runner, run, input):
        pass

    # TODO
