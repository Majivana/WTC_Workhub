# Workflows

The primary workflow is:

```text
Clock in -> record work -> attach evidence -> submit -> review
-> approve/reject/request changes -> verify -> notify -> recalculate progress
```

Invalid submission transitions must fail explicitly.

Work-entry capture accepts a work date, start/end times, break minutes, and activity type. The
server calculates effective duration, enforces the inclusive WorkPeriod dates, rejects duplicate
or overlapping intervals, and keeps new entries editable while they remain `DRAFT`.
