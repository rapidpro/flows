from __future__ import absolute_import, unicode_literals

from abc import ABCMeta, abstractmethod
from . import TranslatableText


class Action(object):
    """
    An action which can be performed inside an action set
    """
    __metaclass__ = ABCMeta

    @classmethod
    def from_json(cls, json_obj, context):
        # TODO
        return ReplyAction(TranslatableText("testing..."))

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
            return None, []

    @abstractmethod
    def execute_with_message(self, runner, context, msg):
        pass


class ReplyAction(MessageAction):
    """
    Sends a message to the contact
    """
    TYPE = 'reply'

    @classmethod
    def from_json(cls, org, json):
        return ReplyAction(TranslatableText.from_json(json.get('msg', None)))

    def to_json(self):
        return {'type': ReplyAction.TYPE, 'msg': self.msg}

    def execute_with_message(self, runner, context, msg):
        from . import TranslatableText

        template = runner.substitute_variables(msg, context)

        performed = ReplyAction(TranslatableText(template.output))
        return performed, template.errors


