RapidPro Flows
==============

Java implementation of the RapidPro flow engine.

Usage
-----

```java
Org org = new Org("RW", "eng", ZoneId.of("Africa/Kigali"), DateStyle.DAY_FIRST, false);
List<Field> fields = new ArrayList();
Contact contact = new Contact(...);
Flow flow = Flow.fromJson("...");

Runner runner = new RunnerBuilder()
    .withLocationResolver(...)
    .build();

RunState run = runner.start(org, fields, contact, flow);

List<Step> steps = run.getCompletedSteps();
List<Field> fieldsToCreate = run.getCreatedFields();

runner.resume(run, Input.of("Yes"));

String json = run.toJson(); // run state can be serialized as JSON

RunState restored = RunState.fromJson(json, flow); // and then de-serialized when needed

runner.resume(run, Input.of(123));

```

Notes
-----

For use on Android, replace regular ThreeTen dependency with https://github.com/JakeWharton/ThreeTenABP