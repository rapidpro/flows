package io.rapidpro.excellent;

import java.math.BigDecimal;
import java.util.*;

/**
 *
 */
public class ExcellentVisitorImpl extends ExcellentBaseVisitor<Object> {

    private Map<String, Object> m_context;

    public ExcellentVisitorImpl(Map<String, Object> context) {
        this.m_context = context;
    }

    @Override
    public Object visitFunctionCall(ExcellentParser.FunctionCallContext ctx) {
        String funcName = ctx.NAME().getText();
        List<Object> parameters;
        if (ctx.parameters() != null) {
            Object paramz = visit(ctx.parameters());
            parameters = (List<Object>) paramz;
        } else {
            parameters = Collections.emptyList();
        }
        return ExcellentUtils.invokeFunction(funcName, parameters);
    }

    @Override
    public Object visitParameterList(ExcellentParser.ParameterListContext ctx) {
        //if len(p) == 2:
        //    p[0] = [p[1]]
        //else:
        //    p[0] = p[1]
        //    p[0].append(p[3])

        return super.visitParameterList(ctx);
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

        // TODO implement dot notation etc

        if (!m_context.containsKey(identifier.toLowerCase())) {
            throw new EvaluationError("No item called '" + identifier + "' in the context");
        }

        return m_context.get(identifier.toLowerCase());

    }

    @Override
    public Object visitDecimalLiteral(ExcellentParser.DecimalLiteralContext ctx) {
        return new BigDecimal(ctx.DECIMAL().getText());
    }

    @Override
    public Object visitStringLiteral(ExcellentParser.StringLiteralContext ctx) {
        String raw = ctx.STRING().getText();
        String val = raw.substring(1, raw.length() - 2);  // remove enclosing quotes
        return val.replaceAll("\"\"", "\"");  // un-escape double quotes
    }
}
