# AI Smart Business Decision System
## Anti-Gravity Inspired Logistics Intelligence — PRD v1.0

> **Type:** Backend-Only | **Status:** Implementation-Ready | **Stack:** Spring Boot · Java 17 · PostgreSQL

---

## 1. Project Objective

AI-driven backend engine for drone-based logistics. Delivers **actionable decisions** — not raw data — across four domains:

| Domain | Goal |
|---|---|
| Inventory Management | Monitor stock, trigger restock alerts |
| Demand Prediction | Analyse sales velocity, forecast units |
| Route Optimisation | Compute optimal drone delivery paths |
| Strategic Decisions | Pricing, quality, and fulfilment recommendations |

---

## 2. Tech Stack

| Component | Technology | Why |
|---|---|---|
| Framework | Spring Boot 3.x | Auto-config, embedded Tomcat, actuators |
| Language | Java 17 LTS | Strong typing, streams, enterprise-grade |
| API Layer | Spring MVC REST | Annotation-driven, Jackson JSON, OpenAPI 3 |
| Database | PostgreSQL 15 | JSONB payloads, window functions, ACID |
| ORM | Spring Data JPA | Repository abstraction, native query support |
| AI Bridge | RestTemplate / WebClient | HTTP calls to Python FastAPI microservice |
| Build | Maven 3.9 | Dependency management, reproducible builds |

---

## 3. Architecture

### 3.1 Layers

```
HTTP Request
    ↓
@RestController     ← validate DTO, route request
    ↓
@Service            ← business logic, rule engine, AI orchestration
    ↓
@Repository (JPA)   ← DB read/write only, zero business logic
    ↓
PostgreSQL
```

### 3.2 Package Structure

```
com.antigravity.logistics
├── controller/        REST controllers (one per domain)
├── service/           Business logic + AI orchestration
├── repository/        JPA repositories
├── model/entity/      JPA-mapped domain objects
├── dto/               Request & Response POJOs
├── ai/                AI microservice HTTP client
└── config/            Beans, RestTemplate, CORS
```

### 3.3 Data Flow

1. Controller receives HTTP request → validates DTO
2. Delegates to Service layer
3. Service reads/writes via Repository
4. Service applies Rule Engine
5. Service calls AI microservice (if needed)
6. Service assembles `DecisionResponseDTO`
7. Controller returns HTTP 200 + JSON

---

## 4. Database Design

### Relationships
```
products (1) ──< inventory (N)
products (1) ──< sales     (N)
products (1) ──< reviews   (N)
orders   (1) ──< delivery_routes (N)
```

### products
| Column | Type | Constraint | Purpose |
|---|---|---|---|
| product_id | BIGINT | PK, AUTO | Surrogate key |
| name | VARCHAR(200) | NOT NULL | Display name |
| category | VARCHAR(100) | NOT NULL | Demand grouping |
| unit_price | DECIMAL(10,2) | NOT NULL | Base selling price |
| restock_threshold | INT | DEFAULT 20 | Minimum safe stock |
| created_at | TIMESTAMP | DEFAULT NOW() | Audit |

### inventory
| Column | Type | Constraint | Purpose |
|---|---|---|---|
| inventory_id | BIGINT | PK | Surrogate key |
| product_id | BIGINT | FK → products | Owning product |
| quantity_on_hand | INT | NOT NULL | Current stock |
| warehouse_zone | VARCHAR(50) | — | Drone zone |
| last_updated | TIMESTAMP | ON UPDATE | Staleness check |

### sales
| Column | Type | Constraint | Purpose |
|---|---|---|---|
| sale_id | BIGINT | PK | Surrogate key |
| product_id | BIGINT | FK → products | Sold product |
| quantity_sold | INT | NOT NULL | Units per transaction |
| sale_date | DATE | NOT NULL | Temporal axis |
| revenue | DECIMAL(12,2) | NOT NULL | qty × price snapshot |
| channel | VARCHAR(50) | — | DRONE / STORE / ONLINE |

### reviews
| Column | Type | Constraint | Purpose |
|---|---|---|---|
| review_id | BIGINT | PK | Surrogate key |
| product_id | BIGINT | FK → products | Reviewed product |
| review_text | TEXT | NOT NULL | NLP input |
| rating | SMALLINT | CHECK(1–5) | Numeric baseline |
| sentiment_score | DECIMAL(3,2) | NULLABLE | AI-computed polarity |
| review_date | DATE | NOT NULL | Temporal context |

### delivery_routes
| Column | Type | Constraint | Purpose |
|---|---|---|---|
| route_id | BIGINT | PK | Surrogate key |
| order_id | BIGINT | FK → orders | Associated order |
| waypoints | JSONB | NOT NULL | Ordered lat/lng array |
| total_distance_km | DECIMAL(6,2) | — | Route cost metric |
| estimated_time_min | INT | — | Delivery ETA |
| status | ENUM | PENDING/ACTIVE/DONE | Lifecycle state |

---

## 5. REST API Design

### 5.1 Product & Inventory

| Endpoint | Method | Input | Output |
|---|---|---|---|
| `/api/products` | POST | `ProductRequestDTO` | `ProductResponseDTO` 201 |
| `/api/products/{id}` | GET | Path: product_id | `ProductResponseDTO` |
| `/api/products` | GET | Query: category, page | `Page<ProductResponseDTO>` |
| `/api/inventory/{productId}/stock` | PUT | `{ quantity_delta, reason }` | `InventoryResponseDTO` |
| `/api/inventory/{productId}` | GET | Path: product_id | `InventoryResponseDTO` + low_stock flag |
| `/api/inventory/alerts` | GET | Query: zone | `List<LowStockAlertDTO>` |

### 5.2 Sales & Analytics

| Endpoint | Method | Input | Output |
|---|---|---|---|
| `/api/sales` | POST | `SaleRequestDTO` | `SaleResponseDTO` 201 |
| `/api/analytics/demand` | GET | Query: product_id, from, to | `DemandAnalyticsDTO` |
| `/api/analytics/revenue` | GET | Query: from, to, group_by | `RevenueReportDTO` |
| `/api/analytics/top-products` | GET | Query: limit, period | `List<TopProductDTO>` |

### 5.3 Reviews

| Endpoint | Method | Input | Output |
|---|---|---|---|
| `/api/reviews` | POST | `ReviewRequestDTO` | `ReviewResponseDTO` + sentiment_score |
| `/api/reviews/{productId}/summary` | GET | Path: product_id | `ReviewSummaryDTO` |

### 5.4 AI Decisions ← Core

| Endpoint | Method | Input | Output |
|---|---|---|---|
| `/api/decisions/{productId}` | GET | Path: product_id | `DecisionResponseDTO` |
| `/api/decisions/bulk` | GET | Query: category, zone | `List<DecisionResponseDTO>` |
| `/api/routes/optimize` | POST | `RouteRequestDTO` | `OptimisedRouteDTO` |

### 5.5 DecisionResponseDTO Schema

```json
{
  "productId": 42,
  "productName": "Drone Battery Pack X1",
  "timestamp": "2025-01-15T10:30:00Z",
  "stockStatus": "LOW",
  "currentStock": 5,
  "restockThreshold": 20,
  "demandTrend": "RISING",
  "salesVelocity": 12.4,
  "averageSentiment": 2.1,
  "decisions": [
    { "action": "RESTOCK_URGENT",    "priority": "HIGH",   "reason": "Stock 5 < threshold 20" },
    { "action": "REVIEW_QUALITY",    "priority": "MEDIUM", "reason": "Avg sentiment 2.1 < 3.0" },
    { "action": "INCREASE_FORECAST", "priority": "LOW",    "reason": "Sales velocity +40% WoW" }
  ],
  "routeOptimization": {
    "recommended": true,
    "suggestedZones": ["ZONE_A", "ZONE_C"]
  }
}
```

---

## 6. Core Business Logic

### 6.1 Stock Monitoring

```
InventoryService.checkStock(productId)
  └─ runs after every POST /api/sales
  └─ IF quantity_on_hand < restock_threshold → emit StockLowEvent
  └─ @Scheduled sweep every 15 min (all products)
```

### 6.2 Demand Analysis

```
DemandAnalysisService.analyse(productId, window=8weeks)
  1. SELECT SUM(quantity_sold) GROUP BY week  →  weekly_series[]
  2. velocity = current_week / AVG(prior_4_weeks)
  3. IF velocity > 1.3  → trend = RISING
     IF velocity < 0.7  → trend = DECLINING
     ELSE               → trend = STABLE
  4. forecast = avg_velocity × seasonal_index
```

### 6.3 Decision Engine (Rule-Based)

| # | IF | THEN | Priority |
|---|---|---|---|
| 1 | stock < threshold AND trend = RISING | RESTOCK_URGENT | HIGH |
| 2 | stock < threshold AND trend ≠ RISING | RESTOCK | MEDIUM |
| 3 | avg_sentiment < 3.0 AND review_count > 5 | REVIEW_QUALITY | MEDIUM |
| 4 | velocity < 0.7 AND margin > 0.3 | REDUCE_PRICE | LOW |
| 5 | velocity > 1.5 AND stock healthy | INCREASE_FORECAST | LOW |
| 6 | route_efficiency < 0.7 | REROUTE_DRONES | MEDIUM |

### 6.4 Route Optimisation

```
RouteOptimizationService (A* variant)
  Nodes  : warehouse zones
  Edges  : distance + battery_cost_weight
  h(n)   : Euclidean distance to destination
  Output : ordered waypoints + total_distance_km + estimated_time_min
```

---

## 7. AI Microservice Integration

**Transport:** Spring Boot → HTTP → Python FastAPI (stateless)

| AI Endpoint | Request | Response |
|---|---|---|
| `POST /ai/sentiment` | `{ "text": "...", "product_id": 42 }` | `{ "score": 2.1, "label": "NEGATIVE" }` |
| `POST /ai/demand-forecast` | `{ "product_id": 42, "history": [12,15,11,...] }` | `{ "forecast": 18, "confidence": 0.87 }` |

**Client config (AiServiceClient.java):**
```
- Bean: @Component RestTemplate
- Timeout: connect=5s, read=10s
- Fallback: on error → rule-only decision, log WARN, no HTTP 500
- Base URL: application.properties → ai.service.base-url
```

---

## 8. AI Concept Mapping

| Unit | Concept | Implementation | Example |
|---|---|---|---|
| UNIT 1 | Intelligent Agent | Backend = Percept→Decide→Act; API call = perception; Service = decision; Response = action | POST /sales → spike detected → RESTOCK |
| UNIT 2 | Search & Optimisation | A* in RouteOptimizationService; heuristic = distance + battery cost | Depot→Zone A→Zone C (shortest path) |
| UNIT 3 | Strategy Comparison | PricingStrategyService compares Penetration / Competitive / Skimming by margin × demand score | Low margin + falling sales → REDUCE_PRICE |
| UNIT 4 | Rule-Based Expert System | DecisionEngine applies ordered IF-THEN rules (§6.3) | stock=5, threshold=20 → RESTOCK_URGENT |
| UNIT 5 | NLP / Sentiment | Backend POSTs review text to Python BERT service; score stored in reviews.sentiment_score | "Poor packaging" → score 1.8 → REVIEW_QUALITY |

---# AI Smart Business Decision System
## Anti-Gravity Inspired Logistics Intelligence — PRD v1.0

> **Type:** Backend-Only | **Status:** Implementation-Ready | **Stack:** Spring Boot · Java 17 · PostgreSQL

---

## 1. Project Objective

AI-driven backend engine for drone-based logistics. Delivers **actionable decisions** — not raw data — across four domains:

| Domain | Goal |
|---|---|
| Inventory Management | Monitor stock, trigger restock alerts |
| Demand Prediction | Analyse sales velocity, forecast units |
| Route Optimisation | Compute optimal drone delivery paths |
| Strategic Decisions | Pricing, quality, and fulfilment recommendations |

---

## 2. Tech Stack

| Component | Technology | Why |
|---|---|---|
| Framework | Spring Boot 3.x | Auto-config, embedded Tomcat, actuators |
| Language | Java 17 LTS | Strong typing, streams, enterprise-grade |
| API Layer | Spring MVC REST | Annotation-driven, Jackson JSON, OpenAPI 3 |
| Database | PostgreSQL 15 | JSONB payloads, window functions, ACID |
| ORM | Spring Data JPA | Repository abstraction, native query support |
| AI Bridge | RestTemplate / WebClient | HTTP calls to Python FastAPI microservice |
| Build | Maven 3.9 | Dependency management, reproducible builds |

---

## 3. Architecture

### 3.1 Layers

```
HTTP Request
    ↓
@RestController     ← validate DTO, route request
    ↓
@Service            ← business logic, rule engine, AI orchestration
    ↓
@Repository (JPA)   ← DB read/write only, zero business logic
    ↓
PostgreSQL
```

### 3.2 Package Structure

```
com.antigravity.logistics
├── controller/        REST controllers (one per domain)
├── service/           Business logic + AI orchestration
├── repository/        JPA repositories
├── model/entity/      JPA-mapped domain objects
├── dto/               Request & Response POJOs
├── ai/                AI microservice HTTP client
└── config/            Beans, RestTemplate, CORS
```

### 3.3 Data Flow

1. Controller receives HTTP request → validates DTO
2. Delegates to Service layer
3. Service reads/writes via Repository
4. Service applies Rule Engine
5. Service calls AI microservice (if needed)
6. Service assembles `DecisionResponseDTO`
7. Controller returns HTTP 200 + JSON

---

## 4. Database Design

### Relationships
```
products (1) ──< inventory (N)
products (1) ──< sales     (N)
products (1) ──< reviews   (N)
orders   (1) ──< delivery_routes (N)
```

### products
| Column | Type | Constraint | Purpose |
|---|---|---|---|
| product_id | BIGINT | PK, AUTO | Surrogate key |
| name | VARCHAR(200) | NOT NULL | Display name |
| category | VARCHAR(100) | NOT NULL | Demand grouping |
| unit_price | DECIMAL(10,2) | NOT NULL | Base selling price |
| restock_threshold | INT | DEFAULT 20 | Minimum safe stock |
| created_at | TIMESTAMP | DEFAULT NOW() | Audit |

### inventory
| Column | Type | Constraint | Purpose |
|---|---|---|---|
| inventory_id | BIGINT | PK | Surrogate key |
| product_id | BIGINT | FK → products | Owning product |
| quantity_on_hand | INT | NOT NULL | Current stock |
| warehouse_zone | VARCHAR(50) | — | Drone zone |
| last_updated | TIMESTAMP | ON UPDATE | Staleness check |

### sales
| Column | Type | Constraint | Purpose |
|---|---|---|---|
| sale_id | BIGINT | PK | Surrogate key |
| product_id | BIGINT | FK → products | Sold product |
| quantity_sold | INT | NOT NULL | Units per transaction |
| sale_date | DATE | NOT NULL | Temporal axis |
| revenue | DECIMAL(12,2) | NOT NULL | qty × price snapshot |
| channel | VARCHAR(50) | — | DRONE / STORE / ONLINE |

### reviews
| Column | Type | Constraint | Purpose |
|---|---|---|---|
| review_id | BIGINT | PK | Surrogate key |
| product_id | BIGINT | FK → products | Reviewed product |
| review_text | TEXT | NOT NULL | NLP input |
| rating | SMALLINT | CHECK(1–5) | Numeric baseline |
| sentiment_score | DECIMAL(3,2) | NULLABLE | AI-computed polarity |
| review_date | DATE | NOT NULL | Temporal context |

### delivery_routes
| Column | Type | Constraint | Purpose |
|---|---|---|---|
| route_id | BIGINT | PK | Surrogate key |
| order_id | BIGINT | FK → orders | Associated order |
| waypoints | JSONB | NOT NULL | Ordered lat/lng array |
| total_distance_km | DECIMAL(6,2) | — | Route cost metric |
| estimated_time_min | INT | — | Delivery ETA |
| status | ENUM | PENDING/ACTIVE/DONE | Lifecycle state |

---

## 5. REST API Design

### 5.1 Product & Inventory

| Endpoint | Method | Input | Output |
|---|---|---|---|
| `/api/products` | POST | `ProductRequestDTO` | `ProductResponseDTO` 201 |
| `/api/products/{id}` | GET | Path: product_id | `ProductResponseDTO` |
| `/api/products` | GET | Query: category, page | `Page<ProductResponseDTO>` |
| `/api/inventory/{productId}/stock` | PUT | `{ quantity_delta, reason }` | `InventoryResponseDTO` |
| `/api/inventory/{productId}` | GET | Path: product_id | `InventoryResponseDTO` + low_stock flag |
| `/api/inventory/alerts` | GET | Query: zone | `List<LowStockAlertDTO>` |

### 5.2 Sales & Analytics

| Endpoint | Method | Input | Output |
|---|---|---|---|
| `/api/sales` | POST | `SaleRequestDTO` | `SaleResponseDTO` 201 |
| `/api/analytics/demand` | GET | Query: product_id, from, to | `DemandAnalyticsDTO` |
| `/api/analytics/revenue` | GET | Query: from, to, group_by | `RevenueReportDTO` |
| `/api/analytics/top-products` | GET | Query: limit, period | `List<TopProductDTO>` |

### 5.3 Reviews

| Endpoint | Method | Input | Output |
|---|---|---|---|
| `/api/reviews` | POST | `ReviewRequestDTO` | `ReviewResponseDTO` + sentiment_score |
| `/api/reviews/{productId}/summary` | GET | Path: product_id | `ReviewSummaryDTO` |

### 5.4 AI Decisions ← Core

| Endpoint | Method | Input | Output |
|---|---|---|---|
| `/api/decisions/{productId}` | GET | Path: product_id | `DecisionResponseDTO` |
| `/api/decisions/bulk` | GET | Query: category, zone | `List<DecisionResponseDTO>` |
| `/api/routes/optimize` | POST | `RouteRequestDTO` | `OptimisedRouteDTO` |

### 5.5 DecisionResponseDTO Schema

```json
{
  "productId": 42,
  "productName": "Drone Battery Pack X1",
  "timestamp": "2025-01-15T10:30:00Z",
  "stockStatus": "LOW",
  "currentStock": 5,
  "restockThreshold": 20,
  "demandTrend": "RISING",
  "salesVelocity": 12.4,
  "averageSentiment": 2.1,
  "decisions": [
    { "action": "RESTOCK_URGENT",    "priority": "HIGH",   "reason": "Stock 5 < threshold 20" },
    { "action": "REVIEW_QUALITY",    "priority": "MEDIUM", "reason": "Avg sentiment 2.1 < 3.0" },
    { "action": "INCREASE_FORECAST", "priority": "LOW",    "reason": "Sales velocity +40% WoW" }
  ],
  "routeOptimization": {
    "recommended": true,
    "suggestedZones": ["ZONE_A", "ZONE_C"]
  }
}
```

---

## 6. Core Business Logic

### 6.1 Stock Monitoring

```
InventoryService.checkStock(productId)
  └─ runs after every POST /api/sales
  └─ IF quantity_on_hand < restock_threshold → emit StockLowEvent
  └─ @Scheduled sweep every 15 min (all products)
```

### 6.2 Demand Analysis

```
DemandAnalysisService.analyse(productId, window=8weeks)
  1. SELECT SUM(quantity_sold) GROUP BY week  →  weekly_series[]
  2. velocity = current_week / AVG(prior_4_weeks)
  3. IF velocity > 1.3  → trend = RISING
     IF velocity < 0.7  → trend = DECLINING
     ELSE               → trend = STABLE
  4. forecast = avg_velocity × seasonal_index
```

### 6.3 Decision Engine (Rule-Based)

| # | IF | THEN | Priority |
|---|---|---|---|
| 1 | stock < threshold AND trend = RISING | RESTOCK_URGENT | HIGH |
| 2 | stock < threshold AND trend ≠ RISING | RESTOCK | MEDIUM |
| 3 | avg_sentiment < 3.0 AND review_count > 5 | REVIEW_QUALITY | MEDIUM |
| 4 | velocity < 0.7 AND margin > 0.3 | REDUCE_PRICE | LOW |
| 5 | velocity > 1.5 AND stock healthy | INCREASE_FORECAST | LOW |
| 6 | route_efficiency < 0.7 | REROUTE_DRONES | MEDIUM |

### 6.4 Route Optimisation

```
RouteOptimizationService (A* variant)
  Nodes  : warehouse zones
  Edges  : distance + battery_cost_weight
  h(n)   : Euclidean distance to destination
  Output : ordered waypoints + total_distance_km + estimated_time_min
```

---

## 7. AI Microservice Integration

**Transport:** Spring Boot → HTTP → Python FastAPI (stateless)

| AI Endpoint | Request | Response |
|---|---|---|
| `POST /ai/sentiment` | `{ "text": "...", "product_id": 42 }` | `{ "score": 2.1, "label": "NEGATIVE" }` |
| `POST /ai/demand-forecast` | `{ "product_id": 42, "history": [12,15,11,...] }` | `{ "forecast": 18, "confidence": 0.87 }` |

**Client config (AiServiceClient.java):**
```
- Bean: @Component RestTemplate
- Timeout: connect=5s, read=10s
- Fallback: on error → rule-only decision, log WARN, no HTTP 500
- Base URL: application.properties → ai.service.base-url
```

---

## 8. AI Concept Mapping

| Unit | Concept | Implementation | Example |
|---|---|---|---|
| UNIT 1 | Intelligent Agent | Backend = Percept→Decide→Act; API call = perception; Service = decision; Response = action | POST /sales → spike detected → RESTOCK |
| UNIT 2 | Search & Optimisation | A* in RouteOptimizationService; heuristic = distance + battery cost | Depot→Zone A→Zone C (shortest path) |
| UNIT 3 | Strategy Comparison | PricingStrategyService compares Penetration / Competitive / Skimming by margin × demand score | Low margin + falling sales → REDUCE_PRICE |
| UNIT 4 | Rule-Based Expert System | DecisionEngine applies ordered IF-THEN rules (§6.3) | stock=5, threshold=20 → RESTOCK_URGENT |
| UNIT 5 | NLP / Sentiment | Backend POSTs review text to Python BERT service; score stored in reviews.sentiment_score | "Poor packaging" → score 1.8 → REVIEW_QUALITY |

---

## 9. System Flow (Step-by-Step)

```
1. POST /api/sales  →  Controller validates SaleRequestDTO
2. SalesService.recordSale()  →  persist to sales table
3. InventoryRepository.decrementStock()  →  update inventory
4. DecisionEngine.evaluate(productId)  →  apply IF-THEN rules
5. AiServiceClient.getSentiment()  →  call /ai/sentiment (if new reviews)
6. AiServiceClient.getForecast()  →  call /ai/demand-forecast
7. Merge rule output + AI output  →  build DecisionResponseDTO
8. Controller returns HTTP 200  →  structured JSON to client
```

---

## 10. Constraints & NFRs

| Attribute | Requirement |
|---|---|
| Scope | Backend only — no frontend |
| Response Format | Structured JSON always; no plain-text decisions |
| AI Failure Handling | Graceful degradation — rules-only fallback, never HTTP 500 |
| API Response Time | < 300 ms (excluding AI call; AI budget: 2 s) |
| Test Coverage | ≥ 70% Service layer — JUnit 5 + Mockito + Testcontainers |
| DB Migration | Flyway or Liquibase — all DDL version-controlled |
| Error Format | RFC 7807 Problem Detail — consistent envelope across all endpoints |

---

*Implementation-ready. No frontend scope. No ambiguity.*


## 9. System Flow (Step-by-Step)

```
1. POST /api/sales  →  Controller validates SaleRequestDTO
2. SalesService.recordSale()  →  persist to sales table
3. InventoryRepository.decrementStock()  →  update inventory
4. DecisionEngine.evaluate(productId)  →  apply IF-THEN rules
5. AiServiceClient.getSentiment()  →  call /ai/sentiment (if new reviews)
6. AiServiceClient.getForecast()  →  call /ai/demand-forecast
7. Merge rule output + AI output  →  build DecisionResponseDTO
8. Controller returns HTTP 200  →  structured JSON to client
```

---

## 10. Constraints & NFRs

| Attribute | Requirement |
|---|---|
| Scope | Backend only — no frontend |
| Response Format | Structured JSON always; no plain-text decisions |
| AI Failure Handling | Graceful degradation — rules-only fallback, never HTTP 500 |
| API Response Time | < 300 ms (excluding AI call; AI budget: 2 s) |
| Test Coverage | ≥ 70% Service layer — JUnit 5 + Mockito + Testcontainers |
| DB Migration | Flyway or Liquibase — all DDL version-controlled |
| Error Format | RFC 7807 Problem Detail — consistent envelope across all endpoints |

---

*Implementation-ready. No frontend scope. No ambiguity.*