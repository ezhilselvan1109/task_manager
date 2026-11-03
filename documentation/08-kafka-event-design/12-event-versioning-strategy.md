# Event Versioning Strategy: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-EVT-012 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-EVT-004`, `DOC-EVT-005` |

---

## 1. Schema Evolution Principles
Because Kafka is an immutable, persistent event log that retains messages for 7 days, producers and consumers evolve asynchronously. An event published yesterday under schema version 1.0 may be consumed tomorrow by a newly deployed consumer version 1.1.

TMS enforces **Strict Backward and Forward Compatibility**:
1. **Consumers Must Tolerate Unknown Fields**: Consumers configure Jackson with `@JsonIgnoreProperties(ignoreUnknown = true)` so that new properties added by producers do not crash existing consumers.
2. **Producers Must Not Remove Mandatory Fields**: Fields declared `required` in `TaskEventEnvelope` version 1.0 must never be removed or renamed in minor version updates.
3. **Additive-Only Minor Releases**: Adding optional attributes constitutes a minor version bump (`1.0` $\rightarrow$ `1.1`), maintaining single-topic compatibility on `task.events.v1`.

---

## 2. Versioning Field in Event Envelope
Every event payload carries an immutable `eventVersion` string:
```json
{
  "eventId": "8f3b2a1c-9d4e-4f7a-8b1e-6c5d4e3f2a1b",
  "eventType": "TASK_CREATED",
  "eventVersion": "1.0",
  "payload": { ... }
}
```

---

## 3. Major Incompatible Schema Changes (`v2`)
If a fundamental domain restructuring requires deleting mandatory fields or changing data types:
1. **New Topic Creation**: Introduce a new topic `task.events.v2`.
2. **Dual-Publishing Window**: During the migration period, the `task-service` outbox poller publishes to *both* `task.events.v1` and `task.events.v2`.
3. **Consumer Migration**: Consumers migrate to subscribe to `task.events.v2`.
4. **Decommission**: Once all consumers have upgraded, publishing to `task.events.v1` is ceased.

---

## 4. Review Checklist
- [x] Strict backward and forward compatibility rules defined.
- [x] Jackson unknown-property tolerance enforced on consumers.
- [x] Dual-publishing migration strategy documented for major breaking changes.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Event Versioning Strategy document. |

