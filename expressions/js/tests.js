var parser = new excellent.Parser('@', ['channel', 'contact', 'date', 'extra', 'flow', 'step']);

describe("finding expressions", function() {
    it("find expressions with and without parentheses", function() {
        expect(parser.expressions('Hi @contact.name from @(flow.sender)')).toEqual([
            {start: 3, end: 16, text: '@contact.name', closed: false}, 
            {start: 22, end: 36, text: '@(flow.sender)', closed: true}
        ]);
    });

    it("ignores invalid top levels", function() {
        expect(parser.expressions('Hi @contact.name from @nyaruka')).toEqual([
            {start: 3, end: 16, text: '@contact.name', closed: false}
        ]);
    });

    it("ignore parentheses inside string literals", function() {
        expect(parser.expressions('Hi @(LEN("))"))')).toEqual([
            {start: 3, end: 15, text: '@(LEN("))"))', closed: true}
        ]);
    });
});

describe("get expression context", function() {
    it("finds context for expression without parentheses", function() {
        expect(parser.expressionContext('Hi @contact.na')).toBe('contact.na');
    });

    it("finds context for expression with parentheses", function() {
        expect(parser.expressionContext('Hi @contact.name from @(flow.sen')).toBe('(flow.sen');
    });

    it("don't include a closed expression", function() {
        expect(parser.expressionContext('Hi @contact.name from @(flow.sender)')).toBeNull();
    });
});

describe("get auto-complete context", function() {
    it("finds context for expression without parentheses", function() {
        expect(parser.autoCompleteContext('Hi @contact.na')).toBe('contact.na');
    });

    it("finds context for expression with parentheses", function() {
        expect(parser.autoCompleteContext('Hi @contact.name from @(flow.sen')).toBe('flow.sen');
    });

    it("no context if last typed thing can't be an identifier", function() {
        expect(parser.autoCompleteContext('Hi @contact.name from @(flow.sender + ')).toBeNull();
    });
});
