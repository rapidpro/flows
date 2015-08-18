RapidPro Expressions
====================

Python implementation of the RapidPro expression and templating system

Usage
-----

```python
context = new EvaluationContext()
context.putVariable("name", "bob jones")

TemplateEvaluator evaluator = get_template_evaluator()
EvaluatedTemplate evaluated = evaluator.evaluate_template("Hi @(PROPER(name))", context, False)

assert evaluated.output == "Hi Bob Jones"
assert len(evaluated.errors) == 0
```