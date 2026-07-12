# ENHANCEMENT 3: DATA PIPELINE & INTEGRATIONS
## Real-Time Data Ingestion with AA/ULI/OCEN Integration

---

### Feature Name
**Unified Data Ingestion Pipeline with Account Aggregator, ULI, and OCEN Integration**

---

### Problem It Solves
MSME data is scattered across multiple silos (GST, UPI, EPFO, banks). No unified view exists for credit assessment. Manual data collection is slow and error-prone.

---

### Why It Is Needed
- RBI mandates Account Aggregator framework adoption
- ULI (Unified Lending Interface) enables instant credit
- OCEN (Open Credit Enablement Network) standardizes lending
- 6+ data sources need real-time aggregation

---

### Relation to Problem Statement
Directly implements the data aggregation and integration requirements stated in the problem statement.

---

### Business Benefits
- 90% reduction in data collection time
- Real-time credit assessment capability
- Standardized data format across sources
- API-first architecture for future integrations

---

### Technical Benefits
- Event-driven architecture with Kafka
- Sub-second data processing
- Automatic retry and error handling
- Data quality validation pipeline

---

### Implementation Steps

```
Phase 1: Data Connectors (Week 1-2)
├── GST Portal API Integration
├── UPI Transaction Fetcher
├── EPFO Employer API
├── MCA Company Registry
└── Bank Statement Parser (OCR)

Phase 2: AA/ULI/OCEN (Week 3-4)
├── Account Aggregator Gateway
├── ULI API Integration
├── OCEN Protocol Implementation
├── Consent Management System
└── Data Standardization Layer

Phase 3: Pipeline Orchestration (Week 5)
├── Apache Kafka Setup
├── Real-time Streaming
├── Data Quality Checks
├── Error Handling & Retry
└── Monitoring & Alerting

Phase 4: Data Storage (Week 6)
├── TimescaleDB for Time Series
├── MongoDB for Documents
├── Redis for Caching
├── Elasticsearch for Search
└── Data Retention Policies
```

---

### Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    DATA SOURCES                                  │
│                                                                  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │
│  │  GST    │ │   UPI   │ │  EPFO   │ │  MCA    │ │  Bank   │ │
│  │ Portal  │ │  NPCI   │ │   API   │ │Registry │ │Statements│ │
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ │
└───────┼──────────┼──────────┼──────────┼──────────┼────────────┘
        │          │          │          │          │
┌───────┴──────────┴──────────┴──────────┴──────────┴────────────┐
│                    INGESTION LAYER                              │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │           Apache Kafka Event Streaming                   │    │
│  │                                                          │    │
│  │  Topics:                                                │    │
│  │  ├── raw.gst.transactions                               │    │
│  │  ├── raw.upi.transactions                               │    │
│  │  ├── raw.epfo.contributions                             │    │
│  │  ├── raw.mca.company_data                               │    │
│  │  ├── raw.bank.statements                                │    │
│  │  ├── processed.features                                 │    │
│  │  ├── validated.scores                                   │    │
│  │  └── alerts.risk_events                                 │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │           Data Quality Engine                            │    │
│  │  • Schema validation                                    │    │
│  │  • Missing value detection                              │    │
│  │  • Anomaly flagging                                     │    │
│  │  • Data freshness check                                 │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└────────────────────────────────┬─────────────────────────────────┘
                                 │
┌────────────────────────────────┴─────────────────────────────────┐
│                    PROCESSING LAYER                              │
│                                                                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │  Stream     │ │  Batch      │ │  Feature    │              │
│  │  Processor  │ │  Processor  │ │  Engineering│              │
│  │  (Flink)    │ │  (Spark)    │ │  (Python)   │              │
│  └─────────────┘ └─────────────┘ └─────────────┘              │
│                                                                  │
└────────────────────────────────┬─────────────────────────────────┘
                                 │
┌────────────────────────────────┴─────────────────────────────────┐
│                    STORAGE LAYER                                 │
│                                                                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │PostgreSQL│ │TimescaleDB│ │  Redis   │ │ MinIO    │          │
│  │ (Master) │ │(Time Srs)│ │ (Cache)  │ │(Objects) │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### Account Aggregator Integration

```python
class AccountAggregatorService:
    """
    RBI Account Aggregator Framework Integration
    """
    
    def __init__(self):
        self.aa_gateway = AAGateway(config.AA_API_KEY)
        self.consent_manager = ConsentManager()
    
    async def initiate_data_fetch(self, msme_id: str, data_types: list) -> dict:
        # 1. Check existing consent
        consent = await self.consent_manager.check_consent(msme_id, data_types)
        
        if not consent or consent.is_expired():
            # 2. Request new consent
            consent_request = await self.aa_gateway.create_consent_request(
                msme_id=msme_id,
                data_types=data_types,
                purpose='credit_assessment',
                validity_days=90
            )
            
            # 3. Wait for user consent (async callback)
            return {
                'status': 'consent_pending',
                'consent_request_id': consent_request.id,
                'callback_url': consent_request.callback_url
            }
        
        # 4. Fetch data using existing consent
        data = await self.aa_gateway.fetch_data(
            consent_id=consent.id,
            data_types=data_types
        )
        
        # 5. Validate and standardize
        standardized_data = self.standardize_data(data)
        
        # 6. Store in feature store
        await self.feature_store.store(msme_id, standardized_data)
        
        return {
            'status': 'success',
            'data': standardized_data,
            'freshness': datetime.now()
        }
    
    def standardize_data(self, raw_data: dict) -> dict:
        """
        Standardizes data from different AA providers
        """
        return {
            'accounts': self.parse_accounts(raw_data.get('accounts', [])),
            'transactions': self.parse_transactions(raw_data.get('transactions', [])),
            'balances': self.parse_balances(raw_data.get('balances', [])),
            'metadata': {
                'source': raw_data.get('provider'),
                'fetch_time': datetime.now(),
                'schema_version': '1.0'
            }
        }
```

---

### ULI Integration

```python
class ULIService:
    """
    Unified Lending Interface Integration
    """
    
    def __init__(self):
        self.uli_gateway = ULIGateway(config.ULI_API_KEY)
        self.data_mapper = ULIDataMapper()
    
    async def submit_loan_application(
        self, 
        msme_id: str, 
        loan_details: dict,
        health_card: FinancialHealthCard
    ) -> dict:
        
        # 1. Map data to ULI format
        uli_payload = self.data_mapper.to_ulformat(
            msme_id=msme_id,
            loan_details=loan_details,
            health_card=health_card
        )
        
        # 2. Submit to ULI
        response = await self.uli_gateway.submit_application(uli_payload)
        
        # 3. Track status
        await self.tracking_service.track(response.application_id)
        
        return {
            'application_id': response.application_id,
            'status': response.status,
            'estimated_decision_time': response.eta,
            'lenders_notified': response.lender_count
        }
    
    async def receive_decision(self, application_id: str) -> dict:
        """
        Callback from ULI when lenders respond
        """
        decision = await self.uli_gateway.get_decision(application_id)
        
        # Update MSME record
        await self.update_msme_status(decision)
        
        # Notify loan officer
        await self.notification_service.notify(decision)
        
        return decision
```

---

### OCEN Integration

```python
class OCENService:
    """
    Open Credit Enablement Network Integration
    """
    
    def __init__(self):
        self.ocen_gateway = OCENGateway(config.OCEN_API_KEY)
        self.loan_contract_generator = LoanContractGenerator()
    
    async def create_loan_offer(
        self,
        msme_id: str,
        lender_id: str,
        loan_terms: dict
    ) -> dict:
        
        # 1. Generate loan contract
        contract = await self.loan_contract_generator.generate(
            msme_id=msme_id,
            lender_id=lender_id,
            terms=loan_terms
        )
        
        # 2. Submit to OCEN
        offer = await self.ocen_gateway.create_offer(
            contract=contract,
            collateral=loan_terms.get('collateral'),
            guarantee=loan_terms.get('guarantee')
        )
        
        # 3. Notify borrower
        await self.notification_service.notify_offer(offer)
        
        return {
            'offer_id': offer.id,
            'terms': offer.terms,
            'valid_until': offer.expiry,
            'accept_url': offer.accept_url
        }
    
    async def disburse_loan(self, offer_id: str, acceptance: dict) -> dict:
        """
        Process loan disbursement through OCEN
        """
        # 1. Verify acceptance
        verification = await self.verify_acceptance(offer_id, acceptance)
        
        # 2. Initiate disbursement
        disbursement = await self.ocen_gateway.disburse(
            offer_id=offer_id,
            bank_account=acceptance.bank_account
        )
        
        # 3. Update records
        await self.loan_service.update_status(disbursement.loan_id, 'disbursed')
        
        # 4. Start repayment tracking
        await self.repayment_service.start_tracking(disbursement.loan_id)
        
        return disbursement
```

---

### Data Pipeline Orchestration

```python
class DataPipelineOrchestrator:
    """
    Manages end-to-end data flow
    """
    
    def __init__(self):
        self.kafka_producer = KafkaProducer()
        self.kafka_consumer = KafkaConsumer()
        self.data_validators = {
            'gst': GSTValidator(),
            'upi': UPIValidator(),
            'epfo': EPFOValidator(),
            'bank': BankStatementValidator()
        }
    
    async def ingest_data(self, source: str, msme_id: str, data: dict):
        # 1. Validate data
        validator = self.data_validators.get(source)
        if not validator.validate(data):
            raise DataValidationError(f"Invalid {source} data")
        
        # 2. Publish to Kafka
        await self.kafka_producer.send(
            topic=f'raw.{source}.data',
            key=msme_id,
            value={
                'source': source,
                'msme_id': msme_id,
                'data': data,
                'timestamp': datetime.now().isoformat(),
                'schema_version': '1.0'
            }
        )
        
        # 3. Wait for processing
        result = await self.wait_for_processing(msme_id, source)
        
        return result
    
    async def process_stream(self):
        """
        Kafka stream processor
        """
        async for message in self.kafka_consumer:
            source = message.topic.split('.')[1]
            msme_id = message.key
            data = message.value
            
            try:
                # 1. Transform data
                transformed = self.transform(data)
                
                # 2. Compute features
                features = await self.feature_engineer.compute(transformed)
                
                # 3. Store in feature store
                await self.feature_store.store(msme_id, features)
                
                # 4. Trigger scoring if enough data
                if await self.has_sufficient_data(msme_id):
                    await self.scoring_engine.trigger_scoring(msme_id)
                
                # 5. Publish processed data
                await self.kafka_producer.send(
                    topic='processed.features',
                    key=msme_id,
                    value=features
                )
                
            except Exception as e:
                await self.handle_error(e, message)
```

---

### Database Changes

```sql
-- Data Source Registry
CREATE TABLE data_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_name VARCHAR(50) NOT NULL UNIQUE,
    api_endpoint VARCHAR(255),
    api_key_ref VARCHAR(100),
    status VARCHAR(20) DEFAULT 'active',
    last_sync TIMESTAMPTZ,
    sync_frequency INTERVAL,
    error_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Data Ingestion Logs
CREATE TABLE ingestion_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL,
    source VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    records_count INTEGER,
    error_message TEXT,
    processing_time_ms INTEGER,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_ingestion_logs_lookup 
ON ingestion_logs(msme_id, source, created_at DESC);

-- Consent Records
CREATE TABLE consent_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL,
    data_provider VARCHAR(50) NOT NULL,
    consent_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    granted_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    consent_artifact JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ULI Applications
CREATE TABLE uli_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL,
    application_id VARCHAR(100) UNIQUE,
    status VARCHAR(30) NOT NULL,
    loan_amount DECIMAL(15,2),
    loan_purpose VARCHAR(100),
    lenders_notified INTEGER,
    responses_received INTEGER,
    submitted_at TIMESTAMPTZ,
    decision_at TIMESTAMPTZ,
    decision VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- OCEN Offers
CREATE TABLE ocen_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL,
    lender_id UUID NOT NULL,
    offer_id VARCHAR(100) UNIQUE,
    loan_amount DECIMAL(15,2),
    interest_rate DECIMAL(5,2),
    tenure_months INTEGER,
    emi_amount DECIMAL(15,2),
    status VARCHAR(20) NOT NULL,
    valid_until TIMESTAMPTZ,
    accepted_at TIMESTAMPTZ,
    disbursed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

### Frontend Changes

```
┌─────────────────────────────────────────────────────────────────┐
│                 DATA INTEGRATION DASHBOARD                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  CONNECTED DATA SOURCES                                  │    │
│  │                                                          │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐      │    │
│  │  │  GST    │ │   UPI   │ │  EPFO   │ │  Bank   │      │    │
│  │  │   ✅    │ │   ✅    │ │   ✅    │ │   ⏳    │      │    │
│  │  │ Synced  │ │ Synced  │ │ Synced  │ │Pending  │      │    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘      │    │
│  │                                                          │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐      │    │
│  │  │  MCA    │ │  CIBIL  │ │  AA     │ │  ULI    │      │    │
│  │  │   ✅    │ │   ⚠️    │ │   ✅    │ │   ✅    │      │    │
│  │  │ Synced  │ │ Partial │ │ Active  │ │ Active  │      │    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  CONSENT MANAGEMENT                                      │    │
│  │                                                          │    │
│  │  [List of active consents with expiry dates]            │    │
│  │  [Renew/Revoke buttons]                                 │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  DATA FRESHNESS                                         │    │
│  │                                                          │    │
│  │  Last Updated: 2 minutes ago                            │    │
│  │  Next Scheduled: 15 minutes                             │    │
│  │  [Refresh Now]                                           │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### Estimated Development Time
- **Data Connectors**: 2 weeks
- **AA/ULI/OCEN Integration**: 2 weeks
- **Pipeline Orchestration**: 1 week
- **Data Storage**: 1 week
- **Total**: 6 weeks

---

### Hackathon Priority
**CRITICAL** - Foundation for all features

---

### Difficulty Level
**HIGH** - Complex API integrations and real-time processing

---

### Expected Judge Impression
**EXCELLENT** - Demonstrates integration expertise and RBI ecosystem knowledge
