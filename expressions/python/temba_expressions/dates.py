from __future__ import absolute_import, unicode_literals

import datetime
import pkg_resources
import regex

from collections import OrderedDict
from enum import Enum


class DateStyle(Enum):
    DAY_FIRST = 1
    MONTH_FIRST = 2


class Component(Enum):
    YEAR = 0  # 99 or 1999
    MONTH = 1  # 1 or Jan
    DAY = 2
    HOUR = 3
    MINUTE = 4
    HOUR_AND_MINUTE = 5  # e.g. 1400
    SECOND = 6
    AM_PM = 7


class Mode(Enum):
    DATE = 1
    DATETIME = 2
    TIME = 3
    AUTO = 4


##
# Flexible date parser for human written dates
##
class DateParser(object):

    AM = 0
    PM = 1

    DATE_SEQUENCES_DAY_FIRST = [
        [Component.DAY, Component.MONTH, Component.YEAR],
        [Component.MONTH, Component.DAY, Component.YEAR],
        [Component.YEAR, Component.MONTH, Component.DAY],
        [Component.DAY, Component.MONTH],
        [Component.MONTH, Component.DAY],
        [Component.MONTH, Component.YEAR],
    ]

    DATE_SEQUENCES_MONTH_FIRST = [
        [Component.MONTH, Component.DAY, Component.YEAR],
        [Component.DAY, Component.MONTH, Component.YEAR],
        [Component.YEAR, Component.MONTH, Component.DAY],
        [Component.MONTH, Component.DAY],
        [Component.DAY, Component.MONTH],
        [Component.MONTH, Component.YEAR],
    ]

    TIME_SEQUENCES = [
        [Component.HOUR_AND_MINUTE],
        [Component.HOUR, Component.MINUTE],
        [Component.HOUR, Component.MINUTE, Component.AM_PM],
        [Component.HOUR, Component.MINUTE, Component.SECOND],
        [Component.HOUR, Component.MINUTE, Component.SECOND, Component.AM_PM],
    ]

    def __init__(self, now, timezone, date_style):
        """
        Creates a new date parser
        :param now: the now which parsing happens relative to
        :param timezone: the timezone in which times are interpreted
        :param date_style: whether dates are usually entered day first or month first
        """
        self._now = now
        self._timezone = timezone
        self._date_style = date_style

    def auto(self, text):
        """
        Returns a date or datetime depending on what information is available
        :param text: the text to parse
        :return: the parsed date or datetime
        """
        return self._parse(text, Mode.AUTO)

    def time(self, text):
        """
        Tries to parse a time value from the given text
        :param text: the text to parse
        :return: the parsed time
        """
        return self._parse(text, Mode.TIME)

    def _parse(self, text, mode):
        """
        Returns a date, datetime or time depending on what information is available
        """
        if not text.strip():
            return None

        # split the text into numerical and text tokens
        tokens = regex.findall(r'([0-9]+|\w+)', text, flags=regex.MULTILINE | regex.UNICODE | regex.V0)

        # get the possibilities for each token
        token_possibilities = []
        for token in tokens:
            possibilities = self._get_token_possibilities(token, mode)
            if len(possibilities) > 0:
                token_possibilities.append(possibilities)

        # see what valid sequences we can make
        sequences = self._get_possible_sequences(mode, len(token_possibilities), self._date_style)
        possible_matches = []

        for sequence in sequences:
            match = OrderedDict()

            for c in range(len(sequence)):
                component = sequence[c]
                value = token_possibilities[c].get(component, None)
                match[component] = value

                if value is None:
                    break
            else:
                possible_matches.append(match)

        # find the first match that can form a valid date or datetime
        for match in possible_matches:
            obj = self._make_result(match, self._now, self._timezone)
            if obj is not None:
                return obj

        return None

    @classmethod
    def _get_possible_sequences(cls, mode, length, date_style):
        """
        Gets possible component sequences in the given mode
        :param mode: the mode
        :param length: the length (only returns sequences of this length)
        :param date_style: whether dates are usually entered day first or month first
        :return:
        """
        sequences = []
        date_sequences = cls.DATE_SEQUENCES_DAY_FIRST if date_style == DateStyle.DAY_FIRST else cls.DATE_SEQUENCES_MONTH_FIRST

        if mode == Mode.DATE or mode == Mode.AUTO:
            for seq in date_sequences:
                if len(seq) == length:
                    sequences.append(seq)

        elif mode == Mode.TIME:
            for seq in cls.TIME_SEQUENCES:
                if len(seq) == length:
                    sequences.append(seq)

        if mode == Mode.DATETIME or mode == Mode.AUTO:
            for date_seq in date_sequences:
                for time_seq in cls.TIME_SEQUENCES:
                    if len(date_seq) + len(time_seq) == length:
                        sequences.append(date_seq + time_seq)

        return sequences

    @classmethod
    def _get_token_possibilities(cls, token, mode):
        """
        Returns all possible component types of a token without regard to its context. For example "26" could be year,
        date or minute, but can't be a month or an hour.
        :param token: the token to classify
        :param mode: the parse mode
        :return: the dict of possible types and values if token was of that type
        """
        token = token.lower().strip()
        possibilities = {}
        try:
            as_int = int(token)

            if mode != Mode.TIME:
                if 1 <= as_int <= 9999 and (len(token) == 2 or len(token) == 4):
                    possibilities[Component.YEAR] = as_int
                if 1 <= as_int <= 12:
                    possibilities[Component.MONTH] = as_int
                if 1 <= as_int <= 31:
                    possibilities[Component.DAY] = as_int

            if mode != Mode.DATE:
                if 0 <= as_int <= 23:
                    possibilities[Component.HOUR] = as_int
                if 0 <= as_int <= 59:
                    possibilities[Component.MINUTE] = as_int
                if 0 <= as_int <= 59:
                    possibilities[Component.SECOND] = as_int
                if len(token) == 4:
                    hour = as_int / 100
                    minute = as_int - (hour * 100)
                    if 1 <= hour <= 24 and 1 <= minute <= 59:
                        possibilities[Component.HOUR_AND_MINUTE] = as_int

        except ValueError:
            if mode != Mode.TIME:
                # could it be a month alias?
                month = MONTHS_BY_ALIAS.get(token, None)
                if month is not None:
                    possibilities[Component.MONTH] = month

            if mode != Mode.DATE:
                # could it be an AM/PM marker?
                is_am_marker = token == "am"
                is_pm_marker = token == "pm"
                if is_am_marker or is_pm_marker:
                    possibilities[Component.AM_PM] = cls.AM if is_am_marker else cls.PM

        return possibilities

    @classmethod
    def _make_result(cls, values, now, timezone):
        """
        Makes a date or datetime or time object from a map of component values
        :param values: the component values
        :param now: the current now
        :param timezone: the current timezone
        :return: the date, datetime, time or none if values are invalid
        """
        date = None
        time = None

        if Component.MONTH in values:
            year = cls._year_from_2digits(values.get(Component.YEAR, now.year), now.year)
            month = values[Component.MONTH]
            day = values.get(Component.DAY, 1)
            try:
                date = datetime.date(year, month, day)
            except ValueError:
                return None  # not a valid date

        if (Component.HOUR in values and Component.MINUTE in values) or Component.HOUR_AND_MINUTE in values:
            if Component.HOUR_AND_MINUTE in values:
                combined = values[Component.HOUR_AND_MINUTE]
                hour = combined / 100
                minute = combined - (hour * 100)
                second = 0
            else:
                hour = values[Component.HOUR]
                minute = values[Component.MINUTE]
                second = values.get(Component.SECOND, 0)

                if hour <= 12 and values.get(Component.AM_PM, cls.AM) == cls.PM:
                    hour += 12

            try:
                time = datetime.time(hour, minute, second)
            except ValueError:
                return None  # not a valid time

        if date is not None and time is not None:
            return datetime.datetime.combine(date, time).replace(tzinfo=timezone)
        elif date is not None:
            return date
        elif time is not None:
            return time
        else:
            return None

    @staticmethod
    def _year_from_2digits(short_year, current_year):
        """
        Converts a relative 2-digit year to an absolute 4-digit year
        :param short_year: the relative year
        :param current_year: the current year
        :return: the absolute year
        """
        if short_year < 100:
            short_year += current_year - (current_year % 100)
            if abs(short_year - current_year) >= 50:
                if short_year < current_year:
                    return short_year + 100
                else:
                    return short_year - 100
        return short_year


def load_month_aliases(filename):
    alias_file = pkg_resources.resource_string(__name__, filename).decode('UTF-8', 'replace')
    aliases = {}
    month = 1
    for line in alias_file.split('\n'):
        for alias in line.split(','):
            aliases[alias] = month
        month += 1
    return aliases


MONTHS_BY_ALIAS = load_month_aliases('month.aliases')
