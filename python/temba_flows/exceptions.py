from __future__ import absolute_import, unicode_literals


class FlowParseException(Exception):
    """
    Exception thrown when flow JSON is invalid
    """
    def __init__(self, message):
        super(FlowParseException, self).__init__(message)


class FlowRunException(Exception):
    """
    General exception class for any problems during flow execution
    """
    def __init__(self, message):
        super(FlowRunException, self).__init__(message)


class FlowLoopException(FlowRunException):
    """
    Exception thrown when runner detects an infinite loop - i.e. we return to a previously visited node without having
    waited for user input.
    """
    def __init__(self, path):
        super(FlowLoopException, self).__init__("Non-pausing loop detected after path:\n" + "\n".join(path))

        self.path = path
