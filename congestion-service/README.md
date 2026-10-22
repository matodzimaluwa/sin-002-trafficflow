# CongestionServiceApp

## Overview

Tracks the city-wide Congestion Level (0-8).

Part of the [TrafficFlow](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service publishes to the ActiveMQ topic `congestion-topic` — see [`../common/`](../common). Broker URL and topic name come from the common `co.wethinkcode.trafficflow.mq.MqConfig` class alongside it in this module.

## Project structure

```
congestion-service/
├── pom.xml
└── src/
    ├── main/java/co/wethinkcode/trafficflow/
    │   ├── CongestionServiceApp.java
    │   └── mq/
    │       └── MqConfig.java
    └── test/java/co/wethinkcode/trafficflow/
        └── CongestionServiceTest.java
```

## Build

```
mvn package
```

## Run

```
java -jar target/congestion-service.jar
```

Listens on port `7022`.

## Endpoints

- `GET /health` returns `OK`
- `GET /congestion` returns `{"level": n}`. The level starts at 0 and is kept in memory.
- `PUT /congestion?level=n` sets the level and returns it. Returns `400` if the level is missing, not a number, or outside 0-8.

## Test

Run the unit tests with `mvn test`. To manually verify it's up:

```
curl http://localhost:7022/health   # -> OK
```