from __future__ import absolute_import, unicode_literals


class FlowParseException(Exception):
    """
    Exception thrown when flow JSON is invalid
    """
    def __init__(self, message):
        super(FlowParseException, self).__init__(message)


class TranslatableText(object):
    """
    Text that may be a single untranslated value or a translation map
    """
    def __init__(self, value):
        self.value = value

    @classmethod
    def from_json(cls, json_elem):
        if json_elem is None:
            return None

        if not (isinstance(json_elem, dict) or isinstance(json_elem, basestring)):
            raise FlowParseException("Translatable text be an object or string primitive")

        return TranslatableText(json_elem)

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
