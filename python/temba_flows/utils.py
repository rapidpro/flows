from __future__ import absolute_import, unicode_literals

import phonenumbers
import regex


def edit_distance(s1, s2):
    """
    Compute the Damerau-Levenshtein distance between two given strings
    """
    d = {}
    lenstr1 = len(s1)
    lenstr2 = len(s2)

    for i in xrange(-1, lenstr1 + 1):
        d[(i, -1)] = i + 1
    for j in xrange(-1, lenstr2 + 1):
        d[(-1, j)] = j + 1

    for i in xrange(0, lenstr1):
        for j in xrange(0, lenstr2):
            if s1[i] == s2[j]:
                cost = 0
            else:
                cost = 1
            d[(i, j)] = min(
                d[(i-1, j)] + 1,  # deletion
                d[(i, j-1)] + 1,  # insertion
                d[(i-1, j-1)] + cost,  # substitution
            )
            if i > 1 and j > 1 and s1[i] == s2[j - 1] and s1[i - 1] == s2[j]:
                d[(i, j)] = min(d[(i, j)], d[i - 2, j - 2] + cost)  # transposition

    return d[lenstr1 - 1, lenstr2 - 1]


def normalize_number(number, country_code):
    """
    Normalizes the passed in number, they should be only digits, some backends prepend + and
    maybe crazy users put in dashes or parentheses in the console.
    :param number: the number, e.g. "0783835665"
    :param country_code: the 2-letter country code, e.g. "RW"
    :return: a tuple of the normalized number and whether it looks like a possible full international number
    """
    # if the number ends with e11, then that is Excel corrupting it, remove it
    if number.lower().endswith("e+11") or number.lower().endswith("e+12"):
        number = number[0:-4].replace('.', '')

    # remove other characters
    number = regex.sub('[^0-9a-z\+]', '', number.lower(), regex.V0)

    # add on a plus if it looks like it could be a fully qualified number
    if len(number) >= 11 and number[0] != '+':
        number = '+' + number

    try:
        normalized = phonenumbers.parse(number, str(country_code) if country_code else None)

        # now does it look plausible?
        if phonenumbers.is_possible_number(normalized):
            return phonenumbers.format_number(normalized, phonenumbers.PhoneNumberFormat.E164), True
    except Exception:
        pass

    # this must be a local number of some kind, just lowercase and save
    return regex.sub('[^0-9a-z]', '', number.lower(), regex.V0), False
