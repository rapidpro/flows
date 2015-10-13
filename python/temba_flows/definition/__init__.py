from __future__ import absolute_import, unicode_literals

from abc import ABCMeta
from ..exceptions import FlowParseException


class TranslatableText(object):
    """
    Text that may be a single untranslated value or a translation map
    """
    def __init__(self, value):
        self.value = value

    @classmethod
    def from_json(cls, json_elm):
        if json_elm is None:
            return None

        if not (isinstance(json_elm, dict) or isinstance(json_elm, basestring)):
            raise FlowParseException("Translatable text be an object or string primitive")

        return TranslatableText(json_elm)

    def to_json(self):
        return self.value

    def get_localized(self, run_state=None, default_text=""):
        """
        Gets the localized text. We return according to the following precedence:
          1) Contact's language
          2) Org Primary Language
          3) Flow Base Language
          4) Default Text
        :param run_state: the ran state
        :param default_text: the default to return if there's no suitable translation
        :return: localized text
        """
        preferred_languages = []

        if run_state.contact.language:
            preferred_languages.append(run_state.contact.language)

        preferred_languages.append(run_state.org.primary_language)
        preferred_languages.append(run_state.flow.base_language)

        return self.get_localized_by_preferred(preferred_languages, default_text)

    def get_localized_by_preferred(self, preferred_languages, default_text):
        if not self.value:
            return default_text
        elif isinstance(self.value, basestring):
            return self.value

        for lang in preferred_languages:
            if lang in self.value:
                return self.value[lang]

        return default_text

    def get_languages(self):
        return self.value.keys() if isinstance(self.value, dict) else set()

    def __eq__(self, other):
        return self.value == other.value


class ObjectRef(object):
    """
    Base class for references that can be an object like {"id":123,"name":"Testers"} or an expression string
    """
    __metaclass__ = ABCMeta

    def __init__(self, id_val, name):
        self.id = id_val
        self.name = name

    @classmethod
    def from_json(cls, json_obj, context):
        if isinstance(json_obj, dict):
            return cls(json_obj.get('id'), json_obj.get('name'))
        else:
            return cls(None, json_obj)

    def to_json(self):
        if self.id:
            return {'id': self.id, 'name': self.name}
        else:
            return self.name

    def __eq__(self, other):
        return self.id == other.id and self.name == other.name


class ContactRef(ObjectRef):
    """
    Reference to a contact which can be an object like {"id":123,"name":"Mr Test"} or an expression string
    """
    pass


class GroupRef(ObjectRef):
    """
    Reference to a contact group which can be an object like {"id":123,"name":"Testers"} or an expression string
    """
    pass


class LabelRef(ObjectRef):
    """
    Reference to a message label which can be an object like {"id":123,"name":"Testing"} or an expression string
    """
    pass


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
