Excellent Templating in Java
============================

Java implementation of RapidPro's templating system

Usage
-----

```java
EvaluationContext context = new EvaluationContext();
context.putVariable("name", "bob jones");

Excellent.TemplateEvaluator evaluator = Excellent.getTemplateEvaluator();
EvaluatedTemplate output = evaluator.evaluateTemplate("Hi @(PROPER(name))", context, false);

assert output.getOutput() == "Hi Bob Jones";
assert output.getErrors().size() == 0;
```