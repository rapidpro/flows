RapidPro Expressions
====================

Python implementation of the RapidPro expression and templating system

Usage
-----

```python
context = new EvaluationContext()
context.put_variable("name", "bob jones")

evaluator = Evaluator()
output, errors = evaluator.evaluate_template("Hi @(PROPER(name))", context, False)

assert output == "Hi Bob Jones"
assert len(errors) == 0
```

Development
-----------

If you make changes to the grammar file _Excellent.g4_ you need to generate new lexer and parser python modules

   1. Ensure you have the ANTLR command line tools installed.
   2. Run _gen_parser.sh_
   