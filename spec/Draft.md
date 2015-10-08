# Flow Specification Version 6

## Common Components

```
<text>          : string, e.g. "Testers"
<template>      : string containing expressions, e.g. "Hi @contact.name"
<int>           : 32 bit integer
<id>            : 32 bit integer containing an object identifier, e.g. 12345
<uuid>          : 36 character hexadecimal UUID, e.g. "123e4567-e89b-12d3-a456-426655440000"
<language-code> : 3-digit ISO 639 language code, e.g. "eng"
<language-name> : ISO 639 language name, e.g. "English"
```

### Translatable Text

```
<translatable-text> : <template> | { <language-code>: <template> * }
```

Example (translated): 

```js
{ "eng": "Hello @contact", "fre": "Bonjour @contact" }
```

Example (untranslated): 

```js
"Hello @contact"
```

### Object References

#### Contact References

```
<contact-ref> : { "id": <id>, "name": <text> }
```

Name is used for display purposes in the editor. Example:

```js
{ "id": "1234", "name": "Joe Flow" }
```

#### Group References

```
<group-ref> : { "id": <id>, "name": <text> }
```

Name is used for display purposes in the editor. Example:

```js
{ "id": 2345, "name": "Testers" }
```

#### Label References

```
<label-ref> :  { "id": <id>, "name": <text> }
```

Name is used for display purposes in the editor. Example:

```js
{ "id": 3456, "name": "Spam" }
```

#### Variable References

```
<variable-ref>: { "id": <template> }
```

The template should contain one of:

 * An expression which evaluates to a group name
 * An expression which evaluates to a phone number
 * The string `"@new_contact"`

Example:

```js
{ "id": "@contact.chw_phone" }
```

## Top-level Structure

```
<flow>: { "uuid": <uuid>,
          "version": <int>,
          "spec_version": 6,
          "flow_type": "F" | "M" | "S" | "V",
          "name": <text>,
          "definition": <definition> }
          
<definition>: { "base_language": <language-code>,
                "action_sets": [ <action-set>* ],
                "rule_sets": [ <rule-set>* ],
                "entry": <uuid> | null }
```

## Action Sets

```
<action-set> : { "uuid": <uuid>, 
                 "actions": [ <action>* ], 
                 "destination": <uuid> | null }
```

The destination must be one of:

 * The UUID of another action set
 * The UUID of a rule set
 * `null` to make this a terminal node

### Actions

```
<action> : <reply-action> 
         | <send-action> 
         | <email-action> 
         | <set-language-action> 
         | <save-to-contact-action> 
         | <add-to-groups-action> 
         | <remove-from-groups-action> 
         | <add-labels-action>
```

#### Reply Actions

```
<reply-action> : { "type": "reply",
                   "msg": <translatable-text> }
```

#### Send Actions

```
<send-action> : { "type": "send", 
                  "groups": [ <group-ref>* ], 
                  "contacts": [ <contact-ref>* ], 
                  "variables": [ <variable-ref>* ], 
                  "msg": <translatable-text> }
```

#### Email Actions

```
<email-action> : { "type": "email", 
                   "emails": [ <template>* ], 
                   "subject": <template>, 
                   "msg": <template> }
```

#### Set Contact Language Actions

```
<set-language-action> : { "type": "lang", 
                          "lang": <language-code>, 
                          "name": <language-name> }
```

#### Save To Contact Actions

```
<save-to-contact-action> : { "type": "save", 
                             "field": ????, 
                             "label": <text>,
                             "value": <template>}
```

#### Add To Groups Actions

```
<add-to-groups-action> : { "type": "add_group", 
                           "groups": [ (<group-ref> | <template>)* ] }
```

When a group is a template, only those beginning with "@" are evaluated, and others are assumed to be group names.

#### Remove From Groups Actions

```
<remove-from-groups-action> : { "type": "del_group", 
                                "groups": [ (<group-ref> | <template>)* ] }
```

When a group is a template, only those beginning with "@" are evaluated, and others are assumed to be group names.

#### Add Labels Actions

```
<add-labels-action> : { "type": "add_label", 
                        "labels": [ (<label-ref> | <template>)* ] }
```

When a label is a template, only those beginning with "@" are evaluated, and others are assumed to be label names.

## Rule Sets

```
<rule-set> : { "uuid": <uuid>,
               "ruleset_type": <rule-set-type>,
               "label": <text>,
               "rules": [ <rule>* ],
               "operand": <template> }
               
<rule-set-type> : "wait_message"
                | "wait_recording"
                | "wait_digit"
                | "wait_digits"
                | "webhook"
                | "flow_field"
                | "form_field"
                | "contact_field"
                | "expression"
               
<rule> : { "test": <test>,
           "category": <translatable-text>,
           "destination": <uuid> | null }
```

The destination must be one of:

 * The UUID of another rule set
 * The UUID of an action set
 * `null` to make this a terminal rule

### Tests

```
<test> : <true-test>
       | <false-test>
       | <and-test>
       | <or-test>
       | <not-empty-test>
       | <contains-test>
       | <contains-any-test>
       | <starts-with-test>
       | <regex-test>
       | <has-number-test>
       | <equal-test>
       | <less-than-test>
       | <less-than-or-equal-test>
       | <greater-than-test>
       | <greater-than-or-equal-test>
       | <between-test>
       | <has-date-test>
       | <date-equal-test>
       | <date-after-test>
       | <date-before-test>
       | <has-phone-test>
       | <has-state-test>
       | <has-district-test>
```

#### True Tests

Test that always returns true.

```
<true-test> : { "type": "true" }
```

#### False Tests

Test that always returns false.

```
<false-test> : { "type": "false" }
```

#### And Tests

Test which returns the AND'ed result of other tests.

```
<and-test> : { "type": "and", "tests": [ <test>* ] }
```

#### Or Tests

Test which returns the OR'ed result of other tests

```
<or-test> : { "type": "or", "tests": [ <test>* ] }
```

#### Not Empty Tests

Test that returns whether the input is non-empty (and non-blank).

```
<not-empty-test> : { "type": "not_empty" }
```

#### Contains Tests

Test that returns whether the text contains the given words.

```
<contains-test> : { "type": "contains", "test": <translatable-text> }
```

#### Contains Any Tests

Test that returns whether the text contains any of the given words.

```
<contains-any-test> : { "type": "contains_any", "test": <translatable-text> }
```

#### Starts With Tests

Test that returns whether the text starts with the given text.

```
<starts-with-test> : { "type": "starts", "test": <translatable-text> }
```

#### Regex Tests

Test that returns whether the input matches a regular expression.

```
<regex-test> : { "type": "regex", "test": <translatable-text> }
```

#### Has Number Tests

Test which returns whether input has a number.

```
<has-number-test> : { "type": "number", "test": <translatable-text> }
```

#### Equal Tests

Test which returns whether input is numerically equal a value.

```
<equal-test> : { "type": "eq", "test": <template> }
```

#### Less Than Tests

Test which returns whether input is numerically less than a value.

```
<less-than-test> : { "type": "lt", "test": <template> }
```

#### Less Than Or Equal Tests

Test which returns whether input is numerically less than or equal to a value

```
<less-than-or-equal-test> : { "type": "lte", "test": <template> }
```

#### Greater Than Tests

Test which returns whether input is numerically greater than a value.

```
<greater-than-test> : { "type": "gt", "test": <template> }
```

#### Greater Than Or Equal Tests

Test which returns whether input is numerically greater than or equal to a value.

```
<greater-than-or-equal-test> : { "type": "gte", "test": <template> }
```

#### Between Tests

Test which returns whether input is a number between two numbers (inclusive).

```
<between-test> : { "type": "between", "min": <template>, "max": <template> }
```

#### Has Date Tests

Test which returns whether input contains a valid date.

```
<has-date-test> : { "type": "date" }
```

#### Date Equal Tests

Test which returns whether input is a date equal to the given value.

```
<date-equal-test> : { "type": "date_equal", "test": <template> }
```

#### Date After Tests

Test which returns whether input is a date after the given value.

```
<date-after-test> : { "type": "date_after", "test": <template> }
```

#### Date Before Tests

Test which returns whether input is a date before the given value

```
<date-before-test> : { "type": "date_before", "test": <template> }
```

#### Has Phone Tests

Test that returns whether the text contains a valid phone number

```
<has-phone-test> : { "type": "phone" }
```

#### Has State Tests

Test that returns whether the text contains a valid state.

```
<has-state-test> : { "type": "state" }
```

#### Has District Tests

Test that returns whether the text contains a valid district in the given state.

```
<has-district-test> : { "type": "district", "test": <template> }
```
