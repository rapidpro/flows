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

## Design Goals

It is hard to enumerate the full set of capabilities provided by RapidPro, but
at a high level it provides:

 * Contact and Message management
 * Visual interface to build messaging "flows"
 * Ability to collect open ended, numerical, categorical, time or geographic input
 * Ability to build multi step questionnaires that interlink, contain skip logic
   control contact group memberships
 * Ability to schedule messages or flows based on time, keywords, missed or incoming calls
 * Ability to build individualized time based  campaigns, such as those used
   for maternity reminders
 * Powerful analytics framework to let you quickly gain insight into your data
 * Integration with Twilio, Nexmo, Kannel and many other messaging providers
 * Integration with Twitter to allow for direct messaging interactions
 * Integration with Twilio and Verboice allowing for IVR based flows to allow
   for voice prompting and either touch tone or recorded responses
 * Scalable architecture, able to handle millions of messages across
   thousands of organizations
 * Powerful API to allow integrating your own solution or building on top of the
   data you collect using RapidPro
