from __future__ import absolute_import, unicode_literals

import unittest

from .evaluator import Excellent
from temba.utils.parser import EvaluationContext, evaluate_template


class ExistingTembaSystemTest(unittest.TestCase):

    def test_templates(self):
        evaluator = Excellent.get_template_evaluator()




class TemplateTest(object):

    def __init__(self, json):
        self.template = json['template']
        self.context = EvaluationContext(json['context']['vars'], {'tz': json['context']['tz'],
                                                                   'day_first': json['context']['day_first']})
        self.url_encode = json['url_encode']
        self.expected_output = json['output']
        self.expected_errors = json['errors']

        self.actual_output = None
        self.actual_errors = None

    def run(self, evaluator):
        evaluated = evaluator.evaluate_template(self.template, self.context, self.url_encode)
        self.actual_output = evaluated.output
        self.actual_errors = evaluated.errors

        return self.expected_output == self.actual_output and self.expected_errors == self.actual_errors
