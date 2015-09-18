# Generated from Excellent.g4 by ANTLR 4.5.1
# encoding: utf-8
from __future__ import print_function
from antlr4 import *
from io import StringIO

def serializedATN():
    with StringIO() as buf:
        buf.write(u"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3")
        buf.write(u"\30D\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\3\2\3\2\3\2\3\3")
        buf.write(u"\3\3\3\3\3\3\3\3\3\3\5\3\24\n\3\3\3\3\3\3\3\3\3\3\3\3")
        buf.write(u"\3\3\3\3\3\3\3\3\3\3\3\5\3!\n\3\3\3\3\3\3\3\3\3\3\3\3")
        buf.write(u"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\7")
        buf.write(u"\3\65\n\3\f\3\16\38\13\3\3\4\3\4\3\5\3\5\3\5\7\5?\n\5")
        buf.write(u"\f\5\16\5B\13\5\3\5\2\3\4\6\2\4\6\b\2\7\3\2\b\t\3\2\6")
        buf.write(u"\7\3\2\r\20\3\2\13\f\3\2\24\26N\2\n\3\2\2\2\4 \3\2\2")
        buf.write(u"\2\69\3\2\2\2\b;\3\2\2\2\n\13\5\4\3\2\13\f\7\2\2\3\f")
        buf.write(u"\3\3\2\2\2\r\16\b\3\1\2\16\17\7\7\2\2\17!\5\4\3\17\20")
        buf.write(u"\21\5\6\4\2\21\23\7\4\2\2\22\24\5\b\5\2\23\22\3\2\2\2")
        buf.write(u"\23\24\3\2\2\2\24\25\3\2\2\2\25\26\7\5\2\2\26!\3\2\2")
        buf.write(u"\2\27!\7\23\2\2\30!\7\22\2\2\31!\7\24\2\2\32!\7\25\2")
        buf.write(u"\2\33!\7\26\2\2\34\35\7\4\2\2\35\36\5\4\3\2\36\37\7\5")
        buf.write(u"\2\2\37!\3\2\2\2 \r\3\2\2\2 \20\3\2\2\2 \27\3\2\2\2 ")
        buf.write(u"\30\3\2\2\2 \31\3\2\2\2 \32\3\2\2\2 \33\3\2\2\2 \34\3")
        buf.write(u"\2\2\2!\66\3\2\2\2\"#\f\16\2\2#$\7\n\2\2$\65\5\4\3\17")
        buf.write(u"%&\f\r\2\2&\'\t\2\2\2\'\65\5\4\3\16()\f\f\2\2)*\t\3\2")
        buf.write(u"\2*\65\5\4\3\r+,\f\13\2\2,-\t\4\2\2-\65\5\4\3\f./\f\n")
        buf.write(u"\2\2/\60\t\5\2\2\60\65\5\4\3\13\61\62\f\t\2\2\62\63\7")
        buf.write(u"\21\2\2\63\65\5\4\3\n\64\"\3\2\2\2\64%\3\2\2\2\64(\3")
        buf.write(u"\2\2\2\64+\3\2\2\2\64.\3\2\2\2\64\61\3\2\2\2\658\3\2")
        buf.write(u"\2\2\66\64\3\2\2\2\66\67\3\2\2\2\67\5\3\2\2\28\66\3\2")
        buf.write(u"\2\29:\t\6\2\2:\7\3\2\2\2;@\5\4\3\2<=\7\3\2\2=?\5\4\3")
        buf.write(u"\2><\3\2\2\2?B\3\2\2\2@>\3\2\2\2@A\3\2\2\2A\t\3\2\2\2")
        buf.write(u"B@\3\2\2\2\7\23 \64\66@")
        return buf.getvalue()


class ExcellentParser ( Parser ):

    grammarFileName = "Excellent.g4"

    atn = ATNDeserializer().deserialize(serializedATN())

    decisionsToDFA = [ DFA(ds, i) for i, ds in enumerate(atn.decisionToState) ]

    sharedContextCache = PredictionContextCache()

    literalNames = [ u"<INVALID>", u"','", u"'('", u"')'", u"'+'", u"'-'", 
                     u"'*'", u"'/'", u"'^'", u"'='", u"'<>'", u"'<='", u"'<'", 
                     u"'>='", u"'>'", u"'&'" ]

    symbolicNames = [ u"<INVALID>", u"COMMA", u"LPAREN", u"RPAREN", u"PLUS", 
                      u"MINUS", u"TIMES", u"DIVIDE", u"EXPONENT", u"EQ", 
                      u"NEQ", u"LTE", u"LT", u"GTE", u"GT", u"AMPERSAND", 
                      u"DECIMAL", u"STRING", u"TRUE", u"FALSE", u"NAME", 
                      u"WS", u"ERROR" ]

    RULE_parse = 0
    RULE_expression = 1
    RULE_fnname = 2
    RULE_parameters = 3

    ruleNames =  [ u"parse", u"expression", u"fnname", u"parameters" ]

    EOF = Token.EOF
    COMMA=1
    LPAREN=2
    RPAREN=3
    PLUS=4
    MINUS=5
    TIMES=6
    DIVIDE=7
    EXPONENT=8
    EQ=9
    NEQ=10
    LTE=11
    LT=12
    GTE=13
    GT=14
    AMPERSAND=15
    DECIMAL=16
    STRING=17
    TRUE=18
    FALSE=19
    NAME=20
    WS=21
    ERROR=22

    def __init__(self, input):
        super(ExcellentParser, self).__init__(input)
        self.checkVersion("4.5.1")
        self._interp = ParserATNSimulator(self, self.atn, self.decisionsToDFA, self.sharedContextCache)
        self._predicates = None



    class ParseContext(ParserRuleContext):

        def __init__(self, parser, parent=None, invokingState=-1):
            super(ExcellentParser.ParseContext, self).__init__(parent, invokingState)
            self.parser = parser

        def expression(self):
            return self.getTypedRuleContext(ExcellentParser.ExpressionContext,0)


        def EOF(self):
            return self.getToken(ExcellentParser.EOF, 0)

        def getRuleIndex(self):
            return ExcellentParser.RULE_parse

        def accept(self, visitor):
            if hasattr(visitor, "visitParse"):
                return visitor.visitParse(self)
            else:
                return visitor.visitChildren(self)




    def parse(self):

        localctx = ExcellentParser.ParseContext(self, self._ctx, self.state)
        self.enterRule(localctx, 0, self.RULE_parse)
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 8
            self.expression(0)
            self.state = 9
            self.match(ExcellentParser.EOF)
        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx

    class ExpressionContext(ParserRuleContext):

        def __init__(self, parser, parent=None, invokingState=-1):
            super(ExcellentParser.ExpressionContext, self).__init__(parent, invokingState)
            self.parser = parser


        def getRuleIndex(self):
            return ExcellentParser.RULE_expression

     
        def copyFrom(self, ctx):
            super(ExcellentParser.ExpressionContext, self).copyFrom(ctx)


    class DecimalLiteralContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.DecimalLiteralContext, self).__init__(parser)
            self.copyFrom(ctx)

        def DECIMAL(self):
            return self.getToken(ExcellentParser.DECIMAL, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitDecimalLiteral"):
                return visitor.visitDecimalLiteral(self)
            else:
                return visitor.visitChildren(self)


    class ParenthesesContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.ParenthesesContext, self).__init__(parser)
            self.copyFrom(ctx)

        def LPAREN(self):
            return self.getToken(ExcellentParser.LPAREN, 0)
        def expression(self):
            return self.getTypedRuleContext(ExcellentParser.ExpressionContext,0)

        def RPAREN(self):
            return self.getToken(ExcellentParser.RPAREN, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitParentheses"):
                return visitor.visitParentheses(self)
            else:
                return visitor.visitChildren(self)


    class NegationContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.NegationContext, self).__init__(parser)
            self.copyFrom(ctx)

        def MINUS(self):
            return self.getToken(ExcellentParser.MINUS, 0)
        def expression(self):
            return self.getTypedRuleContext(ExcellentParser.ExpressionContext,0)


        def accept(self, visitor):
            if hasattr(visitor, "visitNegation"):
                return visitor.visitNegation(self)
            else:
                return visitor.visitChildren(self)


    class ExponentExpressionContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.ExponentExpressionContext, self).__init__(parser)
            self.copyFrom(ctx)

        def expression(self, i=None):
            if i is None:
                return self.getTypedRuleContexts(ExcellentParser.ExpressionContext)
            else:
                return self.getTypedRuleContext(ExcellentParser.ExpressionContext,i)

        def EXPONENT(self):
            return self.getToken(ExcellentParser.EXPONENT, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitExponentExpression"):
                return visitor.visitExponentExpression(self)
            else:
                return visitor.visitChildren(self)


    class AdditionOrSubtractionExpressionContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.AdditionOrSubtractionExpressionContext, self).__init__(parser)
            self.copyFrom(ctx)

        def expression(self, i=None):
            if i is None:
                return self.getTypedRuleContexts(ExcellentParser.ExpressionContext)
            else:
                return self.getTypedRuleContext(ExcellentParser.ExpressionContext,i)

        def PLUS(self):
            return self.getToken(ExcellentParser.PLUS, 0)
        def MINUS(self):
            return self.getToken(ExcellentParser.MINUS, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitAdditionOrSubtractionExpression"):
                return visitor.visitAdditionOrSubtractionExpression(self)
            else:
                return visitor.visitChildren(self)


    class FalseContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.FalseContext, self).__init__(parser)
            self.copyFrom(ctx)

        def FALSE(self):
            return self.getToken(ExcellentParser.FALSE, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitFalse"):
                return visitor.visitFalse(self)
            else:
                return visitor.visitChildren(self)


    class ContextReferenceContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.ContextReferenceContext, self).__init__(parser)
            self.copyFrom(ctx)

        def NAME(self):
            return self.getToken(ExcellentParser.NAME, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitContextReference"):
                return visitor.visitContextReference(self)
            else:
                return visitor.visitChildren(self)


    class ComparisonExpressionContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.ComparisonExpressionContext, self).__init__(parser)
            self.copyFrom(ctx)

        def expression(self, i=None):
            if i is None:
                return self.getTypedRuleContexts(ExcellentParser.ExpressionContext)
            else:
                return self.getTypedRuleContext(ExcellentParser.ExpressionContext,i)

        def LTE(self):
            return self.getToken(ExcellentParser.LTE, 0)
        def LT(self):
            return self.getToken(ExcellentParser.LT, 0)
        def GTE(self):
            return self.getToken(ExcellentParser.GTE, 0)
        def GT(self):
            return self.getToken(ExcellentParser.GT, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitComparisonExpression"):
                return visitor.visitComparisonExpression(self)
            else:
                return visitor.visitChildren(self)


    class ConcatenationContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.ConcatenationContext, self).__init__(parser)
            self.copyFrom(ctx)

        def expression(self, i=None):
            if i is None:
                return self.getTypedRuleContexts(ExcellentParser.ExpressionContext)
            else:
                return self.getTypedRuleContext(ExcellentParser.ExpressionContext,i)

        def AMPERSAND(self):
            return self.getToken(ExcellentParser.AMPERSAND, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitConcatenation"):
                return visitor.visitConcatenation(self)
            else:
                return visitor.visitChildren(self)


    class StringLiteralContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.StringLiteralContext, self).__init__(parser)
            self.copyFrom(ctx)

        def STRING(self):
            return self.getToken(ExcellentParser.STRING, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitStringLiteral"):
                return visitor.visitStringLiteral(self)
            else:
                return visitor.visitChildren(self)


    class FunctionCallContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.FunctionCallContext, self).__init__(parser)
            self.copyFrom(ctx)

        def fnname(self):
            return self.getTypedRuleContext(ExcellentParser.FnnameContext,0)

        def LPAREN(self):
            return self.getToken(ExcellentParser.LPAREN, 0)
        def RPAREN(self):
            return self.getToken(ExcellentParser.RPAREN, 0)
        def parameters(self):
            return self.getTypedRuleContext(ExcellentParser.ParametersContext,0)


        def accept(self, visitor):
            if hasattr(visitor, "visitFunctionCall"):
                return visitor.visitFunctionCall(self)
            else:
                return visitor.visitChildren(self)


    class TrueContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.TrueContext, self).__init__(parser)
            self.copyFrom(ctx)

        def TRUE(self):
            return self.getToken(ExcellentParser.TRUE, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitTrue"):
                return visitor.visitTrue(self)
            else:
                return visitor.visitChildren(self)


    class EqualityExpressionContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.EqualityExpressionContext, self).__init__(parser)
            self.copyFrom(ctx)

        def expression(self, i=None):
            if i is None:
                return self.getTypedRuleContexts(ExcellentParser.ExpressionContext)
            else:
                return self.getTypedRuleContext(ExcellentParser.ExpressionContext,i)

        def EQ(self):
            return self.getToken(ExcellentParser.EQ, 0)
        def NEQ(self):
            return self.getToken(ExcellentParser.NEQ, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitEqualityExpression"):
                return visitor.visitEqualityExpression(self)
            else:
                return visitor.visitChildren(self)


    class MultiplicationOrDivisionExpressionContext(ExpressionContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ExpressionContext)
            super(ExcellentParser.MultiplicationOrDivisionExpressionContext, self).__init__(parser)
            self.copyFrom(ctx)

        def expression(self, i=None):
            if i is None:
                return self.getTypedRuleContexts(ExcellentParser.ExpressionContext)
            else:
                return self.getTypedRuleContext(ExcellentParser.ExpressionContext,i)

        def TIMES(self):
            return self.getToken(ExcellentParser.TIMES, 0)
        def DIVIDE(self):
            return self.getToken(ExcellentParser.DIVIDE, 0)

        def accept(self, visitor):
            if hasattr(visitor, "visitMultiplicationOrDivisionExpression"):
                return visitor.visitMultiplicationOrDivisionExpression(self)
            else:
                return visitor.visitChildren(self)



    def expression(self, _p=0):
        _parentctx = self._ctx
        _parentState = self.state
        localctx = ExcellentParser.ExpressionContext(self, self._ctx, _parentState)
        _prevctx = localctx
        _startState = 2
        self.enterRecursionRule(localctx, 2, self.RULE_expression, _p)
        self._la = 0 # Token type
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 30
            la_ = self._interp.adaptivePredict(self._input,1,self._ctx)
            if la_ == 1:
                localctx = ExcellentParser.NegationContext(self, localctx)
                self._ctx = localctx
                _prevctx = localctx

                self.state = 12
                self.match(ExcellentParser.MINUS)
                self.state = 13
                self.expression(13)
                pass

            elif la_ == 2:
                localctx = ExcellentParser.FunctionCallContext(self, localctx)
                self._ctx = localctx
                _prevctx = localctx
                self.state = 14
                self.fnname()
                self.state = 15
                self.match(ExcellentParser.LPAREN)
                self.state = 17
                _la = self._input.LA(1)
                if (((_la) & ~0x3f) == 0 and ((1 << _la) & ((1 << ExcellentParser.LPAREN) | (1 << ExcellentParser.MINUS) | (1 << ExcellentParser.DECIMAL) | (1 << ExcellentParser.STRING) | (1 << ExcellentParser.TRUE) | (1 << ExcellentParser.FALSE) | (1 << ExcellentParser.NAME))) != 0):
                    self.state = 16
                    self.parameters()


                self.state = 19
                self.match(ExcellentParser.RPAREN)
                pass

            elif la_ == 3:
                localctx = ExcellentParser.StringLiteralContext(self, localctx)
                self._ctx = localctx
                _prevctx = localctx
                self.state = 21
                self.match(ExcellentParser.STRING)
                pass

            elif la_ == 4:
                localctx = ExcellentParser.DecimalLiteralContext(self, localctx)
                self._ctx = localctx
                _prevctx = localctx
                self.state = 22
                self.match(ExcellentParser.DECIMAL)
                pass

            elif la_ == 5:
                localctx = ExcellentParser.TrueContext(self, localctx)
                self._ctx = localctx
                _prevctx = localctx
                self.state = 23
                self.match(ExcellentParser.TRUE)
                pass

            elif la_ == 6:
                localctx = ExcellentParser.FalseContext(self, localctx)
                self._ctx = localctx
                _prevctx = localctx
                self.state = 24
                self.match(ExcellentParser.FALSE)
                pass

            elif la_ == 7:
                localctx = ExcellentParser.ContextReferenceContext(self, localctx)
                self._ctx = localctx
                _prevctx = localctx
                self.state = 25
                self.match(ExcellentParser.NAME)
                pass

            elif la_ == 8:
                localctx = ExcellentParser.ParenthesesContext(self, localctx)
                self._ctx = localctx
                _prevctx = localctx
                self.state = 26
                self.match(ExcellentParser.LPAREN)
                self.state = 27
                self.expression(0)
                self.state = 28
                self.match(ExcellentParser.RPAREN)
                pass


            self._ctx.stop = self._input.LT(-1)
            self.state = 52
            self._errHandler.sync(self)
            _alt = self._interp.adaptivePredict(self._input,3,self._ctx)
            while _alt!=2 and _alt!=ATN.INVALID_ALT_NUMBER:
                if _alt==1:
                    if self._parseListeners is not None:
                        self.triggerExitRuleEvent()
                    _prevctx = localctx
                    self.state = 50
                    la_ = self._interp.adaptivePredict(self._input,2,self._ctx)
                    if la_ == 1:
                        localctx = ExcellentParser.ExponentExpressionContext(self, ExcellentParser.ExpressionContext(self, _parentctx, _parentState))
                        self.pushNewRecursionContext(localctx, _startState, self.RULE_expression)
                        self.state = 32
                        if not self.precpred(self._ctx, 12):
                            from antlr4.error.Errors import FailedPredicateException
                            raise FailedPredicateException(self, "self.precpred(self._ctx, 12)")
                        self.state = 33
                        self.match(ExcellentParser.EXPONENT)
                        self.state = 34
                        self.expression(13)
                        pass

                    elif la_ == 2:
                        localctx = ExcellentParser.MultiplicationOrDivisionExpressionContext(self, ExcellentParser.ExpressionContext(self, _parentctx, _parentState))
                        self.pushNewRecursionContext(localctx, _startState, self.RULE_expression)
                        self.state = 35
                        if not self.precpred(self._ctx, 11):
                            from antlr4.error.Errors import FailedPredicateException
                            raise FailedPredicateException(self, "self.precpred(self._ctx, 11)")
                        self.state = 36
                        _la = self._input.LA(1)
                        if not(_la==ExcellentParser.TIMES or _la==ExcellentParser.DIVIDE):
                            self._errHandler.recoverInline(self)
                        else:
                            self.consume()
                        self.state = 37
                        self.expression(12)
                        pass

                    elif la_ == 3:
                        localctx = ExcellentParser.AdditionOrSubtractionExpressionContext(self, ExcellentParser.ExpressionContext(self, _parentctx, _parentState))
                        self.pushNewRecursionContext(localctx, _startState, self.RULE_expression)
                        self.state = 38
                        if not self.precpred(self._ctx, 10):
                            from antlr4.error.Errors import FailedPredicateException
                            raise FailedPredicateException(self, "self.precpred(self._ctx, 10)")
                        self.state = 39
                        _la = self._input.LA(1)
                        if not(_la==ExcellentParser.PLUS or _la==ExcellentParser.MINUS):
                            self._errHandler.recoverInline(self)
                        else:
                            self.consume()
                        self.state = 40
                        self.expression(11)
                        pass

                    elif la_ == 4:
                        localctx = ExcellentParser.ComparisonExpressionContext(self, ExcellentParser.ExpressionContext(self, _parentctx, _parentState))
                        self.pushNewRecursionContext(localctx, _startState, self.RULE_expression)
                        self.state = 41
                        if not self.precpred(self._ctx, 9):
                            from antlr4.error.Errors import FailedPredicateException
                            raise FailedPredicateException(self, "self.precpred(self._ctx, 9)")
                        self.state = 42
                        _la = self._input.LA(1)
                        if not((((_la) & ~0x3f) == 0 and ((1 << _la) & ((1 << ExcellentParser.LTE) | (1 << ExcellentParser.LT) | (1 << ExcellentParser.GTE) | (1 << ExcellentParser.GT))) != 0)):
                            self._errHandler.recoverInline(self)
                        else:
                            self.consume()
                        self.state = 43
                        self.expression(10)
                        pass

                    elif la_ == 5:
                        localctx = ExcellentParser.EqualityExpressionContext(self, ExcellentParser.ExpressionContext(self, _parentctx, _parentState))
                        self.pushNewRecursionContext(localctx, _startState, self.RULE_expression)
                        self.state = 44
                        if not self.precpred(self._ctx, 8):
                            from antlr4.error.Errors import FailedPredicateException
                            raise FailedPredicateException(self, "self.precpred(self._ctx, 8)")
                        self.state = 45
                        _la = self._input.LA(1)
                        if not(_la==ExcellentParser.EQ or _la==ExcellentParser.NEQ):
                            self._errHandler.recoverInline(self)
                        else:
                            self.consume()
                        self.state = 46
                        self.expression(9)
                        pass

                    elif la_ == 6:
                        localctx = ExcellentParser.ConcatenationContext(self, ExcellentParser.ExpressionContext(self, _parentctx, _parentState))
                        self.pushNewRecursionContext(localctx, _startState, self.RULE_expression)
                        self.state = 47
                        if not self.precpred(self._ctx, 7):
                            from antlr4.error.Errors import FailedPredicateException
                            raise FailedPredicateException(self, "self.precpred(self._ctx, 7)")
                        self.state = 48
                        self.match(ExcellentParser.AMPERSAND)
                        self.state = 49
                        self.expression(8)
                        pass

             
                self.state = 54
                self._errHandler.sync(self)
                _alt = self._interp.adaptivePredict(self._input,3,self._ctx)

        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.unrollRecursionContexts(_parentctx)
        return localctx

    class FnnameContext(ParserRuleContext):

        def __init__(self, parser, parent=None, invokingState=-1):
            super(ExcellentParser.FnnameContext, self).__init__(parent, invokingState)
            self.parser = parser

        def NAME(self):
            return self.getToken(ExcellentParser.NAME, 0)

        def TRUE(self):
            return self.getToken(ExcellentParser.TRUE, 0)

        def FALSE(self):
            return self.getToken(ExcellentParser.FALSE, 0)

        def getRuleIndex(self):
            return ExcellentParser.RULE_fnname

        def accept(self, visitor):
            if hasattr(visitor, "visitFnname"):
                return visitor.visitFnname(self)
            else:
                return visitor.visitChildren(self)




    def fnname(self):

        localctx = ExcellentParser.FnnameContext(self, self._ctx, self.state)
        self.enterRule(localctx, 4, self.RULE_fnname)
        self._la = 0 # Token type
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 55
            _la = self._input.LA(1)
            if not((((_la) & ~0x3f) == 0 and ((1 << _la) & ((1 << ExcellentParser.TRUE) | (1 << ExcellentParser.FALSE) | (1 << ExcellentParser.NAME))) != 0)):
                self._errHandler.recoverInline(self)
            else:
                self.consume()
        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx

    class ParametersContext(ParserRuleContext):

        def __init__(self, parser, parent=None, invokingState=-1):
            super(ExcellentParser.ParametersContext, self).__init__(parent, invokingState)
            self.parser = parser


        def getRuleIndex(self):
            return ExcellentParser.RULE_parameters

     
        def copyFrom(self, ctx):
            super(ExcellentParser.ParametersContext, self).copyFrom(ctx)



    class FunctionParametersContext(ParametersContext):

        def __init__(self, parser, ctx): # actually a ExcellentParser.ParametersContext)
            super(ExcellentParser.FunctionParametersContext, self).__init__(parser)
            self.copyFrom(ctx)

        def expression(self, i=None):
            if i is None:
                return self.getTypedRuleContexts(ExcellentParser.ExpressionContext)
            else:
                return self.getTypedRuleContext(ExcellentParser.ExpressionContext,i)

        def COMMA(self, i=None):
            if i is None:
                return self.getTokens(ExcellentParser.COMMA)
            else:
                return self.getToken(ExcellentParser.COMMA, i)

        def accept(self, visitor):
            if hasattr(visitor, "visitFunctionParameters"):
                return visitor.visitFunctionParameters(self)
            else:
                return visitor.visitChildren(self)



    def parameters(self):

        localctx = ExcellentParser.ParametersContext(self, self._ctx, self.state)
        self.enterRule(localctx, 6, self.RULE_parameters)
        self._la = 0 # Token type
        try:
            localctx = ExcellentParser.FunctionParametersContext(self, localctx)
            self.enterOuterAlt(localctx, 1)
            self.state = 57
            self.expression(0)
            self.state = 62
            self._errHandler.sync(self)
            _la = self._input.LA(1)
            while _la==ExcellentParser.COMMA:
                self.state = 58
                self.match(ExcellentParser.COMMA)
                self.state = 59
                self.expression(0)
                self.state = 64
                self._errHandler.sync(self)
                _la = self._input.LA(1)

        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx



    def sempred(self, localctx, ruleIndex, predIndex):
        if self._predicates == None:
            self._predicates = dict()
        self._predicates[1] = self.expression_sempred
        pred = self._predicates.get(ruleIndex, None)
        if pred is None:
            raise Exception("No predicate with index:" + str(ruleIndex))
        else:
            return pred(localctx, predIndex)

    def expression_sempred(self, localctx, predIndex):
            if predIndex == 0:
                return self.precpred(self._ctx, 12)
         

            if predIndex == 1:
                return self.precpred(self._ctx, 11)
         

            if predIndex == 2:
                return self.precpred(self._ctx, 10)
         

            if predIndex == 3:
                return self.precpred(self._ctx, 9)
         

            if predIndex == 4:
                return self.precpred(self._ctx, 8)
         

            if predIndex == 5:
                return self.precpred(self._ctx, 7)
         




