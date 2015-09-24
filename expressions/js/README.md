RapidPro Expressions
====================

Javascript support for parsing expressions and providing auto-complete context.

Usage
-----

```javascript
var parser = new excellent.Parser('@', ['channel', 'contact', 'date', 'extra', 'flow', 'step']);

describe("finding expressions", function() {
    it("find expressions with and without parentheses", function() {
        expect(parser.expressions('Hi @contact.name from @(flow.sender)')).toEqual([
            {start: 3, end: 16, text: '@contact.name', closed: false}, 
            {start: 22, end: 36, text: '@(flow.sender)', closed: true}
        ]);
    });
});
```

Development
-----------

If you have the Karma commandline tools installed, then just run:

```
karma start
```
   