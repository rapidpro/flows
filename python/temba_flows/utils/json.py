from __future__ import absolute_import, unicode_literals

import datetime
import pytz


JSON_DATETIME_FORMAT = '%Y-%m-%dT%H:%M:%S.%fZ'


def parse_json_date(value):
    """
    Parses a datetime as a UTC ISO8601 date time
    """
    if not value:
        return None

    return datetime.datetime.strptime(value, JSON_DATETIME_FORMAT).replace(tzinfo=pytz.UTC)


def format_json_date(dt):
    """
    Formats a datetime as an ISO8601 date time in UTC with millisecond precision
    """
    if not dt:
        return None

    # %f will include 6 microsecond digits
    micro_precision = dt.astimezone(pytz.UTC).strftime(JSON_DATETIME_FORMAT)

    # only keep the milliseconds portion of the second fraction
    return micro_precision[:-4] + 'Z'
