from __future__ import absolute_import, unicode_literals

import datetime

from decimal import Decimal, getcontext, ROUND_HALF_UP
from . import EvaluationError


def to_boolean(value, ctx):
    """
    Tries conversion of any value to a boolean
    """
    if isinstance(value, bool):
        return value
    elif isinstance(value, int):
        return value != 0
    elif isinstance(value, Decimal):
        return value != Decimal(0)
    elif isinstance(value, basestring):
        value = value.lower()
        if value == 'true':
            return True
        elif value == 'false':
            return False
    elif isinstance(value, datetime.date) or isinstance(value, datetime.time):
        return True

    raise EvaluationError("Can't convert '%s' to a boolean" % unicode(value))


def to_integer(value, ctx):
    """
    Tries conversion of any value to an integer
    """
    if isinstance(value, bool):
        return 1 if value else 0
    elif isinstance(value, int):
        return value
    elif isinstance(value, Decimal):
        try:
            val = int(value.to_integral_exact(ROUND_HALF_UP))
            if isinstance(val, int):
                return val
        except ArithmeticError:
            pass
    elif isinstance(value, basestring):
        try:
            return int(value)
        except ValueError:
            pass

    raise EvaluationError("Can't convert '%s' to an integer" % unicode(value))


def to_decimal(value, ctx):
    """
    Tries conversion of any value to a decimal
    """
    if isinstance(value, bool):
        return Decimal(1) if value else Decimal(0)
    elif isinstance(value, int):
        return Decimal(value)
    elif isinstance(value, Decimal):
        return value
    elif isinstance(value, basestring):
        try:
            return Decimal(value)
        except Exception:
            pass

    raise EvaluationError("Can't convert '%s' to a decimal" % unicode(value))


def to_string(value, ctx):
    """
    Tries conversion of any value to a string
    """
    if isinstance(value, bool):
        return "TRUE" if value else "FALSE"
    elif isinstance(value, int):
        return unicode(value)
    elif isinstance(value, Decimal):
        return format_decimal(value)
    elif isinstance(value, basestring):
        return value
    elif type(value) == datetime.date:
        return value.strftime(ctx.get_date_format(False))
    elif isinstance(value, datetime.time):
        return value.strftime('%H:%M')
    elif isinstance(value, datetime.datetime):
        return value.astimezone(ctx.timezone).strftime(ctx.get_date_format(True))

    raise EvaluationError("Can't convert '%s' to a string" % unicode(value))


def to_date(value, ctx):
    """
    Tries conversion of any value to a date
    """
    if isinstance(value, basestring):
        temporal = ctx.get_date_parser().auto(value)
        if temporal is not None:
            return to_date(temporal, ctx)
    elif type(value) == datetime.date:
        return value
    elif isinstance(value, datetime.datetime):
        return value.date()  # discard time

    raise EvaluationError("Can't convert '%s' to a date" % unicode(value))


def to_datetime(value, ctx):
    """
    Tries conversion of any value to a datetime
    """
    if isinstance(value, basestring):
        temporal = ctx.get_date_parser().auto(value)
        if temporal is not None:
            return to_datetime(temporal, ctx)
    elif type(value) == datetime.date:
        return datetime.datetime.combine(value, datetime.time(0, 0)).replace(tzinfo=ctx.timezone)
    elif isinstance(value, datetime.datetime):
        return value.astimezone(ctx.timezone)

    raise EvaluationError("Can't convert '%s' to a datetime" % unicode(value))


def to_date_or_datetime(value, ctx):
    """
    Tries conversion of any value to a date or datetime
    """
    if isinstance(value, basestring):
        temporal = ctx.get_date_parser().auto(value)
        if temporal is not None:
            return temporal
    elif type(value) == datetime.date:
        return value
    elif isinstance(value, datetime.datetime):
        return value.astimezone(ctx.timezone)

    raise EvaluationError("Can't convert '%s' to a date or datetime" % unicode(value))


def to_time(value, ctx):
    """
    Tries conversion of any value to a time
    """
    if isinstance(value, basestring):
        time = ctx.get_date_parser().time(value)
        if time is not None:
            return time
    elif isinstance(value, datetime.time):
        return value
    elif isinstance(value, datetime.datetime):
        return value.astimezone(ctx.timezone).time()

    raise EvaluationError("Can't convert '%s' to a time" % unicode(value))


def to_same(value1, value2, ctx):
    """
    Converts a pair of arguments to their most-likely types. This deviates from Excel which doesn't auto convert values
    but is necessary for us to intuitively handle contact fields which don't use the correct value type
    """
    if type(value1) == type(value2):
        return value1, value2

    try:
        # try converting to two decimals
        return to_decimal(value1, ctx), to_decimal(value2, ctx)
    except EvaluationError:
        pass

    try:
        # try converting to two dates
        return to_date_or_datetime(value1, ctx), to_date_or_datetime(value2, ctx)
    except EvaluationError:
        pass

    # try converting to two strings
    return to_string(value1, ctx), to_string(value2, ctx)


def to_repr(value, ctx):
    """
    Converts a value back to its representation form, e.g. x -> "x"
    """
    as_string = to_string(value, ctx)

    if isinstance(value, basestring) or isinstance(value, datetime.date) or isinstance(value, datetime.time):
        as_string = as_string.replace('"', '""')  # escape quotes by doubling
        as_string = '"%s"' % as_string

    return as_string


def format_decimal(decimal):
    """
    Formats a decimal number using the same precision as Excel
    :param decimal: the decimal value
    :return: the formatted string value
    """
    getcontext().rounding = ROUND_HALF_UP

    # strip trailing zeros
    normalized = decimal.normalize()
    sign, digits, exponent = normalized.as_tuple()
    if exponent >= 1:
        normalized = normalized.quantize(1)
        sign, digits, exponent = normalized.as_tuple()

    int_digits = len(digits) + exponent
    fractional_digits = min(max(10 - int_digits, 0), -exponent)

    normalized = normalized.quantize(Decimal(10) ** -fractional_digits)

    return unicode(normalized)
