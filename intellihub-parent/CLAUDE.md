# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IntelliHub is a Spring Boot microservices project using Maven as the build system. It follows a multi-module Maven architecture with a common shared module and multiple service modules.

## Architecture

The project follows Spring Cloud microservices architecture with:

- **Parent Module**: `intellihub-parent` - Root Maven POM managing all sub-modules
- **Common Module**: `intelli-common` - Shared utilities, dependencies, and common code
- **Service Modules**: Individual microservices following the pattern `intelli-*-service`

### Services

1. **intelli-gateway-service** - API Gateway (Port: 8080) using Spring Cloud Gateway
2. **intelli-auth-iam-service** - Authentication & IAM Service
3. **intelli-api-platform-service** - API Platform Management
4. **intelli-governance-service** - Governance Service
5. **intelli-aigc-service** - AI Generation Service
6. **intelli-app-center-service** - Application Center
7. **intelli-search-service** - Search Service
8. **intelli-event-service** - Event Service
9. **intelli-log-audit-service** - Logging & Audit Service

## Technology Stack

- **Java**: 1.8 (minimum), compatible with Java 11
- **Spring Boot**: 2.7.18
- **Spring Cloud**: 2021.0.9
- **Spring Cloud Alibaba**: 2021.0.5.0
- **MyBatis Plus**: 3.5.5
- **MySQL**: 8.0.33
- **Nacos**: Service discovery and configuration management
- **Redis**: Caching and session management
- **Maven**: Build tool

## Common Development Tasks

### Building the Project

```bash
# Build all modules
mvn clean package

# Build without running tests
mvn clean package -DskipTests

# Build specific module
mvn clean package -pl intelli-gateway-service
```

### Running Services

Each service has its own main application class:

```bash
# Example: Run gateway service
cd intelli-gateway-service
mvn spring-boot:run

# Or run the compiled JAR
java -jar target/intelli-gateway-service-1.0.0-SNAPSHOT.jar
```

### Testing

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl intelli-auth-iam-service

# Generate test coverage report
mvn jacoco:report
```

## Configuration

- All services use `application.yml` in `src/main/resources`
- Centralized configuration via Nacos (server-addr: localhost:8848, namespace: dev)
- Gateway routes configured in shared Nacos config: `gateway-routes.yml`

## Key Patterns

### Package Structure
All services follow the package pattern: `com.vibe.intellihub.*`

### Service Discovery
Services register with Nacos using `@EnableDiscoveryClient`

### Database Layer
- MyBatis Plus for ORM
- Each service manages its own database schema
- Database connections configured per service

## Development Environment Setup

Required tools:
- JDK 8+ (11 recommended)
- Maven 3.6+
- MySQL 8.0+
- Redis
- Nacos Server
- Docker (optional, for infrastructure services)

## Documentation

Comprehensive project documentation is available in the `doc/` directory:
- `01_需求文档.md` - Requirements
- `02_项目规划与设计文档.md` - Architecture and design
- `03_开发与实现文档.md` - Development guide and best practices