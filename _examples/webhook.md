---
layout: examples
title: Webhook Flow
permalink: /examples/webhook/
---

# Webhook Example

An example of a flow that gets user input of an order number then calls a webhook to look up the result.

![flow example](../../images/webhook_example.png "Webhook Example")

```javascript
    {
      "version": 8,
      "base_language": "base",
      "action_sets": [
        {
          "y": 516,
          "x": 591,
          "destination": null,
          "uuid": "51c55df9-01fd-4bf8-81ab-4213c43159cf",
          "actions": [
            {
              "msg": {
                "base": "Uh oh @extra.name!  Our record indicate that your order for @extra.description was cancelled on @extra.cancel_date."
              },
              "type": "reply"
            }
          ]
        },
        {
          "y": 518,
          "x": 386,
          "destination": null,
          "uuid": "8652711b-1610-4ae0-bc4e-89cae6a00da7",
          "actions": [
            {
              "msg": {
                "base": "Hi @extra.name.  Hope you are patient because we haven't shipped your order for @extra.description yet.  We expect to ship it by @extra.ship_date though."
              },
              "type": "reply"
            }
          ]
        },
        {
          "y": 520,
          "x": 178,
          "destination": null,
          "uuid": "208279d4-f5ab-452d-8be6-3a36697c6348",
          "actions": [
            {
              "msg": {
                "base": "Great news @extra.name! We shipped your order for @extra.description on @extra.ship_date and we expect it will be delivered on @extra.delivery_date."
              },
              "type": "reply"
            }
          ]
        },
        {
          "y": 203,
          "x": 773,
          "destination": "c4d3c3b8-3f7a-4008-b730-697805432a8c",
          "uuid": "ea1c4340-a76c-42e5-accf-a13b9e380393",
          "actions": [
            {
              "msg": {
                "base": "Sorry that doesn't look like a valid order number.  Maybe try: CU001, CU002 or CU003?"
              },
              "type": "reply"
            }
          ]
        },
        {
          "y": 0,
          "x": 409,
          "destination": "c4d3c3b8-3f7a-4008-b730-697805432a8c",
          "uuid": "c266b998-a192-44a9-a63c-0110a04caf2c",
          "actions": [
            {
              "msg": {
                "base": "Thanks for contacting the ThriftShop order status system. Please send your order # and we'll help you in a jiffy!"
              },
              "type": "reply"
            }
          ]
        }
      ],
      "version": 7,
      "flow_type": "F",
      "entry": "c266b998-a192-44a9-a63c-0110a04caf2c",
      "rule_sets": [
        {
          "uuid": "c4d3c3b8-3f7a-4008-b730-697805432a8c",
          "webhook_action": null,
          "rules": [
            {
              "test": {
                "test": "true",
                "type": "true"
              },
              "category": {
                "base": "All Responses"
              },
              "destination": "23fac3f0-e7d0-41b6-ba68-2e0091af060c",
              "uuid": "f5011b22-b2aa-4612-895e-615c0233768a",
              "destination_type": "R"
            }
          ],
          "webhook": null,
          "ruleset_type": "wait_message",
          "label": "Lookup Response",
          "operand": "@step.value",
          "finished_key": null,
          "response_type": "",
          "y": 198,
          "x": 356,
          "config": {}
        },
        {
          "uuid": "14e80d17-12cc-43f4-8a9b-49dafd94bda8",
          "webhook_action": null,
          "rules": [
            {
              "test": {
                "test": "true",
                "type": "true"
              },
              "category": {
                "base": "All Responses"
              },
              "destination": null,
              "uuid": "b3e1844d-5db0-494c-8aeb-dfe293fbe1d7"
            }
          ],
          "webhook": null,
          "ruleset_type": "wait_message",
          "label": "Extra Comments",
          "operand": "@step.value",
          "finished_key": null,
          "response_type": "",
          "y": 1252,
          "x": 389,
          "config": {}
        },
        {
          "uuid": "41bcf1fb-7d13-464f-9c1f-9fd2c48b60f2",
          "webhook_action": null,
          "rules": [
            {
              "test": {
                "test": {
                  "base": "Shipped"
                },
                "type": "contains"
              },
              "category": {
                "base": "Shipped"
              },
              "destination": "208279d4-f5ab-452d-8be6-3a36697c6348",
              "uuid": "b66694f8-3c77-4ae0-bea4-466b30fa271a",
              "destination_type": "A"
            },
            {
              "test": {
                "test": {
                  "base": "Pending"
                },
                "type": "contains"
              },
              "category": {
                "base": "Pending"
              },
              "destination": "8652711b-1610-4ae0-bc4e-89cae6a00da7",
              "uuid": "3ae32b3f-dc12-4bed-a40a-7fe480343f71",
              "destination_type": "A"
            },
            {
              "test": {
                "test": {
                  "base": "Cancelled"
                },
                "type": "contains"
              },
              "category": {
                "base": "Cancelled"
              },
              "destination": "51c55df9-01fd-4bf8-81ab-4213c43159cf",
              "uuid": "6ab10a4f-c351-4104-b031-88b4f1804378",
              "destination_type": "A"
            },
            {
              "test": {
                "test": "true",
                "type": "true"
              },
              "category": {
                "base": "Other"
              },
              "destination": "ea1c4340-a76c-42e5-accf-a13b9e380393",
              "uuid": "f5011b22-b2aa-4612-895e-615c0233768a",
              "destination_type": "A"
            }
          ],
          "webhook": null,
          "ruleset_type": "expression",
          "label": "Lookup",
          "operand": "@extra.status",
          "finished_key": null,
          "response_type": "",
          "y": 398,
          "x": 356,
          "config": {}
        },
        {
          "uuid": "23fac3f0-e7d0-41b6-ba68-2e0091af060c",
          "webhook_action": null,
          "rules": [
            {
              "test": {
                "test": "true",
                "type": "true"
              },
              "category": {
                "base": "All Responses"
              },
              "destination": "41bcf1fb-7d13-464f-9c1f-9fd2c48b60f2",
              "uuid": "f5011b22-b2aa-4612-895e-615c0233768a",
              "destination_type": "R"
            }
          ],
          "webhook": "https://api.textit.in/demo/status/",
          "ruleset_type": "webhook",
          "label": "Lookup Webhook",
          "operand": "@step.value",
          "finished_key": null,
          "response_type": "",
          "y": 298,
          "x": 356,
          "config": {}
        }
      ],
      "metadata": {
        "uuid": null,
        "notes": [],
        "expires": 720,
        "name": "Sample Flow - Order Status Checker",
        "saved_on": "2015-10-15T22:05:54.098450Z",
        "id": 39970,
        "revision": 20
      }
    }
```
