RapidPro Flows
==============

Java implementation of the RapidPro flow engine.

Usage
-----

```java
Org org = new Org("RW", "eng", ZoneId.of("Africa/Kigali"), DateStyle.DAY_FIRST, false);
Contact contact = new Contact(...);
Flow flow = Flow.fromJson("...");

Flows.Runner runner = new Flows.RunnerBuilder()
    .withLocationResolver(...)
    .build();

RunState run = runner.start(org, contact, flow);

m_runner.resume(run, Input.of("Yes"));
```

Notes
-----

For use on Android, replace regular ThreeTen dependency with https://github.com/JakeWharton/ThreeTenABP