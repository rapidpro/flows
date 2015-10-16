---
layout: examples
title: Simple Flow
permalink: /examples/simple/
---

# Simple Flow

A simple flow asking for the age of a contact.

![flow example](../../images/simple_example.png "Simple Example")

```javascript
    {
      "version": 8,
      "base_language": "eng",
      "action_sets": [
        {
          "y": 0,
          "x": 100,
          "destination": "fcde9e1b-7019-47fb-bbce-97c75401d650",
          "uuid": "8d255202-7a08-4d5b-aee3-4061e95dd41a",
          "actions": [
            {
              "msg": {
                "eng": "Hi there, what is your age?"
              },
              "type": "reply"
            }
          ]
        },
        {
          "y": 0,
          "x": 533,
          "destination": "fcde9e1b-7019-47fb-bbce-97c75401d650",
          "uuid": "714fdf53-fe56-431c-a508-e018692b0670",
          "actions": [
            {
              "msg": {
                "eng": "Sorry, that doesn't look like a valid number. Please enter an age between 1 and 120."
              },
              "type": "reply"
            }
          ]
        },
        {
          "y": 227,
          "x": 104,
          "destination": null,
          "uuid": "97950536-4970-48c1-af2f-9269ea045ee8",
          "actions": [
            {
              "field": "age",
              "type": "save",
              "value": "@flow.age",
              "label": "Age"
            },
            {
              "msg": {
                "eng": "Great, your age is @contact.age"
              },
              "type": "reply"
            }
          ]
        }
      ],
      "version": 7,
      "flow_type": "F",
      "entry": "8d255202-7a08-4d5b-aee3-4061e95dd41a",
      "rule_sets": [
        {
          "uuid": "fcde9e1b-7019-47fb-bbce-97c75401d650",
          "webhook_action": null,
          "rules": [
            {
              "test": {
                "max": "120",
                "type": "between",
                "min": "1"
              },
              "category": {
                "eng": "1 - 120"
              },
              "destination": "97950536-4970-48c1-af2f-9269ea045ee8",
              "uuid": "4e9fe94e-1704-4f4e-bee9-0526d42ff58b",
              "destination_type": "A"
            },
            {
              "test": {
                "test": "true",
                "type": "true"
              },
              "category": {
                "eng": "Other"
              },
              "destination": "714fdf53-fe56-431c-a508-e018692b0670",
              "uuid": "562307fb-d1a6-4d6a-99ee-5ea799391ccc",
              "destination_type": "A"
            }
          ],
          "webhook": null,
          "ruleset_type": "wait_message",
          "label": "Age",
          "operand": "@step.value",
          "finished_key": null,
          "response_type": "",
          "y": 93,
          "x": 241,
          "config": {}
        }
      ],
      "metadata": {
        "expires": 10080,
        "revision": 15,
        "id": 18902,
        "name": "Example Flow",
        "saved_on": "2015-10-15T20:05:52.156366Z"
      }
}
```
