from __future__ import absolute_import, unicode_literals

from antlr4 import *
from decimal import Decimal


class ExcellentVisitor(ParseTreeVisitor):

    def visitParse(self, ctx):
        return self.visitChildren(ctx)

    def visitFunctionCall(self, ctx):
        return self.visitChildren(ctx)

    def visitFunctionParameters(self, ctx):
        return self.visitChildren(ctx)

    def visitNegation(self, ctx):
        return self.visitChildren(ctx)

    def visitExponentExpression(self, ctx):
        return self.visitChildren(ctx)

    def visitMultiplicationOrDivisionExpression(self, ctx):
        return self.visitChildren(ctx)

    def visitAdditionOrSubtractionExpression(self, ctx):
        return self.visitChildren(ctx)

    def visitComparisonExpression(self, ctx):
        return self.visitChildren(ctx)

    def visitEqualityExpression(self, ctx):
        return self.visitChildren(ctx)

    def visitConcatenation(self, ctx):
        return self.visitChildren(ctx)

    def visitStringLiteral(self, ctx):
        return self.visitChildren(ctx)

    def visitDecimalLiteral(self, ctx):
        return Decimal(ctx.getText())

    def visitTrue(self, ctx):
        return True

    def visitFalse(self, ctx):
        return False

    def visitContextReference(self, ctx):
        return self.visitChildren(ctx)

    def visitParentheses(self, ctx):
        return self.visit(ctx.expression())
