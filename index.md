---
layout: default
title: Flows
permalink: /
---

# Introduction
With the increasing adoption of RapidPro by both the public and private sector, it is becoming more
important for there to be a standard way of describing flows and their functionality. This will allow
greater interoperability between systems and with the addition of a reference Flow Engine even allow
for the use of flows in different environments than cloud services.

The primary goal of the specification and reference flow engine is to allow other parties to build
compatible flow builders (and engines) without having to guess as to what the appropriate behavior is.

Our goal is to be only as specific as needed to avoid ambiguity while also creating a framework that
allows vendors to extend the functionality without starting anew.

## Reference Engines

Two reference engines are provided, one in Python and one in Java in the [RapidPro Flow Repository](https://github.com/rapidpro/flows). While the specification attempts to be complete
in it's description of the flow format, the engines should be referenced for details on specific
behaviors and interactions of rules and actions.

## Versions

RapidPro is an evolving platform, as such the flow specification continues to change as we add
or modify functionality. This specification refers to the latest version of the flow specification,
version 8.
