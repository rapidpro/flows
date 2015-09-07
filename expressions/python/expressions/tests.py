# coding=utf-8
from __future__ import absolute_import, unicode_literals

import pytz
import unittest

# from .evaluator import get_te
# from temba.utils.parser import EvaluationContext, evaluate_template


from datetime import datetime, date, time
from .dates.parser import DateParser, DateStyle


class DateParserTest(unittest.TestCase):

    def test_auto(self):
        tz = pytz.timezone('Africa/Kigali')
        parser = DateParser(date(2015, 8, 12), tz, DateStyle.DAY_FIRST)

        tests = (
            ("1/2/34", date(2034, 2, 1)),
            ("1-2-34", date(2034, 2, 1)),
            ("01 02 34", date(2034, 2, 1)),
            ("1 Feb 34", date(2034, 2, 1)),
            ("1. 2 '34", date(2034, 2, 1)),
            ("1st february 2034", date(2034, 2, 1)),
            ("1er février 2034", date(2034, 2, 1)),
            ("2/25-70", date(1970, 2, 25)),  # date style should be ignored when it doesn't make sense
            ("1 feb", date(2015, 2, 1)),  # year can be omitted
            ("Feb 1st", date(2015, 2, 1)),
            ("1 feb 9999999", date(2015, 2, 1)),  # ignore invalid values
            ("1/2/34 14:55", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("1-2-34 2:55PM", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("01 02 34 1455", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("1 Feb 34 02:55 PM", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("1. 2 '34 02:55pm", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("1st february 2034 14.55", datetime(2034, 2, 1, 14, 55, 0, 0, tz)),
            ("1er février 2034 1455h", datetime(2034, 2, 1, 14, 55, 0, 0, tz))
        )
        for test in tests:
            self.assertEqual(parser.auto(test[0]), test[1], "Parser error for %s" % test[0])

    def test_time(self):
        tz = pytz.timezone('Africa/Kigali')
        parser = DateParser(date(2015, 8, 12), tz, DateStyle.DAY_FIRST)

        tests = (
            ("2:55", time(2, 55, 0)),
            ("2:55 AM", time(2, 55, 0)),
            ("14:55", time(14, 55, 0)),
            ("2:55PM", time(14, 55, 0)),
            ("1455", time(14, 55, 0)),
            ("02:55 PM", time(14, 55, 0)),
            ("02:55pm", time(14, 55, 0)),
            ("14.55", time(14, 55, 0)),
            ("1455h", time(14, 55, 0)),
            ("14:55:30", time(14, 55, 30)),
            ("14:55.30PM", time(14, 55, 30))
        )
        for test in tests:
            self.assertEqual(parser.time(test[0]), test[1], "Parser error for %s" % test[0])

    def test_year_from_2digits(self):
        self.assertEqual(DateParser._year_from_2digits(1, 2015), 2001)
        self.assertEqual(DateParser._year_from_2digits(64, 2015), 2064)
        self.assertEqual(DateParser._year_from_2digits(65, 2015), 1965)
        self.assertEqual(DateParser._year_from_2digits(99, 2015), 1999)

        self.assertEqual(DateParser._year_from_2digits(1, 1990), 2001)
        self.assertEqual(DateParser._year_from_2digits(40, 1990), 2040)
        self.assertEqual(DateParser._year_from_2digits(41, 1990), 1941)
        self.assertEqual(DateParser._year_from_2digits(99, 1990), 1999)


# class ExistingTembaSystemTest(unittest.TestCase):
#
#    def test_templates(self):
#        evaluator = Excellent.get_template_evaluator()


class TemplateTest(object):

    def __init__(self, json):
        self.template = json['template']
        # self.context = EvaluationContext(json['context']['vars'], {'tz': json['context']['tz'],
        #                                                            'day_first': json['context']['day_first']})
        # self.url_encode = json['url_encode']
        self.expected_output = json['output']
        self.expected_errors = json['errors']

        self.actual_output = None
        self.actual_errors = None

    def run(self, evaluator):
        evaluated = evaluator.evaluate_template(self.template, self.context, self.url_encode)
        self.actual_output = evaluated.output
        self.actual_errors = evaluated.errors

        return self.expected_output == self.actual_output and self.expected_errors == self.actual_errors
