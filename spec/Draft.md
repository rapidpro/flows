# Flow Specification

## Common Components

```
<uuid>          : 36 character hexadecimal UUID, e.g. "123e4567-e89b-12d3-a456-426655440000"
<language-code> : 3-digit ISO 639 language code, e.g. "eng"
<language-name> : ISO 639 language name, e.g. "English"
```

## Translated Text

```
<translated-text> : <text> | { <language-code>: <text> * }
```

Example: 

```
{ "eng": "Hello", "fre": "Bonjour" }
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
<send-action> : { "type": "send", "groups": [ <group-ref>* ], "contacts": [ <contact-ref>* ], "variables": [ <variable-ref>* ], "msg": <translated-text> }
```

#### Set Contact Language Action

```
<set-language-action> : { "type": "lang", "lang": <language-code>, "name": <language-name> }
```

