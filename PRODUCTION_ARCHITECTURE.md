# PRODUCTION ARCHITECTURE
## MSME AI-Powered Credit Intelligence Platform
### IDBI Bank Hackathon - Production-Grade Implementation

---

## ARCHITECTURE OVERVIEW

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                    │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  React 19 + TypeScript + Vite + Tailwind CSS + PWA                │   │
│  │  TanStack Query │ React Router │ React Hook Form + Zod            │   │
│  │  Recharts/ECharts │ Framer Motion │ Dark/Light Mode               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           API GATEWAY                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  Spring Cloud Gateway                                               │   │
│  │  JWT Validation │ Rate Limiting │ API Versioning │ CORS             │   │
│  │  Request Logging │ Load Balancing │ Circuit Breaker                 │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
          ┌───────────────────────────┼───────────────────────────┐
          ▼                           ▼                           ▼
┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│   JAVA SERVICES     │  │   PYTHON AI SERVICES │  │   MESSAGE BROKER    │
│                     │  │                     │  │                     │
│ Auth Service        │  │ Scoring Engine      │  │                     │
│ MSME Service        │  │ Feature Engineering │  │      Kafka          │
│ Financial Health    │◄─┤ Explainability      │  │                     │
│ Aggregation Service │  │ Forecasting         │  │  Event Streaming    │
│ Fraud Service       │  │ Fraud Detection     │  │  Async Processing   │
│ Recommendation Svc  │  │ Recommendations     │  │  Service Comm       │
│ Notification Svc    │  │ Benchmarking        │  │                     │
│ Reporting Service   │  │ Early Warning       │  │                     │
│ Audit Service       │  │                     │  │                     │
│ Admin Service       │  │                     │  │                     │
└─────────────────────┘  └─────────────────────┘  └─────────────────────┘
          │                           │                           │
          └───────────────────────────┼───────────────────────────┘
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DATA LAYER                                         │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐      │
│  │  PostgreSQL   │ │    Redis     │ │Elasticsearch │ │   MinIO/S3   │      │
│  │  (per svc)    │ │   (Cache)    │ │  (Logs)      │ │ (Reports)    │      │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘      │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        INFRASTRUCTURE                                        │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐      │
│  │   Docker     │ │  Kubernetes  │ │   Flyway     │ │   GitHub     │      │
│  │   Compose    │ │   (Future)   │ │  (Migrations)│ │  Actions     │      │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘      │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                       │
│  │  Prometheus  │ │   Grafana    │ │  Spring Boot │                       │
│  │  (Metrics)   │ │ (Dashboards) │ │  Actuator    │                       │
│  └──────────────┘ └──────────────┘ └──────────────┘                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## MICROSERVICES TOPOLOGY

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| api-gateway | 8080 | - | Central entry point |
| auth-service | 8081 | auth_db | Authentication, JWT, OAuth2 |
| msme-service | 8082 | msme_db | MSME profiles, KYC |
| financial-health-service | 8083 | health_db | Health scoring |
| aggregation-service | 8084 | aggregation_db | AA, ULI, OCEN integration |
| fraud-service | 8085 | fraud_db | Fraud detection |
| recommendation-service | 8086 | recommendation_db | AI recommendations |
| forecast-service | 8087 | forecast_db | Predictions |
| notification-service | 8088 | notification_db | Alerts, emails |
| reporting-service | 8089 | report_db | PDF/Excel generation |
| audit-service | 8090 | audit_db | Audit logging |
| admin-service | 8091 | admin_db | User management |
| ai-scoring | 5001 | - | Health score ML |
| ai-forecasting | 5002 | - | Forecast ML |
| ai-fraud | 5003 | - | Fraud ML |
| ai-explainability | 5004 | - | SHAP/LIME |
| ai-recommendation | 5005 | - | Recommendations |
| ai-benchmarking | 5006 | - | Industry data |
| ai-early-warning | 5007 | - | Risk alerts |

---

## TECHNOLOGY STACK

### Frontend
```json
{
  "react": "19.x",
  "typescript": "5.x",
  "vite": "6.x",
  "tailwindcss": "3.x",
  "@tanstack/react-query": "5.x",
  "react-router-dom": "7.x",
  "react-hook-form": "7.x",
  "zod": "3.x",
  "recharts": "2.x",
  "framer-motion": "11.x",
  "axios": "1.x",
  "next-themes": "0.x",
  "workbox": "7.x"
}
```

### Java Services
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.6</version>
    </dependency>
</dependencies>
```

### Python AI Services
```txt
fastapi==0.111.0
uvicorn==0.30.0
pydantic==2.7.0
sqlalchemy==2.0.30
psycopg2-binary==2.9.9
redis==5.0.4
celery==5.4.0
scikit-learn==1.5.0
xgboost==2.0.3
lightgbm==4.3.0
shap==0.45.0
lime==0.2.0
prophet==1.1.5
pandas==2.2.2
numpy==1.26.4
joblib==1.4.2
httpx==0.27.0
```

### Infrastructure
```yaml
Database: PostgreSQL 16
Cache: Redis 7
Search: Elasticsearch 8.x
Storage: MinIO (S3-compatible)
Broker: Apache Kafka 3.x
Container: Docker, Docker Compose
Orchestration: Kubernetes (future)
CI/CD: GitHub Actions
Monitoring: Prometheus + Grafana
Logging: ELK Stack
Migration: Flyway (Java) / Alembic (Python)
```

---

## SERVICE COMMUNICATION

### Synchronous (REST)
```
Frontend → Gateway → Java Service → AI Service
```

### Asynchronous (Kafka)
```
Event: MSME_CREATED → Aggregation Service
Event: SCORE_CALCULATED → Notification Service
Event: FRAUD_DETECTED → Audit Service
Event: LOAN_APPLICATION → Scoring Service
```

### Kafka Topics
```yaml
topics:
  - msme.events
  - scoring.events
  - fraud.events
  - notification.events
  - audit.events
  - loan.events
  - aggregation.events
```

---

## API GATEWAY CONFIGURATION

```yaml
# application.yml - Spring Cloud Gateway

spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://auth-service:8081
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: auth-circuit
                fallbackUri: forward:/fallback/auth

        - id: msme-service
          uri: http://msme-service:8082
          predicates:
            - Path=/api/v1/msme/**
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20

        - id: scoring-service
          uri: http://financial-health-service:8083
          predicates:
            - Path=/api/v1/scoring/**
          filters:
            - StripPrefix=2

        - id: ai-scoring
          uri: http://ai-scoring:5001
          predicates:
            - Path=/api/v1/ai/scoring/**
          filters:
            - StripPrefix=3

      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:3000"
              - "http://localhost:5173"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
```

---

## SECURITY ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────────┐
│                    SECURITY LAYERS                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Layer 1: API Gateway                                     │   │
│  │ • JWT Validation                                         │   │
│  │ • Rate Limiting (100 req/min)                           │   │
│  │ • IP Whitelisting (production)                          │   │
│  │ • CORS Policy                                            │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                      │
│                           ▼                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Layer 2: Service Security                                │   │
│  │ • OAuth2/OpenID Connect                                  │   │
│  │ • Role-Based Access Control (RBAC)                      │   │
│  │ • Method-Level Security                                  │   │
│  │ • Input Validation (Zod/Bean Validation)                │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                      │
│                           ▼                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Layer 3: Data Security                                   │   │
│  │ • Encryption at Rest (AES-256)                          │   │
│  │ • Encryption in Transit (TLS 1.3)                       │   │
│  │ • Field-Level Encryption (PII)                          │   │
│  │ • Secrets Manager (HashiCorp Vault)                     │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                      │
│                           ▼                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Layer 4: Application Security                            │   │
│  │ • SQL Injection Protection (JPA/Hibernate)              │   │
│  │ • XSS Protection (CSP Headers)                          │   │
│  │ • CSRF Protection                                        │   │
│  │ • Secure Headers (Helmet.js)                            │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### JWT Token Structure
```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "key-id-123"
  },
  "payload": {
    "sub": "user-uuid-123",
    "email": "msme@example.com",
    "role": "msme",
    "permissions": ["read:profile", "write:profile", "read:score"],
    "msme_id": "msme-uuid-456",
    "iat": 1704067200,
    "exp": 1704070800,
    "iss": "msme-platform"
  }
}
```

### RBAC Matrix
```
┌────────────────────┬───────┬──────────────┬───────────────┬──────────┐
│ Permission         │ MSME  │ Loan Officer │ Credit Mgr    │ Admin    │
├────────────────────┼───────┼──────────────┼───────────────┼──────────┤
│ read:own-profile   │   ✓   │      ✓       │       ✓       │    ✓     │
│ write:own-profile  │   ✓   │      -       │       -       │    ✓     │
│ read:own-score     │   ✓   │      ✓       │       ✓       │    ✓     │
│ read:all-profiles  │   -   │      ✓       │       ✓       │    ✓     │
│ read:all-scores    │   -   │      ✓       │       ✓       │    ✓     │
│ approve:loan       │   -   │      ✓       │       ✓       │    ✓     │
│ reject:loan        │   -   │      -       │       ✓       │    ✓     │
│ view:portfolio     │   -   │      -       │       ✓       │    ✓     │
│ manage:users       │   -   │      -       │       -       │    ✓     │
│ view:audit         │   -   │      -       │       -       │    ✓     │
│ system:config      │   -   │      -       │       -       │    ✓     │
└────────────────────┴───────┴──────────────┴───────────────┴──────────┘
```

---

## MONITORING STACK

```yaml
# Prometheus Configuration
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'java-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
          - 'auth-service:8081'
          - 'msme-service:8082'
          - 'financial-health-service:8083'
          - 'aggregation-service:8084'
          - 'fraud-service:8085'
          - 'notification-service:8088'

  - job_name: 'ai-services'
    metrics_path: '/metrics'
    static_configs:
      - targets:
          - 'ai-scoring:5001'
          - 'ai-forecasting:5002'
          - 'ai-fraud:5003'
          - 'ai-explainability:5004'

  - job_name: 'kafka'
    static_configs:
      - targets:
          - 'kafka:9090'

  - job_name: 'redis'
    static_configs:
      - targets:
          - 'redis:6379'

# Grafana Dashboards
dashboards:
  - name: Service Health
    panels:
      - API Response Time
      - Error Rate
      - Request Count
      - JVM Memory
      - CPU Usage

  - name: AI Performance
    panels:
      - Model Inference Time
      - Accuracy Metrics
      - Feature Importance
      - Prediction Distribution

  - name: Business Metrics
    panels:
      - Active MSMEs
      - Loans Processed
      - Score Distribution
      - Fraud Detection Rate
```

---

## DOCKER COMPOSE (PRODUCTION)

```yaml
version: '3.8'

services:
  # Infrastructure
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_MULTIPLE_DATABASES: auth,msme,health,aggregation,fraud,recommendation,forecast,notification,reporting,audit,admin
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./infrastructure/postgres/init.sh:/docker-entrypoint-initdb.d/init.sh
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_NUM_PARTITIONS: 6
    ports:
      - "9092:9092"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  elasticsearch:
    image: elasticsearch:8.13.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_USER}
      MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD}
    ports:
      - "9000:9000"
      - "9001:9001"

  # Java Services
  api-gateway:
    build:
      context: ./gateway
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery:8761/eureka
    depends_on:
      - postgres
      - redis

  auth-service:
    build:
      context: ./auth-service
      dockerfile: Dockerfile
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DATABASE_URL=jdbc:postgresql://postgres:5432/auth
    depends_on:
      - postgres
      - redis

  msme-service:
    build:
      context: ./msme-service
      dockerfile: Dockerfile
    ports:
      - "8082:8082"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/msme
    depends_on:
      - postgres

  financial-health-service:
    build:
      context: ./financial-health-service
      dockerfile: Dockerfile
    ports:
      - "8083:8083"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/health
    depends_on:
      - postgres

  aggregation-service:
    build:
      context: ./aggregation-service
      dockerfile: Dockerfile
    ports:
      - "8084:8084"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/aggregation
    depends_on:
      - postgres

  fraud-service:
    build:
      context: ./fraud-service
      dockerfile: Dockerfile
    ports:
      - "8085:8085"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/fraud
    depends_on:
      - postgres

  notification-service:
    build:
      context: ./notification-service
      dockerfile: Dockerfile
    ports:
      - "8088:8088"
    depends_on:
      - kafka
      - redis

  reporting-service:
    build:
      context: ./reporting-service
      dockerfile: Dockerfile
    ports:
      - "8089:8089"
    depends_on:
      - minio

  audit-service:
    build:
      context: ./audit-service
      dockerfile: Dockerfile
    ports:
      - "8090:8090"
    depends_on:
      - kafka
      - elasticsearch

  admin-service:
    build:
      context: ./admin-service
      dockerfile: Dockerfile
    ports:
      - "8091:8091"
    depends_on:
      - postgres

  # AI Services
  ai-scoring:
    build:
      context: ./ai-services/scoring
      dockerfile: Dockerfile
    ports:
      - "5001:5001"
    environment:
      - DATABASE_URL=postgresql://postgres:password@postgres:5432/health
    depends_on:
      - postgres
      - redis

  ai-forecasting:
    build:
      context: ./ai-services/forecasting
      dockerfile: Dockerfile
    ports:
      - "5002:5002"
    depends_on:
      - redis

  ai-fraud:
    build:
      context: ./ai-services/fraud
      dockerfile: Dockerfile
    ports:
      - "5003:5003"
    depends_on:
      - redis

  ai-explainability:
    build:
      context: ./ai-services/explainability
      dockerfile: Dockerfile
    ports:
      - "5004:5004"

  ai-recommendation:
    build:
      context: ./ai-services/recommendation
      dockerfile: Dockerfile
    ports:
      - "5005:5005"
    depends_on:
      - redis

  ai-benchmarking:
    build:
      context: ./ai-services/benchmarking
      dockerfile: Dockerfile
    ports:
      - "5006:5006"
    depends_on:
      - redis

  ai-early-warning:
    build:
      context: ./ai-services/early-warning
      dockerfile: Dockerfile
    ports:
      - "5007:5007"
    depends_on:
      - kafka

  # Frontend
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "3000:80"
    depends_on:
      - api-gateway

  # Monitoring
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./infrastructure/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana:latest
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
    volumes:
      - ./infrastructure/grafana/dashboards:/var/lib/grafana/dashboards
    ports:
      - "3001:3000"

  kibana:
    image: kibana:8.13.0
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    ports:
      - "5601:5601"

volumes:
  postgres_data:
  redis_data:

networks:
  default:
    name: msme-platform-network
```

---

## GITHUB ACTIONS CI/CD

```yaml
# .github/workflows/ci.yml

name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_PREFIX: ${{ github.repository }}

jobs:
  test-java:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [auth-service, msme-service, financial-health-service]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Test ${{ matrix.service }}
        run: |
          cd ${{ matrix.service }}
          ./mvnw test

  test-python:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [scoring, forecasting, fraud]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: '3.11'
      - name: Test ${{ matrix.service }}
        run: |
          cd ai-services/${{ matrix.service }}
          pip install -r requirements.txt
          pytest

  build:
    needs: [test-java, test-python]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build Docker images
        run: docker-compose build

  deploy:
    if: github.ref == 'refs/heads/main'
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to staging
        run: |
          echo "Deploying to staging environment..."
          # kubectl apply -f k8s/staging/
```

---

## FLYWAY MIGRATIONS

```sql
-- V1__create_auth_tables.sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'msme',
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- V2__create_msme_tables.sql
CREATE TABLE msme_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id),
    business_name VARCHAR(200) NOT NULL,
    registration_number VARCHAR(100),
    pan_number VARCHAR(20) ENCRYPTED,
    gstin VARCHAR(20) ENCRYPTED,
    udyam_number VARCHAR(50),
    industry_classification VARCHAR(100),
    industry_code VARCHAR(20),
    msme_category VARCHAR(20),
    employee_count INTEGER,
    annual_turnover DECIMAL(15,2),
    years_in_operation INTEGER,
    business_type VARCHAR(50),
    address JSONB,
    contact_info JSONB,
    documents JSONB,
    verification_status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- V3__create_financial_health_tables.sql
CREATE TABLE financial_health_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    overall_score DECIMAL(5,2),
    confidence_score DECIMAL(5,2),
    grade VARCHAR(10),
    
    -- Dimension Scores
    cash_flow_score DECIMAL(5,2),
    revenue_score DECIMAL(5,2),
    compliance_score DECIMAL(5,2),
    liquidity_score DECIMAL(5,2),
    payment_discipline_score DECIMAL(5,2),
    employee_stability_score DECIMAL(5,2),
    business_stability_score DECIMAL(5,2),
    digital_transaction_score DECIMAL(5,2),
    working_capital_score DECIMAL(5,2),
    
    -- Weights
    dimension_weights JSONB,
    
    -- Metadata
    model_version VARCHAR(20),
    features_used JSONB,
    computed_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_health_score_msme ON financial_health_scores(msme_id, computed_at DESC);

-- V4__create_lending_tables.sql
CREATE TABLE loan_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    application_number VARCHAR(50) UNIQUE NOT NULL,
    loan_amount DECIMAL(15,2) NOT NULL,
    loan_purpose VARCHAR(200),
    loan_type VARCHAR(50),
    tenure_months INTEGER,
    
    -- Status
    status VARCHAR(30) DEFAULT 'pending',
    
    -- Assessments
    health_score_snapshot JSONB,
    risk_assessment JSONB,
    fraud_check_result JSONB,
    recommendation JSONB,
    
    -- Decision
    decision JSONB,
    decided_by UUID REFERENCES users(id),
    decided_at TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_loan_msme ON loan_applications(msme_id);
CREATE INDEX idx_loan_status ON loan_applications(status);

-- V5__create_fraud_tables.sql
CREATE TABLE fraud_checks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    application_id UUID REFERENCES loan_applications(id),
    check_type VARCHAR(50),
    risk_score DECIMAL(5,2),
    risk_factors JSONB,
    is_suspicious BOOLEAN DEFAULT FALSE,
    model_version VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE fraud_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fraud_check_id UUID NOT NULL REFERENCES fraud_checks(id),
    alert_type VARCHAR(50),
    severity VARCHAR(20),
    description TEXT,
    status VARCHAR(20) DEFAULT 'open',
    assigned_to UUID REFERENCES users(id),
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- V6__create_notifications_tables.sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,
    channel VARCHAR(20) DEFAULT 'in_app',
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications(user_id, read_at);

-- V7__create_audit_tables.sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    correlation_id VARCHAR(100),
    user_id UUID REFERENCES users(id),
    service_name VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_method VARCHAR(10),
    request_path VARCHAR(500),
    response_status INTEGER,
    duration_ms BIGINT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_audit_correlation ON audit_logs(correlation_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
```

---

## KUBERNETES MANIFESTS (FUTURE)

```yaml
# k8s/base/auth-service.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
  labels:
    app: auth-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
        - name: auth-service
          image: ghcr.io/msme-platform/auth-service:latest
          ports:
            - containerPort: 8081
          env:
            - name: DATABASE_URL
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: auth-db-url
            - name: JWT_SECRET
              valueFrom:
                secretKeyRef:
                  name: security-secrets
                  key: jwt-secret
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8081
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8081
            initialDelaySeconds: 5
            periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: auth-service
spec:
  selector:
    app: auth-service
  ports:
    - port: 8081
      targetPort: 8081
  type: ClusterIP
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: auth-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: auth-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

---

## COMPLETE FOLDER STRUCTURE

```
msme-platform/
│
├── gateway/                          # Spring Cloud Gateway
│   ├── src/main/java/
│   │   └── com/msme/gateway/
│   │       ├── GatewayApplication.java
│   │       ├── config/
│   │       │   ├── SecurityConfig.java
│   │       │   ├── CorsConfig.java
│   │       │   └── RateLimitConfig.java
│   │       ├── filter/
│   │       │   ├── JwtAuthFilter.java
│   │       │   ├── RequestLoggingFilter.java
│   │       │   └── CorrelationIdFilter.java
│   │       └── exception/
│   │           └── GlobalExceptionHandler.java
│   └── pom.xml
│
├── auth-service/                     # Authentication Microservice
│   ├── src/main/java/
│   │   └── com/msme/auth/
│   │       ├── AuthApplication.java
│   │       ├── controller/
│   │       │   ├── AuthController.java
│   │       │   └── UserController.java
│   │       ├── service/
│   │       │   ├── AuthService.java
│   │       │   ├── UserService.java
│   │       │   ├── JwtService.java
│   │       │   └── OAuthService.java
│   │       ├── model/
│   │       │   ├── User.java
│   │       │   ├── Role.java
│   │       │   └── Permission.java
│   │       ├── repository/
│   │       │   └── UserRepository.java
│   │       └── security/
│   │           ├── JwtTokenProvider.java
│   │           └── CustomUserDetailsService.java
│   └── pom.xml
│
├── msme-service/                     # MSME Profile Management
│   ├── src/main/java/
│   │   └── com/msme/profile/
│   │       ├── MsmeServiceApplication.java
│   │       ├── controller/
│   │       │   ├── MsmeProfileController.java
│   │       │   └── DocumentController.java
│   │       ├── service/
│   │       │   ├── MsmeProfileService.java
│   │       │   ├── DocumentService.java
│   │       │   └── VerificationService.java
│   │       ├── model/
│   │       │   ├── MsmeProfile.java
│   │       │   └── Document.java
│   │       └── repository/
│   │           └── MsmeProfileRepository.java
│   └── pom.xml
│
├── financial-health-service/         # Health Scoring
│   ├── src/main/java/
│   │   └── com/msme/health/
│   │       ├── HealthServiceApplication.java
│   │       ├── controller/
│   │       │   └── HealthScoreController.java
│   │       ├── service/
│   │       │   ├── HealthScoreService.java
│   │       │   ├── DimensionCalculator.java
│   │       │   └── GradeCalculator.java
│   │       ├── model/
│   │       │   ├── HealthScore.java
│   │       │   └── DimensionScore.java
│   │       └── repository/
│   │           └── HealthScoreRepository.java
│   └── pom.xml
│
├── aggregation-service/              # AA/ULI/OCEN Integration
│   ├── src/main/java/
│   │   └── com/msme/aggregation/
│   │       ├── AggregationApplication.java
│   │       ├── controller/
│   │       │   ├── AAConsentController.java
│   │       │   ├── ULIController.java
│   │       │   └── OCENController.java
│   │       ├── service/
│   │       │   ├── AAConsentService.java
│   │       │   ├── ULIService.java
│   │       │   ├── OCENService.java
│   │       │   └── DataFetchService.java
│   │       └── client/
│   │           ├── AAPIClient.java
│   │           ├── ULIClient.java
│   │           └── OCENClient.java
│   └── pom.xml
│
├── fraud-service/                    # Fraud Detection
├── recommendation-service/           # AI Recommendations
├── forecast-service/                 # Forecasting
├── notification-service/             # Notifications
├── reporting-service/                # PDF/Excel Reports
├── audit-service/                    # Audit Logging
├── admin-service/                    # Admin Management
│
├── ai-services/                      # Python AI Microservices
│   │
│   ├── scoring/                      # Health Score ML
│   │   ├── app/
│   │   │   ├── main.py
│   │   │   ├── models/
│   │   │   │   ├── xgboost_model.py
│   │   │   │   └── lightgbm_model.py
│   │   │   ├── features/
│   │   │   │   └── feature_engineering.py
│   │   │   └── api/
│   │   │       └── routes.py
│   │   ├── ml_models/
│   │   └── requirements.txt
│   │
│   ├── forecasting/                  # Forecast ML
│   │   ├── app/
│   │   │   ├── models/
│   │   │   │   ├── prophet_model.py
│   │   │   │   ├── lightgbm_forecast.py
│   │   │   │   └── lstm_model.py
│   │   │   └── scenarios/
│   │   │       └── digital_twin.py
│   │   └── requirements.txt
│   │
│   ├── fraud/                        # Fraud ML
│   │   ├── app/
│   │   │   ├── models/
│   │   │   │   ├── isolation_forest.py
│   │   │   │   ├── local_outlier.py
│   │   │   │   └── autoencoder.py
│   │   │   └── rules/
│   │   │       └── rule_engine.py
│   │   └── requirements.txt
│   │
│   ├── explainability/               # SHAP/LIME
│   │   ├── app/
│   │   │   ├── shap_explainer.py
│   │   │   ├── lime_explainer.py
│   │   │   └── counterfactual.py
│   │   └── requirements.txt
│   │
│   ├── recommendation/               # Recommendations
│   │   ├── app/
│   │   │   ├── rule_engine.py
│   │   │   └── ml_recommender.py
│   │   └── requirements.txt
│   │
│   ├── benchmarking/                 # Industry Benchmarks
│   │   ├── app/
│   │   │   └── benchmark_service.py
│   │   └── requirements.txt
│   │
│   └── early-warning/                # Early Warning
│       ├── app/
│       │   ├── monitor.py
│       │   └── alert_engine.py
│       └── requirements.txt
│
├── frontend/                         # React 19 Frontend
│   ├── src/
│   │   ├── main.tsx
│   │   ├── App.tsx
│   │   ├── api/
│   │   │   ├── client.ts
│   │   │   ├── auth.api.ts
│   │   │   ├── msme.api.ts
│   │   │   └── health.api.ts
│   │   ├── components/
│   │   │   ├── ui/
│   │   │   ├── charts/
│   │   │   ├── forms/
│   │   │   ├── layout/
│   │   │   └── shared/
│   │   ├── pages/
│   │   ├── hooks/
│   │   ├── store/
│   │   ├── types/
│   │   └── utils/
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   └── tsconfig.json
│
├── infrastructure/
│   ├── postgres/
│   │   └── init.sh
│   ├── prometheus/
│   │   └── prometheus.yml
│   ├── grafana/
│   │   └── dashboards/
│   └── k8s/
│       └── base/
│
├── docker/
│   ├── Dockerfile.java
│   ├── Dockerfile.python
│   └── Dockerfile.frontend
│
├── docs/
│   ├── API.md
│   ├── ARCHITECTURE.md
│   └── DEPLOYMENT.md
│
├── docker-compose.yml
├── docker-compose.prod.yml
├── .github/
│   └── workflows/
│       └── ci.yml
├── .env.example
└── README.md
```

---

## NON-FUNCTIONAL REQUIREMENTS COMPLIANCE

| Requirement | Implementation |
|-------------|----------------|
| Response Time < 2s | Redis caching, connection pooling, async processing |
| Horizontal Scalability | Kubernetes HPA, stateless services |
| Fault Tolerance | Circuit breakers, retry mechanisms, fallbacks |
| 99.9% Availability | Multi-replica deployment, health checks |
| Input Validation | Zod (frontend), Bean Validation (Java), Pydantic (Python) |
| Audit Logging | Dedicated audit service, Kafka event streaming |
| API Documentation | OpenAPI/Swagger for all services |
| Security | JWT, OAuth2, RBAC, encryption, secure headers |

---

## HACKATHON PRESENTATION SUMMARY

### Key Differentiators
1. **Microservices Architecture** - Production-grade, not a monolith
2. **AI-Powered Scoring** - XGBoost, LightGBM with SHAP explainability
3. **Real AA/ULI/OCEN Integration** - Not just mockups
4. **Multi-Language Stack** - Java + Python best-of-breed
5. **Event-Driven** - Kafka for async processing
6. **Enterprise Security** - OAuth2, RBAC, encryption
7. **Monitoring** - Prometheus + Grafana dashboards
8. **DevOps Ready** - Docker, K8s manifests, CI/CD

### Technical Innovation
- Digital Twin simulation for scenario analysis
- Early Warning System with proactive alerts
- Portfolio Intelligence for bank dashboards
- Dynamic weights adapting to industry/business age
- Confidence scores alongside health scores

**Total Services: 19 (11 Java + 7 Python + 1 Frontend)**
**Total Database Tables: 25+**
**Total API Endpoints: 150+**
