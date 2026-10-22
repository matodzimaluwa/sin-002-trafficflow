# IntersectionServiceApp

## Overview

Validates intersection/district names (source of truth).

Part of the [TrafficFlow](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service publishes to the ActiveMQ queue `intersection-heartbeat-queue` — see [`../common/`](../common). Broker URL and queue name come from the common `co.wethinkcode.trafficflow.mq.MqConfig` class alongside it in this module.

## Project structure

```
intersection-service/
├── pom.xml
└── src/main/java/co/wethinkcode/trafficflow/
    ├── IntersectionServiceApp.java
    └── mq/
        └── MqConfig.java
```

## Build

```
mvn package
```

## Run

```
java -jar target/intersection-service.jar
```

Listens on port `7021`.

## Endpoints

- `GET /health` returns `OK`
- `GET /intersections/{id}` returns the intersection as JSON (case-insensitive id), or `404` if it isn't known
- `GET /districts/{name}` returns `{"district": "...", "intersections": [...]}` (case-insensitive name), or `404` if no intersection belongs to that district

The intersection list comes from ingestion-service (port 7020) on the first request and is then cached in memory. If ingestion can't be reached, these endpoints return `503`. The cache never refreshes, so restart this service to pick up new data.

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7021/health   # -> OK
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/trafficflow/`, and run `mvn test`.