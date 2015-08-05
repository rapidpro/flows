Excellent Templating
====================

Templating engine for RapidPro, loosely based on Excel formulae.

Usage
-----

```java
ExcellentEngine engine = ExcellentEngine.getInstance();
EvaluationContext context = new EvaluationContext();
context.put("name", "bob jones");
String output = engine.evaluateTemplate("Hi @(PROPER(name))", context);
assert output == "Hi Bob Jones";
```