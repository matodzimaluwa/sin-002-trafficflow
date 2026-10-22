# RoutingServiceApp

## Overview

Provides estimated travel times based on congestion and intersection.

Part of the [TrafficFlow](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service subscribes to the ActiveMQ topic `congestion-topic` — see [`../common/`](../common). Broker URL and topic name come from the common `co.wethinkcode.trafficflow.mq.MqConfig` class alongside it in this module.

## Project structure

```
routing-service/
├── pom.xml
└── src/
    ├── main/java/co/wethinkcode/trafficflow/
    │   ├── RoutingServiceApp.java
    │   └── mq/
    │       └── MqConfig.java
    └── test/java/co/wethinkcode/trafficflow/
        └── RoutingServiceTest.java
```

## Build

```
mvn package
```

## Run

```
java -jar target/routing-service.jar
```

Listens on port `7023`.

## Endpoints

- `GET /health` returns `OK`
- `GET /route?from=INT-1001&to=INT-1002` checks both intersections with intersection-service (port 7021), reads the congestion level from congestion-service (port 7022), and returns both intersections, the `congestionLevel` and `estimatedTravelTimeMinutes`.

Travel time is 5 minutes plus 2 minutes per congestion level.

Errors:

- `400` if `from` or `to` is missing
- `404` if either intersection is unknown
- `502` if a service answers with an error
- `503` if a service can't be reached

## Test

Run the unit tests with `mvn test`. To manually verify it's up:

```
curl http://localhost:7023/health   # -> OK
```