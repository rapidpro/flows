from __future__ import absolute_import, unicode_literals


class EvaluationError(Exception):
    """
    Exception class for errors during template/expression evaluation
    """
    def __init__(self, message, caused_by=None):
        Exception.__init__(self, message)
        self.caused_by = caused_by
