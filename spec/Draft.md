# Flow Specification

## Common Components

```
<text>          : string, e.g. "Testers"
<template>      : string containing expressions, e.g. "Hi @contact.name"
<id>            : 32 bit integer, e.g. 12345
<uuid>          : 36 character hexadecimal UUID, e.g. "123e4567-e89b-12d3-a456-426655440000"
<language-code> : 3-digit ISO 639 language code, e.g. "eng"
<language-name> : ISO 639 language name, e.g. "English"
```

### Translatable Text

```
<translatable-text> : <template> | { <language-code>: <template> * }
```

Example: 

```
{ "eng": "Hello @contact", "fre": "Bonjour @contact" }
```

### Object References

#### Contact Reference

```
<contact-ref>: { "id": <id>, "name": <text> }
```

#### Group Reference

```
<group-ref>: { "id": <id>, "name": <text> }
```

#### Variable Reference

```
<variable-ref>: { "id": <template> }
```

## Action Sets

```
<action-set> : { "uuid": <uuid>, "actions": [ <action>* ], "destination": <uuid> }
```

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

#### Send Action

```
<send-action> : { "type": "send", 
                  "groups": [ <group-ref>* ], 
                  "contacts": [ <contact-ref>* ], 
                  "variables": [ <variable-ref>* ], 
                  "msg": <translatable-text> }
```

#### Set Contact Language Action

```
<set-language-action> : { "type": "lang", 
                          "lang": <language-code>, 
                          "name": <language-name> }
```

