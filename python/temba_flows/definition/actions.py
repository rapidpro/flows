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
                AddLabelAction.TYPE: AddLabelAction,
            }

        action_type = json_obj['type']
        action_cls = cls.CLASS_BY_TYPE.get(action_type, None)
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
            self.errors = errors

        @classmethod
        def performed(cls, performed, errors=()):
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
        return cls(TranslatableText.from_json(json_obj.get('msg', None)))

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
        # TODO
        return cls(TranslatableText.from_json(json_obj.get('msg', None)),
                   json_obj.get('contacts', None),
                   json_obj.get('groups', None),
                   json_obj.get('variables', None))

    def to_json(self):
        # TODO
        return {'type': self.TYPE, 'msg': self.msg, }

    def execute_with_message(self, runner, context, msg):
        # TODO evaluate variables (except @new_contact)... though what do we return them as ?

        # create a new context without the @contact.* variables which will remain unresolved for now
        new_vars = context.variables.copy()
        del new_vars['contact']
        context_for_other_contacts = EvaluationContext(new_vars, context.timezone, context.date_style)

        template, errors = runner.substitute_variables_if_available(msg, context_for_other_contacts)

        performed = SendAction(TranslatableText(template), self.groups, self.contacts, self.variables)
        return Action.Result.performed(performed, errors)


class EmailAction(MessageAction):
    """
    Sends an email to someone
    """
    TYPE = 'email'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def execute(self, runner, run, input):
        pass

    # TODO


class SaveToContactAction(Action):
    """
    Saves an evaluated expression to the contact as a field or their name
    """
    TYPE = 'save'

    @classmethod
    def from_json(cls, json_obj, context):
        return cls()

    def execute(self, runner, run, input):
        pass

    # TODO


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
        return cls(json_obj['lang'], json_obj.get('name', None))

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


class AddLabelAction(Action):
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
