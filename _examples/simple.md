---
layout: examples
title: Simple Flow
permalink: /examples/simple/
---

# Simple Flow

Here we provide a few examples of flows, inputs and outputs to help implementors in grasping the concepts involved.

```javascript
{
  "base_language": "fre",
  "action_sets": [
    {
      "y": 4,
      "x": 119,
      "destination": "a86b6a47-a400-4639-b8c7-1df8164eda67",
      "uuid": "bcbfdc41-232a-4549-b396-e23bd883646b",
      "actions": [
        {
          "msg": {
            "fre": "I like your grandpa's style, can I have his hand me downs?"
          },
          "type": "reply"
        }
      ]
    },
    {
      "y": 114,
      "x": 595,
      "destination": "a86b6a47-a400-4639-b8c7-1df8164eda67",
      "uuid": "07eff9c4-d4ba-4973-86c0-e5bd23a32ebc",
      "actions": [
        {
          "msg": {
            "fre": "Please answer yes or no."
          },
          "type": "reply"
        }
      ]
    },
    {
      "y": 327,
      "x": 122,
      "destination": null,
      "uuid": "3e16d2b9-689f-4f92-8134-5471231b5e49",
      "actions": [
        {
          "msg": {
            "fre": "Thank you!"
          },
          "type": "reply"
        }
      ]
    },
    {
      "y": 326,
      "x": 350,
      "destination": null,
      "uuid": "c3acc747-e5a9-446a-a187-7ed300f1f3b5",
      "actions": [
        {
          "msg": {
            "fre": "No but for real, I'm going to take your grandpa's style!"
          },
          "type": "reply"
        }
      ]
    }
  ],
  "last_saved": "2015-06-17T19:37:45.943020Z",
  "entry": "bcbfdc41-232a-4549-b396-e23bd883646b",
  "rule_sets": [
    {
      "uuid": "a86b6a47-a400-4639-b8c7-1df8164eda67",
      "webhook_action": null,
      "rules": [
        {
          "category": {
            "base": "Yes",
            "fre": "Yes"
          },
          "uuid": "b88451fb-fc8e-476a-b7d8-44af34ffba3d",
          "destination": "3e16d2b9-689f-4f92-8134-5471231b5e49",
          "destination_type": "A",
          "test": {
            "test": {
              "fre": "yes y"
            },
            "base": "yes y",
            "type": "contains_any"
          },
          "config": {
            "type": "contains_any",
            "verbose_name": "has any of these words",
            "name": "Contains any",
            "localized": true,
            "operands": 1
          }
        },
        {
          "category": {
            "base": "No",
            "fre": "No"
          },
          "uuid": "5d3b5539-f285-4623-a7fd-b36bf37221ed",
          "destination": "c3acc747-e5a9-446a-a187-7ed300f1f3b5",
          "destination_type": "A",
          "test": {
            "test": {
              "fre": "No n"
            },
            "base": "No n",
            "type": "contains_any"
          },
          "config": {
            "type": "contains_any",
            "verbose_name": "has any of these words",
            "name": "Contains any",
            "localized": true,
            "operands": 1
          }
        },
        {
          "category": {
            "base": "Other",
            "fre": "Other"
          },
          "uuid": "4ae1b3e3-158d-4109-b926-1a2767cc5112",
          "destination": "07eff9c4-d4ba-4973-86c0-e5bd23a32ebc",
          "destination_type": "A",
          "test": {
            "test": "true",
            "type": "true"
          },
          "config": {
            "type": "true",
            "verbose_name": "contains anything",
            "name": "Other",
            "operands": 0
          }
        }
      ],
      "webhook": null,
      "label": "Hand Me Downs",
      "operand": "@step.value",
      "finished_key": null,
      "response_type": "C",
      "y": 186,
      "x": 243
    }
  ],
  "metadata": {}
}
```
