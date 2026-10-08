# NID — Delivery Gaps

Curated list of gaps across development, configuration, collaboration, security, testing, CI, observability, deployment readiness, and documentation.

Priority:
- `P1` — fix before it's demo-safe
- `P2` — strong resume signal
- `P3` — polish / nice-to-have

## Configuration & Environment
- `[P1]` No migrations — add Flyway/Liquibase; replace `ddl-auto: update`
- ~~`[P1]` Spring profiles added (`dev`/`prod`; `SPRING_PROFILES_ACTIVE` defaults to `dev`)~~
- ~~`[P1]` Session fixed — switched to `spring-boot-starter-session-jdbc` (Boot 4 module); `initialize-schema: always` (dev) / `never` (prod)~~
- `[P3]` No `.env.example` for onboarding

## Secrets
- `[P1]` Real secrets committed in `docker-compose.yml` (DB, object storage) — rotate + purge history
- `[P1]` Move to env / secret manager
- `[P2]` Secret scanning undermined by the above

## Correctness (E2E breakages)
- `[P1]` Cross-module event type mismatch → default-notebook provisioning likely broken
- ~~`[P1]` Session schema init missing → login/session likely broken~~ (resolved by the session fix)
- `[P3]` `ImageStorageService` is an empty shell
- `[P3]` AI chat memory in-memory + unbounded
- `[P3]` DLQ name typo (`nmtebook.creation.dlq`)
- `[P3]` Inconsistent error handling (`/artifact`, `/api/**`)
- `[P3]` Unused Kafka in compose

## Collaboration (Realtime)
- `[P2]` WebSocket handlers are stubs — `join`/`exit`/`presence`/`edit` do nothing
- `[P2]` No real-time edit broadcast/fan-out to other viewers of a note
- `[P2]` No concurrent-edit resolution (OT/CRDT) — effectively last-write-wins
- `[P2]` No presence / active-editor tracking (`NotePresencePayload` defined but unused)
- `[P2]` In-memory STOMP broker → no cross-instance fan-out (needs external broker or Redis relay)
- `[P2]` No reconnect / state resync after disconnect
- `[P3]` `/ws` has no auth and no note-level authorization on subscriptions
- `[P3]` Collaborative edits not versioned/persisted; no edit history
- `[P3]` No WebSocket/STOMP tests

## Observability — Runtime
- ~~`[P1]` Actuator added (`spring-boot-starter-actuator`)~~
- ~~`[P1]` `/actuator/health` + liveness/readiness probes enabled (k8s-ready)~~
- ~~`[P2]` `/actuator/prometheus` exposed on a separate management port (`micrometer-registry-prometheus`)~~
- `[P3]` No custom business metrics

## Observability — Traces & Logs
- `[P2]` Traces go to `debug` exporter only — no Tempo/Jaeger
- `[P2]` Logs go to `debug` only — no Loki/aggregation
- `[P2]` No Grafana dashboards
- `[P2]` Alertmanager receiver is a placeholder (`some-service`)
- `[P2]` Alerts are node-level only — no app alerts (error rate, p99, queue/DLQ depth)
- `[P3]` No sampling config; no log↔trace correlation

## Testing
- `[P2]` Unit tests only (2 mocked service files)
- `[P2]` No integration tests (Testcontainers)
- `[P2]` No auth-flow / messaging / storage / AI coverage
- `[P3]` No coverage gate
- `[P2]` No load test / throughput-latency numbers

## CI Pipeline
- `[P2]` No `~/.m2` cache → slow builds
- ~~`[P2]` Only `mvn test` as quality gate; no app static analysis~~ (added SpotBugs + FindSecBugs job)
- ~~`[P2]` No dependency scanning; container scanning only commented~~ (added GitLab Dependency + Container Scanning templates)
- `[P3]` No test reports/artifacts, no SBOM/signing
- `[P2]` Images tagged `:latest` only

## Deployment Readiness (for k8s/VPS later)
- `[P1]` Immutable versioned image tags (not `:latest`)
- `[P2]` OTel agent pulled from `latest` URL — non-reproducible; pin version
- `[P2]` Migration step at startup / init container
- `[P3]` No resource requests/limits
- `[P3]` No TLS / reverse proxy

## Security Hardening
- `[P2]` CSRF disabled though a `/csrf` endpoint exists
- `[P2]` Session fixation — login doesn't rotate session
- `[P3]` WebSocket allowed origins `*`
- `[P3]` `/api/**` `permitAll` → AI endpoints unauthenticated
- `[P3]` No rate limiting

## Presentation / Docs
- `[P1]` No README
- `[P1]` No architecture diagram
- `[P2]` No "tradeoffs / what I'd do next" section
- ~~`[P3]` No seeded test data / demo script~~ (added dev-only, configurable seeder: `DevDataSeeder` + `nid.seed.*`)
