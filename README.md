# Bridge Service

This repository contains the **Bridge Service**, which serves as an intermediary that forwards requests between microservices in a distributed system. The service plays a key role in routing requests from the **[Producer Service](https://github.com/saidRaiss/observability-producer-service)** to the **[Consumer Service](https://github.com/saidRaiss/observability-consumer-service)**, ensuring smooth communication and request handling. It also includes OpenTelemetry integration to provide observability into the system's performance.

## Description

The **Bridge Service** is a core part of a microservices-based architecture, acting as a middle layer that handles communication between the producer and consumer microservices. It forwards the requests and is instrumented with OpenTelemetry for telemetry data collection. The service exports logs, metrics, and traces to observability tools via the OpenTelemetry Collector, enabling better monitoring and analysis of the system.

This repository is used as a **Git submodule** in the main [observability-project](https://github.com/saidRaiss/observability-project), which showcases the full observability stack, including Grafana, Loki, Tempo, and Prometheus.

## Environment Variables (Docker Compose)

The following environment variables are used in the **Bridge Service** configuration within the Docker Compose setup of the main observability project:

| Environment Variable                             | Description                                                         | Example Value                               |
|--------------------------------------------------|---------------------------------------------------------------------|---------------------------------------------|
| `APP_OUTBOUND_REST_CLIENTS_CONSUMER_BASE_URL`    | The base URL for the consumer service.                               | `http://consumer:8082`                      |
| `OTEL_SERVICE_NAME`                              | The name of the service for observability purposes.                  | `bridge-service`                            |
| `OTEL_RESOURCE_ATTRIBUTES`                       | Attributes to identify the service in observability tools.           | `service.name=bridge-service`               |
| `OTEL_EXPORTER_OTLP_ENDPOINT`                    | Endpoint for OpenTelemetry Collector to export telemetry data.       | `http://otel-collector:4317`                |
| `OTEL_EXPORTER_OTLP_PROTOCOL`                    | Protocol used for telemetry data export.                             | `grpc`                                      |
| `OTEL_LOGS_EXPORTER`                             | Logs exporter protocol.                                              | `otlp`                                      |
| `OTEL_TRACES_EXPORTER`                           | Traces exporter protocol.                                            | `otlp`                                      |
| `OTEL_METRICS_EXPORTER`                          | Metrics exporter protocol.                                           | `otlp`                                      |

## Usage as a Submodule

To clone the entire observability project along with this submodule, run the following command:

```bash
git clone --recursive https://github.com/saidRaiss/observability-project.git
```

To initialize the submodule after cloning the main repository, use:
```bash
git submodule update --init --recursive
```
