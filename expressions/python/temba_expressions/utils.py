from __future__ import absolute_import, unicode_literals

import math
import urllib

from decimal import Decimal


def decimal_pow(number, power):
    """
    Pow for two decimals
    """
    return Decimal(math.pow(number, power))


def urlquote(text):
    """
    Encodes text for inclusion in a URL query string. Should be equivalent to Django's urlquote function.
    :param text: the text to encode
    :return: the encoded text
    """
    return urllib.quote(text.encode('utf-8'))
