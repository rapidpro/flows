RapidPro Flows
==============

Java implementation of the RapidPro flow engine.

Usage
-----

```java
Org org = new Org("eng", ZoneId.of("Africa/Kigali"), true, false);
Contact contact = new Contact(...);
Flow flow = Flow.fromJson(flowJson);

RunState state1 = m_runner.newRun(org, contact, flow);
RunState state2 = m_runner.resume(state1, "Yes");
```

Notes
-----

For use on Android, replace regular ThreeTen dependency with https://github.com/JakeWharton/ThreeTenABP