package io.rapidpro.excellent;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;

/**
 *
 */
public class ExcellentEngine {

    private ExcellentEngine() {
    }

    public static ExcellentEngine getInstance() {
        return new ExcellentEngine();
    }

    public String evaluateTemplate(String template, Map<String, Object> context) {
        throw new NotImplementedException();
    }

    public Object evaluateExpression(String expression, Map<String, Object> context) {
        ExcellentLexer lexer = new ExcellentLexer(new ANTLRInputStream(expression));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExcellentParser parser = new ExcellentParser(tokens);
        ParseTree tree = parser.expression();
        ExcellentVisitor visitor = new ExcellentVisitorImpl(context);
        return visitor.visit(tree);
    }
}
