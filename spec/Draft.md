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
<contact-ref>: { "id": <id>, "name": <text> }
```

Name is used for display purposes in the editor. Example:

```js
{ "id": "1234", "name": "Joe Flow" }
```

#### Group References

```
<group-ref>: { "id": <id>, "name": <text> }
```

Name is used for display purposes in the editor. Example:

```js
{ "id": 2345, "name": "Testers" }
```

#### Label References

```
<label-ref>: { "id": <id>, "name": <text> }
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

The destination should be one of:

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
         | <add-label-action>
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

