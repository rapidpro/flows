---
layout: default
title: Flow Concepts
permalink: /concepts/
---

# Flow Concepts

![flow example](../images/overview_flow.png "Flow Example")

## Actions

Actions represent some action that is taken on behalf of the flow. This may be sending a recipient a message,
modifying a contact, starting another flow or sending emails among others.

Actions are organized in ActionSets to help in organizing a flow. All Actions in an ActionSet are executed
immediately in order from top to bottom.

## Rules

Rules represent a logical test that is performed against the current state of the flow execution. Rules may test
for the presence of a word in a variable or response or may test whether an input is a valid phone number or numeric
among others. Rules do not modify the state of a flow when run.

Rules are organized in RuleSets and executed from left to right. The first rule that matches takes effect and no other
rules are evaluated after there is a match.

A flow will always match at least one Rule in a RuleSet as the last Rule must always be a catch-all (the "Other" rule).

RuleSets are named, and the result of the evaluation of a RuleSet becomes a new variable in the flow state.

## Connections

Flows consist of a series of ActionSets and RuleSets interconnected.

Connection destinations may be either ActionSets or RuleSets. Connection sources are either individual Rules or
ActionSets.

## Context

At any point in a flow there is a current context of variables available in that flow. At a minimum these are the
flow variables populated by RuleSets, but may also include variables on a contact passing through the flow or data
loaded from an external source via a WebHook.

## Expressions

Expressions are used in flows to substitute a value from the flow Context into a Rule or Action. Expressions always
begin with @ and may optionally include Excel like functions within them. ```@contact.name``` is an example of an
expression in the above flow.

## Flow Execution

Flow definitions include an entry point and continue executing until they reach a RuleSet causing them to pause.
