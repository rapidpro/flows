RapidPro Expressions
====================

Java implementation of the RapidPro expression and templating system

Usage
-----

```java
EvaluationContext context = new EvaluationContext();
context.putVariable("name", "bob jones");

Evaluator evaluator = new EvaluatorBuilder().build();
EvaluatedTemplate output = evaluator.evaluateTemplate("Hi @(PROPER(name))", context, false);

assert output.getOutput() == "Hi Bob Jones";
assert output.getErrors().size() == 0;
```