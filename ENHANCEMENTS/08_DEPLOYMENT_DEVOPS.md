# ENHANCEMENT 8: DEPLOYMENT & DEVOPS
## Enterprise-Grade Deployment with CI/CD Pipeline

---

### Feature Name
**Kubernetes-Based Deployment with Blue-Green Strategy and Auto-Scaling**

---

### Problem It Solves
Banking applications require high availability, zero-downtime deployments, and strict security. Manual deployments are error-prone and slow.

---

### Why It Is Needed
- 99.99% uptime requirement for banking
- Zero-downtime deployments
- Auto-scaling for variable load
- Security compliance (RBI guidelines)

---

### Relation to Problem Statement
Ensures reliable, scalable deployment of all platform components.

---

### Business Benefits
- 99.99% uptime
- 50% faster deployments
- 70% reduction in downtime
- Auto-scaling saves 30% infrastructure cost

---

### Technical Benefits
- Automated CI/CD
- Infrastructure as Code
- Monitoring and alerting
- Disaster recovery

---

### Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    DEPLOYMENT ARCHITECTURE                       │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  CI/CD PIPELINE (GitHub Actions)                         │    │
│  │                                                          │    │
│  │  Code Push → Lint → Test → Build → Scan → Deploy        │    │
│  │                                                          │    │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐        │    │
│  │  │ Lint │ │ Test │ │Build │ │ Scan │ │Deploy│        │    │
│  │  │ 2min │ │ 5min │ │ 3min │ │ 5min │ │ 10min│        │    │
│  │  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘        │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  KUBERNETES CLUSTER (AWS EKS / Azure AKS)               │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  PRODUCTION                                      │    │    │
│  │  │  ├── API Gateway (Kong)                         │    │    │
│  │  │  ├── Health Service (3 replicas)                │    │    │
│  │  │  ├── Scoring Service (5 replicas)               │    │    │
│  │  │  ├── AA/ULI/OCEN Service (2 replicas)           │    │    │
│  │  │  ├── Fraud Detection (2 replicas)               │    │    │
│  │  │  ├── ML Serving (3 replicas + GPU)              │    │    │
│  │  │  └── Frontend (CDN + 2 replicas)                │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  STAGING                                         │    │    │
│  │  │  └── Same as production (scaled down)           │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  INFRASTRUCTURE (Terraform)                              │    │
│  │                                                          │    │
│  │  ├── VPC + Subnets                                     │    │
│  │  ├── RDS PostgreSQL (Multi-AZ)                         │    │
│  │  ├── ElastiCache Redis (Cluster)                       │    │
│  │  ├── MSK Kafka (3 brokers)                             │    │
│  │  ├── S3 Bucket (Documents)                             │    │
│  │  ├── CloudFront CDN                                    │    │
│  │  ├── WAF (Web Application Firewall)                    │    │
│  │  └── KMS (Key Management)                              │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  MONITORING (Prometheus + Grafana)                       │    │
│  │                                                          │    │
│  │  ├── Application Metrics                               │    │
│  │  ├── Infrastructure Metrics                            │    │
│  │  ├── Business Metrics                                  │    │
│  │  ├── Security Metrics                                  │    │
│  │  └── Alerting (PagerDuty / Slack)                       │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### Kubernetes Configuration

```yaml
# health-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: health-service
  namespace: production
spec:
  replicas: 3
  selector:
    matchLabels:
      app: health-service
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    metadata:
      labels:
        app: health-service
    spec:
      containers:
        - name: health-service
          image: registry.idbi.com/msme-platform/health-service:v2.3.0
          ports:
            - containerPort: 8000
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          env:
            - name: DATABASE_URL
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: url
            - name: REDIS_URL
              valueFrom:
                secretKeyRef:
                  name: redis-credentials
                  key: url
          readinessProbe:
            httpGet:
              path: /health
              port: 8000
            initialDelaySeconds: 10
            periodSeconds: 5
          livenessProbe:
            httpGet:
              path: /health
              port: 8000
            initialDelaySeconds: 30
            periodSeconds: 10
          volumeMounts:
            - name: config
              mountPath: /app/config
              readOnly: true
      volumes:
        - name: config
          configMap:
            name: health-service-config
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: health-service-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: health-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
---
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: health-service-netpol
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: health-service
  policyTypes:
    - Ingress
    - Egress
  ingress:
    - from:
        - namespaceSelector:
            matchLabels:
              name: api-gateway
      ports:
        - protocol: TCP
          port: 8000
  egress:
    - to:
        - namespaceSelector:
            matchLabels:
              name: database
      ports:
        - protocol: TCP
          port: 5432
```

---

### CI/CD Pipeline

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.11'
      - name: Install dependencies
        run: |
          pip install -r requirements.txt
          pip install -r requirements-dev.txt
      - name: Run linting
        run: |
          flake8 .
          mypy .
          black --check .
      - name: Run tests
        run: |
          pytest tests/ --cov=. --cov-report=xml
      - name: Upload coverage
        uses: codecov/codecov-action@v3

  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          severity: 'CRITICAL,HIGH'

  build:
    needs: [test, security-scan]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build Docker image
        run: |
          docker build -t registry.idbi.com/msme-platform/${{ github.event.repository.name }}:${{ github.sha }} .
      - name: Push to registry
        run: |
          echo ${{ secrets.REGISTRY_PASSWORD }} | docker login registry.idbi.com -u ${{ secrets.REGISTRY_USERNAME }} --password-stdin
          docker push registry.idbi.com/msme-platform/${{ github.event.repository.name }}:${{ github.sha }}

  deploy-staging:
    needs: build
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to staging
        run: |
          kubectl set image deployment/${{ github.event.repository.name }} \
            ${{ github.event.repository.name }}=registry.idbi.com/msme-platform/${{ github.event.repository.name }}:${{ github.sha }} \
            --namespace=staging
      - name: Run integration tests
        run: |
          pytest tests/integration/ --env=staging

  deploy-production:
    needs: deploy-staging
    runs-on: ubuntu-latest
    environment: production
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to production (Blue-Green)
        run: |
          # Deploy to green environment
          kubectl set image deployment/${{ github.event.repository.name }}-green \
            ${{ github.event.repository.name }}=registry.idbi.com/msme-platform/${{ github.event.repository.name }}:${{ github.sha }} \
            --namespace=production
          
          # Wait for green to be ready
          kubectl rollout status deployment/${{ github.event.repository.name }}-green --namespace=production
          
          # Run smoke tests
          ./scripts/smoke-tests.sh green
          
          # Switch traffic
          kubectl patch service ${{ github.event.repository.name }} \
            -p '{"spec":{"selector":{"version":"green"}}}' \
            --namespace=production
          
          # Rename old blue to be the new blue
          kubectl rollout restart deployment/${{ github.event.repository.name }}-blue --namespace=production
```

---

### Terraform Infrastructure

```hcl
# main.tf
provider "aws" {
  region = "ap-south-1"
}

module "vpc" {
  source = "./modules/vpc"
  
  vpc_cidr = "10.0.0.0/16"
  azs      = ["ap-south-1a", "ap-south-1b", "ap-south-1c"]
  
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24", "10.0.103.0/24"]
  
  enable_nat_gateway = true
  single_nat_gateway = false
}

module "eks" {
  source = "./modules/eks"
  
  cluster_name    = "msme-platform-prod"
  cluster_version = "1.27"
  
  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets
  
  node_groups = {
    general = {
      instance_types = ["m5.xlarge"]
      min_size       = 3
      max_size       = 10
      desired_size   = 5
    }
    ml = {
      instance_types = ["p3.2xlarge"]
      min_size       = 2
      max_size       = 5
      desired_size   = 3
      labels = {
        workload = "ml"
      }
    }
  }
}

module "rds" {
  source = "./modules/rds"
  
  identifier = "msme-platform-db"
  engine     = "postgres"
  engine_version = "15.4"
  
  instance_class = "db.r6g.xlarge"
  
  allocated_storage     = 100
  max_allocated_storage = 500
  
  multi_az               = true
  db_subnet_group_name   = module.vpc.database_subnet_group
  vpc_security_group_ids = [module.vpc.database_security_group_id]
  
  backup_retention_period = 30
  backup_window          = "03:00-04:00"
  maintenance_window     = "Mon:04:00-Mon:05:00"
  
  performance_insights_enabled = true
  monitoring_interval          = 60
}

module "elasticache" {
  source = "./modules/elasticache"
  
  cluster_id = "msme-platform-cache"
  engine     = "redis"
  engine_version = "7.0"
  
  node_type            = "cache.r6g.large"
  num_cache_nodes      = 3
  
  subnet_group_name  = module.vpc.cache_subnet_group
  security_group_ids = [module.vpc.cache_security_group_id]
}

module "msk" {
  source = "./modules/msk"
  
  cluster_name = "msme-platform-kafka"
  
  kafka_version = "3.5.1"
  
  number_of_broker_nodes = 3
  broker_instance_type    = "kafka.m5.large"
  
  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets
}
```

---

### Monitoring Configuration

```yaml
# prometheus-config.yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alerts/*.yml"

scrape_configs:
  - job_name: 'health-service'
    kubernetes_sd_configs:
      - role: pod
        namespaces:
          names:
            - production
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_label_app]
        regex: health-service
        action: keep

# grafana-dashboard.json
{
  "dashboard": {
    "title": "MSME Platform Overview",
    "panels": [
      {
        "title": "API Latency",
        "type": "graph",
        "targets": [{
          "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))"
        }]
      },
      {
        "title": "Error Rate",
        "type": "stat",
        "targets": [{
          "expr": "sum(rate(http_requests_total{status=~'5..'}[5m])) / sum(rate(http_requests_total[5m])) * 100"
        }]
      },
      {
        "title": "Active Users",
        "type": "stat",
        "targets": [{
          "expr": "count(up{job='health-service'} == 1)"
        }]
      },
      {
        "title": "Model Accuracy",
        "type": "gauge",
        "targets": [{
          "expr": "ml_model_accuracy{model='scoring'}"
        }]
      }
    ]
  }
}
```

---

### Estimated Development Time
- **CI/CD Pipeline**: 1 week
- **Kubernetes Setup**: 1 week
- **Terraform Infrastructure**: 1 week
- **Monitoring**: 3 days
- **Total**: 3.5 weeks

---

### Hackathon Priority
**MEDIUM** - Important for production readiness

---

### Difficulty Level
**MEDIUM** - Standard DevOps practices

---

### Expected Judge Impression
**GOOD** - Demonstrates production readiness
