RapidPro Flows
==============

Python implementation of the RapidPro flow engine.

Usage
-----

```python
org = Org("RW", "eng", pytz.timezone("Africa/Kigali"), DateStyle.DAY_FIRST, False)
fields = [...]
contact = Contact(...)
flow = Flow.from_json("...")

runner = Runner()

run = runner.start(org, fields, contact, flow)

steps = run.get_completed_steps()
fields_to_create = run.get_created_fields()

runner.resume(run, Input.of("Yes"))

json = run.to_json()  # run state can be serialized as JSON

restored = RunState.from_json(json, flow)  # and then de-serialized when needed

runner.resume(run, Input.of(123))

```

   