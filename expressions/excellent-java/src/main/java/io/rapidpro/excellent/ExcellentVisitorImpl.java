package io.rapidpro.excellent;

import java.math.BigDecimal;
import java.util.*;

/**
 * Visitor for our expression trees
 */
public class ExcellentVisitorImpl extends ExcellentBaseVisitor<Object> {

    private EvaluationContext m_evalContext;

    public ExcellentVisitorImpl(EvaluationContext context) {
        this.m_evalContext = context;
    }

    @Override
    public Object visitFunctionCall(ExcellentParser.FunctionCallContext ctx) {
        String funcName = ctx.NAME().getText();
        List<Object> parameters;
        if (ctx.parameters() != null) {
            parameters = (List<Object>) visit(ctx.parameters());
        } else {
            parameters = Collections.emptyList();
        }
        return EvaluationUtils.invokeFunction(funcName, parameters);
    }

    @Override
    public Object visitFunctionParameters(ExcellentParser.FunctionParametersContext ctx) {
        List<Object> paramValues = new ArrayList<>();
        for (ExcellentParser.ExpressionContext expression : ctx.expression()) {
            paramValues.add(visit(expression));
        }

        return paramValues;
    }

    @Override
    public Object visitAdditionOrSubtractionExpression(ExcellentParser.AdditionOrSubtractionExpressionContext ctx) {
        boolean addition = ctx.PLUS() != null;

        BigDecimal arg1 = Conversions.toDecimal(visit(ctx.expression(0)));
        BigDecimal arg2 = Conversions.toDecimal(visit(ctx.expression(1)));

        return addition ? arg1.add(arg2) : arg1.subtract(arg2);
    }

    @Override
    public Object visitMultiplicationOrDivisionExpression(ExcellentParser.MultiplicationOrDivisionExpressionContext ctx) {
        boolean multiplication = ctx.TIMES() != null;

        BigDecimal arg1 = Conversions.toDecimal(visit(ctx.expression(0)));
        BigDecimal arg2 = Conversions.toDecimal(visit(ctx.expression(1)));

        return multiplication ? arg1.multiply(arg2) : arg1.divide(arg2);
    }

    @Override
    public Object visitContextReference(ExcellentParser.ContextReferenceContext ctx) {
        String identifier = ctx.NAME().getText();
        return m_evalContext.read(identifier);

    }

    @Override
    public Object visitDecimalLiteral(ExcellentParser.DecimalLiteralContext ctx) {
        return new BigDecimal(ctx.DECIMAL().getText());
    }

    @Override
    public Object visitStringLiteral(ExcellentParser.StringLiteralContext ctx) {
        String raw = ctx.STRING().getText();
        String val = raw.substring(1, raw.length() - 1);  // remove enclosing quotes
        return val.replaceAll("\"\"", "\"");  // un-escape double quotes
    }
}
