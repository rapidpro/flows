from __future__ import absolute_import, unicode_literals


class TemplateEvaluator:
    def __init__(self):
        pass

    def evaluate_template(self, template, context, url_encode=False):
        raise NotImplementedError()


class Excellent:
    evaluator = TemplateEvaluator()

    @classmethod
    def get_template_evaluator(cls):
        return cls.evaluator
