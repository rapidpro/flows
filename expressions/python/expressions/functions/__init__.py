from __future__ import absolute_import, unicode_literals

import inspect

from expressions import EvaluationError


class FunctionManager(object):

    def __init__(self):
        self._functions = {}

    def add_library(self, library):
        """
        Adds functions from a library module
        :param library: the library module
        :return:
        """
        for fn in library.__dict__.copy().itervalues():
            # ignore imported methods and anything beginning __
            if inspect.isfunction(fn) and inspect.getmodule(fn) == library and not fn.__name__.startswith('__'):
                name = fn.__name__.lower()

                # strip preceding _ chars used to avoid conflicts with Java keywords
                if name.startswith('_'):
                    name = name[1:]

                self._functions[name] = fn

    def get_function(self, name):
        return self._functions.get(name.lower(), None)

    def invoke_function(self, ctx, name, arguments):
        """
        Invokes the given function
        :param ctx: the evaluation context
        :param name: the function name (case insensitive)
        :param arguments: the arguments to be passed to the function
        :return: the function return value
        """
        # find function with given name
        func = self.get_function(name)
        if func is None:
            raise EvaluationError("No such function " + name)

        args, varargs, keywords, defaults = inspect.getargspec(func)

        # build a mapping from argument names to their default values, if any:
        if defaults is None:
            defaults = {}
        else:
            defaulted_args = args[-len(defaults):]
            defaults = {name: val for name, val in zip(defaulted_args, defaults)}

        call_args = []
        passed_args = list(arguments)

        try:
            for arg in args:
                if arg == 'ctx':
                    call_args.append(ctx)
                elif passed_args:
                    call_args.append(passed_args.pop(0))
                elif arg in defaults:
                    call_args.append(defaults[arg])
                else:
                    raise TypeError("Missing argument in call to %s(): %s" % (func.__name__, arg))

            if varargs is not None:
                call_args.extend(passed_args)
                passed_args = []

            # any unused arguments?
            if passed_args:
                raise TypeError("Function %s cannot be called with %d arguments" % (func.__name__, len(arguments)))

            return func(*call_args)
        except Exception, e:
            pretty_args = []
            for arg in arguments:
                pretty_args.append(('"%s"' % arg) if isinstance(arg, basestring) else unicode(arg))
            pretty_args = ', '.join(pretty_args)
            raise EvaluationError("Error calling function %s with arguments %s" % (name, pretty_args), e)
