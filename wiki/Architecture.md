# Product Service Architecture

The service follows **Hexagonal Architecture** (Ports and Adapters): the domain is at the center and the application communicates with the outside world only through ports; infrastructure adapters implement those ports.

## Layers and responsibilities

```mermaid
flowchart TB
    subgraph INFRA["🖥️ INFRASTRUCTURE"]
        subgraph INPUT["Input Adapters (REST / HTTP)"]
            IController["IProductController"]
            Controller["ProductController"]
            Mapper["ProductMapper"]
            Handler["GlobalExceptionHandler"]
        end
        subgraph OUTPUT["Output Adapters (Persistence / Redis)"]
            RepoAdapter["ProductRepositoryAdapter"]
            JpaRepository["ProductJpaRepository"]
            Entity["ProductEntity"]
            Converter["SpecificationsJsonConverter"]
        end
    end

    subgraph APP["⚙️ APPLICATION"]
        UseCase["CompareProductsUseCase"]
        PortOut["ProductRepository"]
        Service["ProductApplicationService"]
    end

    subgraph DOM["📦 DOMAIN"]
        Product["Product"]
        ProductType["ProductType"]
        Exceptions["ProductNotFoundException\nInvalidRequestException\nProductDomainException"]
    end

    HTTP["Client HTTP"] --> Controller
    Controller --> UseCase
    UseCase --> Service
    Service --> PortOut
    PortOut --> RepoAdapter
    RepoAdapter --> JpaRepository
    RepoAdapter --> Entity
    Service --> Product
    Product --> ProductType
    Service -.-> Exceptions
```

**Flow:** The client calls the REST adapter → input port (use case) → application service → output port (repository) → persistence adapter → JPA/H2. The domain (`Product`, `ProductType`, exceptions) is used by the application and has no knowledge of infrastructure.

## Request flow

1. **Input (HTTP):** The request hits the REST adapter (`ProductController`), which implements the `IProductController` contract. The controller parses `ids` and `fields` and delegates to the use case.
2. **Use case:** `CompareProductsUseCase` (input port) is implemented by `ProductApplicationService`, which orchestrates the logic: validate IDs, fetch products in the requested order, and apply field filtering.
3. **Output (persistence):** The service uses the `ProductRepository` port (output port). The `ProductRepositoryAdapter` implements this port using `ProductJpaRepository` (Spring Data JPA) and maps `ProductEntity` ↔ domain `Product`. Specifications are persisted as JSON via `SpecificationsJsonConverter`.
4. **Cache:** Product reads may be cached in Redis (configured in `RedisConfiguration` and `application.yaml`), reducing load on the database.
5. **Response:** The controller maps domain `Product` instances to DTOs (`ProductResponse`, `ProductListResponse`) according to the requested `fields` and returns JSON.

## Product comparison flow (sequence diagram)

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant Controller as ProductController
    participant Mapper as ProductMapper
    participant Service as ProductApplicationService
    participant Cache as CacheManager/Cache
    participant Repo as ProductRepository
    participant Adapter as ProductRepositoryAdapter
    participant JPA as ProductJpaRepository
    participant DB as Database (H2)

    Client->>+Controller: GET /products/compare?ids=1,2&fields=name,price
    Controller->>Mapper: parseIds(ids), parseFields(fields)
    Mapper-->>Controller: idList, fieldSet

    Controller->>+Service: getComparison(idList, fieldSet)

    alt ids null or empty
        Service-->>Controller: InvalidRequestException (400/422)
    end

    Service->>Service: uniqueIds = ids.distinct()
    Service->>Cache: getCache("product")

    alt Cache not configured (null)
        Service->>Repo: findByIdIn(uniqueIds)
        Repo->>Adapter: findByIdIn(ids)
        Adapter->>JPA: findByIdIn(ids)
        JPA->>DB: SELECT * FROM product WHERE id IN (...)
        DB-->>JPA: ProductEntity[]
        JPA-->>Adapter: ProductEntity[]
        Adapter->>Adapter: toDomain(entity)
        Adapter-->>Repo: List<Product>
        Repo-->>Service: List<Product>
        Service->>Service: loadAndOrder(uniqueIds, products)
        Note over Service: Validate missing IDs → ProductNotFoundException
    else Cache available
        loop For each id in uniqueIds
            Service->>Cache: get(id)
            alt Cache hit
                Cache-->>Service: Product
                Service->>Service: idToProduct.put(id, product)
            else Cache miss
                Cache-->>Service: null
                Service->>Service: missedIds.add(id)
            end
        end
        alt Has missedIds
            Service->>Repo: findByIdIn(missedIds)
            Repo->>Adapter: findByIdIn(missedIds)
            Adapter->>JPA: findByIdIn(ids)
            JPA->>DB: SELECT * FROM product WHERE id IN (...)
            DB-->>JPA: ProductEntity[]
            JPA-->>Adapter: ProductEntity[]
            Adapter-->>Repo: List<Product>
            Repo-->>Service: List<Product>
            Service->>Service: Validate missingIds → ProductNotFoundException if any missing
            loop For each loaded Product
                Service->>Service: idToProduct.put(id, product)
                Service->>Cache: put(id, product)
            end
        end
        Service->>Service: uniqueIds.stream().map(idToProduct::get).toList()
    end

    Service-->>-Controller: List<Product>

    loop For each Product
        Controller->>Mapper: fromProductToProductResponse(product, fieldSet)
        Mapper-->>Controller: ProductResponse (requested fields only)
    end
    Controller->>Controller: ProductListResponse.builder().products(...).build()
    Controller-->>-Client: 200 OK, ProductListResponse (JSON)
```

## Design decisions

| Decision | Rationale |
|----------|-----------|
| Domain at the center | Business rules and model independent of frameworks and databases. |
| Ports (interfaces) | The application does not depend on HTTP or JPA details; adapters are interchangeable. |
| API contract in interface (`IProductController`) | API First: OpenAPI documentation and contract in one place; the controller only implements. |
| Specifications as JSON | Flexibility per product type (smartphones, laptops, etc.) without schema changes. |
| Redis cache | Better latency and lower load on H2 for repeated comparison scenarios. |
| Flyway + H2 | Versioned initial data and runnable environment without an external DB (development/demos). |

## Project structure (packages)

```
src/main/java/com/mercadolibre/
├── domain/
│   ├── model/                    # Domain entities and value objects (Product, ProductType)
│   └── exception/                # Domain exceptions
├── application/
│   ├── port/
│   │   ├── input/                # Use cases (CompareProductsUseCase, ProductField)
│   │   └── output/               # Repositories (ProductRepository)
│   └── service/                  # Application services (ProductApplicationService)
├── infrastructure/
│   ├── adapter/
│   │   ├── input/rest/           # Controller, contract, DTOs, mappers, error handling
│   │   └── output/persistence/   # JPA entity, repository, adapter, converters
│   └── config/                   # JPA, Redis, etc. configuration
└── ProductServiceApplication.java
```
