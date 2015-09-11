from __future__ import absolute_import, unicode_literals

import operator
import regex

from expressions import conversions


def field(ctx, text, index, delimiter=' '):
    """
    Reference a field in string separated by a delimiter
    """
    splits = text.split(delimiter)

    # remove our delimiters and whitespace
    splits = [f for f in splits if f != delimiter and len(f.strip()) > 0]

    index = conversions.to_integer(index, ctx)
    if index < 1:
        raise ValueError('Field index cannot be less than 1')

    if index <= len(splits):
        return splits[index-1]
    else:
        return ''


def first_word(ctx, text):
    """
    Returns the first word in the given text string
    """
    # In Excel this would be IF(ISERR(FIND(" ",A2)),"",LEFT(A2,FIND(" ",A2)-1))
    return word(ctx, text, 1)


def percent(ctx, number):
    """
    Formats a number as a percentage
    """
    return '%d%%' % int(round(conversions.to_decimal(number, ctx) * 100))


def read_digits(ctx, text):
    """
    Formats digits in text for reading in TTS
    """
    def chunk(value, chunk_size):
        return [value[i: i + chunk_size] for i in range(0, len(value), chunk_size)]

    text = conversions.to_string(text, ctx).strip()
    if not text:
        return ''

    # trim off the plus for phone numbers
    if text[0] == '+':
        text = text[1:]

    length = len(text)

    # ssn
    if length == 9:
        result = ' '.join(text[:3])
        result += ' , ' + ' '.join(text[3:5])
        result += ' , ' + ' '.join(text[5:])
        return result

    # triplets, most international phone numbers
    if length % 3 == 0 and length > 3:
        chunks = chunk(text, 3)
        return ' '.join(','.join(chunks))

    # quads, credit cards
    if length % 4 == 0:
        chunks = chunk(text, 4)
        return ' '.join(','.join(chunks))

    # otherwise, just put a comma between each number
    return ','.join(text)


def remove_first_word(ctx, text):
    """
    Removes the first word from the given text string
    """
    text = conversions.to_string(text, ctx).lstrip()
    first = first_word(ctx, text)
    return text[len(first):].lstrip() if first else ''


def word(ctx, text, number, by_spaces=False):
    """
    Extracts the nth word from the given text string
    """
    return word_slice(ctx, text, number, conversions.to_integer(number, ctx) + 1, by_spaces)


def word_count(ctx, text, by_spaces=False):
    """
    Returns the number of words in the given text string
    """
    text = conversions.to_string(text, ctx)
    by_spaces = conversions.to_boolean(by_spaces, ctx)
    return len(__get_words(text, by_spaces))


def word_slice(ctx, text, start, stop=0, by_spaces=False):
    """
    Extracts a substring spanning from start up to but not-including stop
    """
    text = conversions.to_string(text, ctx)
    start = conversions.to_integer(start, ctx)
    stop = conversions.to_integer(stop, ctx)
    by_spaces = conversions.to_boolean(by_spaces, ctx)

    if start == 0:
        raise ValueError("Start word cannot be zero")
    elif start > 0:
        start -= 1  # convert to a zero-based offset

    if stop == 0:  # zero is treated as no end
        stop = None
    elif stop > 0:
        stop -= 1  # convert to a zero-based offset

    words = __get_words(text, by_spaces)

    selection = operator.getitem(words, slice(start, stop))

    # re-combine selected words with a single space
    return ' '.join(selection)


#################################### Helper (not available in expressions) ####################################

def __get_words(text, by_spaces):
    """
    Helper function which splits the given text string into words. If by_spaces is false, then text like
    '01-02-2014' will be split into 3 separate words. For backwards compatibility, this is the default for all
    expression functions.
    :param text: the text to split
    :param by_spaces: whether words should be split only by spaces or by punctuation like '-', '.' etc
    """
    rexp = r'\s+' if by_spaces else r'\W+'
    splits = regex.split(rexp, text, flags=regex.MULTILINE | regex.UNICODE | regex.V0)
    return [split for split in splits if split]   # return only non-empty
