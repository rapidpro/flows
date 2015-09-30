from __future__ import absolute_import, unicode_literals

import datetime
import phonenumbers
import pytz

from enum import Enum
from temba_expressions import conversions


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
                   json_obj['timezone'],
                   json_obj['date_style'],
                   json_obj['anon'])

    def to_json(self):
        return {'country': self.country, 'primary_language': self.primary_language, 'timezone': self.timezone,
                'date_style': self.date_style, 'anon': self.is_anon}


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
                   set(json_obj['groups']),
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

    def build_context(self, org):
        """
        Builds the evaluation context for this contact
        :param org: the org
        :return: the context
        """
        context = {
            '*': self.get_display(org, False),
            'name': self.name,
            'first_name': self.get_first_name(org),
            'tel_e164': self.get_urn_display(org, ContactUrn.Scheme.TEL, True),
            'groups': ",".join(self.groups),
            'uuid': self.uuid,
            'language': self.language
        }

        # add all URNs
        for scheme in ContactUrn.Scheme.__members__.values():
            context[unicode(scheme.name).lower()] = self.get_urn_display(org, scheme, False)

        # add all fields
        for key, value in self.fields.iteritems():
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
