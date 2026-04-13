[🇺🇸 English](#english) | [🇧🇷 Português](#português)

---

<br/>

<h1 id="english">🇺🇸 English</h1>

# 📦 Order API - High-Performance Microservices Architecture

This project is the distributed **Order API** built with Java 21 and Spring Boot 3. It serves as a core transactional service utilizing Saga and Outbox patterns for managing distributed transactions across the `catalog-api-java` and `inventory-api-java` ecosystem.

---

## 🛠️ Tech Stack & Infrastructure

- **Language:** Java 21, Spring Boot 3.2.4
- **Database:** PostgreSQL (Relational Database)
- **Database Versioning:** Flyway
- **Cross-Service Communication:** Spring Cloud OpenFeign
- **Message Broker:** Apache Kafka
- **Resilience:** Resilience4j
- **Observability:** Micrometer, Prometheus & Grafana
- **Containers:** Docker & Docker Compose

To spin up the ecosystem locally:
```bash
docker-compose up -d
```

---

## 🧠 Architectural Demonstrations

### 1. Structured Concurrency (Java 21 Virtual Threads)
Replaces manual asynchronous thread pools with Java 21's Virtual Threads for high-throughput, low-latency I/O operations without blocking standard platform threads.

### 2. Saga & Outbox Patterns
Demonstrates distributed transactions using the Outbox pattern for reliable message delivery through Kafka, ensuring eventual consistency without distributed locks or 2PC.

### 3. Synchronous Fetching (Spring Cloud OpenFeign)
Dynamically polls remote clusters mapping directly into the Java layer to instantly pull synchronous data from external microservices (Catalog and Inventory) over HTTP.

### 4. Database Versioning & Migrations (Flyway)
The database schema evolves deterministically via strictly controlled SQL scripts before the web server boots up, dropping automatic unreliable ORM schemas.

### 5. Circuit Breakers & Bulkheads (Resilience4j)
Configured to prevent cascading timeouts with Circuit Breakers, Time Limiters, and Bulkheads protecting the application against slow dependencies (catalogDB, inventoryDB).

---

## 🚀 Running & Testing

1. **Launch Infrastructure:**
   ```bash
   docker-compose up -d
   ```
2. **Launch Application:** Run via IDE. Binds to `8092`.
3. **Swagger UI:** `http://localhost:8092/swagger-ui.html`

### Featured Flow to Test:
- **Order Creation Flow** → Demonstrates the combination of **OpenFeign** for validation, **Virtual Threads** for execution, and **Kafka / Outbox Pattern** for notifying the microservices architecture.

<br/><br/>

<hr/>

<br/>

<h1 id="português">🇧🇷 Português</h1>

# 📦 Order API - Microsserviços de Alta Performance

Este projeto é a **Order API** distribuída em Java 21 e Spring Boot 3. Ele serve como serviço transacional central demonstrando o uso dos padrões Saga e Outbox para lidar com transações distribuídas ao longo do ecossistema das APIs de catálogo (`catalog-api-java`) e estoque (`inventory-api-java`).

---

## 🛠️ Stack Tecnológico

- **Linguagem:** Java 21, Spring Boot 3.2.4
- **Banco de Dados:** PostgreSQL (Banco Relacional)
- **Versionamento de Banco:** Flyway
- **Comunicação Síncrona:** Spring Cloud OpenFeign
- **Mensageria:** Apache Kafka
- **Resiliência:** Resilience4j
- **Observabilidade:** Micrometer, Prometheus & Grafana
- **Containers:** Docker e Docker Compose

Para iniciar localmente:
```bash
docker-compose up -d
```

---

## 🧠 Demonstrações Arquiteturais

### 1. Concorrência Estruturada (Java 21 Virtual Threads)
Substitui pools de threads padrão do sistema por Virtual Threads nativas do Java 21, proporcionando alto throughput em operações de I/O de rede e banco de dados sem bloquear os núcleos físicos.

### 2. Padrões Saga e Outbox
Implementa consistência em transações distribuídas emparelhando inserções em banco e disparo de mensagens em Kafka. Garante a entrega segura dos eventos de negócio preservando o estado transacional do sistema de pedidos.

### 3. Chamadas Síncronas Diretas (Spring Cloud OpenFeign)
Realiza mapeamento de clientes HTTP declarativos para extrair rapidamente propriedades via rede dos microsserviços de Inventário e Catálogo de forma nativa.

### 4. Versionamento de Banco de Dados (Flyway)
Substitui o schema auto-update das ORMs por scripts SQL validados e versionados pelo gerenciador do Flyway no momento que a aplicação inicia.

### 5. Circuit Breakers e Bulkheads (Resilience4j)
Defende o sistema contra tráfego excedente e latência externa. As configurações de Bulkhead e Limitadores de Tempo isolam instabilidades oriundas de chamadas a serviços terceiros em degradação.

---

## 🚀 Executando

1. **Infraestrutura:**
   ```bash
   docker-compose up -d
   ```
2. **Aplicação:** Rode pela IDE na porta `8092`.
3. **Swagger UI:** `http://localhost:8092/swagger-ui.html`

### Fluxo de Destaque:
- **Criação de Pedidos** → Executa e orquestra transações via validação síncrona (**OpenFeign**), paraleliza tarefas com **Virtual Threads** e notifica os demais sistemas do barramento pelo **Padrão Outbox + Kafka**.