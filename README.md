# SmartReviewAnalyzer

**AI-Powered Product Review Intelligence Platform**

A production-oriented Spring Boot application that uses **Spring AI**, **RAG (Retrieval-Augmented Generation)**, **Vector Database (pgvector)**, and modern resilience patterns to analyze customer product reviews in real time.

---

## Features

- **Single Review Analysis** – Sentiment, key topics, summary & actionable insights
- **RAG Pipeline** – Retrieves similar historical reviews before generating insights
- **Vector Search** – PostgreSQL + pgvector with HNSW index
- **Bulk Processing** – Asynchronous bulk analysis endpoint
- **Product Insights** – Aggregated sentiment & trending topics
- **Caching** – Redis for low-latency repeated queries
- **Fault Tolerance** – Resilience4j Circuit Breaker + Retry around LLM calls
- **Observability** – Spring Actuator + Prometheus metrics

---

## Tech Stack

| Layer              | Technology                          |
|--------------------|-------------------------------------|
| Language           | Java 21                             |
| Framework          | Spring Boot 3.3                     |
| AI Framework       | Spring AI                           |
| LLM / Embeddings   | OpenAI (gpt-4o-mini + text-embedding-3-small) |
| Vector Store       | PostgreSQL + pgvector               |
| Cache              | Redis                               |
| Resilience         | Resilience4j                        |
| Build              | Maven                               |
| Container          | Docker + Docker Compose             |

---

## Project Structure

```
smart-review-analyzer/
├── src/main/java/com/example/smartreview/
│   ├── config/          # AI, Redis configuration
│   ├── controller/      # REST controllers
│   ├── dto/             # Request/Response objects
│   ├── entity/          # JPA entities
│   ├── repository/      # Spring Data repositories
│   ├── service/         # Business logic + RAG orchestrator
│   └── exception/       # Global exception handling
├── src/main/resources/
│   └── application.yml
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- OpenAI API Key

### 1. Clone the repository
```bash
git clone https://github.com/your-username/smart-review-analyzer.git
cd smart-review-analyzer
```

### 2. Set your OpenAI API Key
```bash
export OPENAI_API_KEY=sk-your-key-here
```

### 3. Start the full stack
```bash
docker-compose up --build
```

The application will be available at:  
**http://localhost:8080**

---

## API Endpoints

| Method | Endpoint                              | Description                        |
|--------|---------------------------------------|------------------------------------|
| POST   | `/api/v1/reviews/analyze`             | Analyze a single review (sync)     |
| POST   | `/api/v1/reviews/analyze/bulk`        | Analyze multiple reviews (async)   |
| GET    | `/api/v1/reviews/jobs/{jobId}`        | Check bulk job status              |
| POST   | `/api/v1/reviews`                     | Ingest + embed a review            |
| GET    | `/api/v1/products/{productId}/insights` | Aggregated product insights      |

### Example – Analyze Single Review

```bash
curl -X POST http://localhost:8080/api/v1/reviews/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-12345",
    "reviewText": "The battery life is excellent but the camera quality is disappointing in low light.",
    "rating": 3,
    "userId": "user_987"
  }'
```

---

## Architecture Highlights

- **RAG Flow**: Embed → Similarity Search (pgvector) → Context Augmentation → LLM Generation
- **Circuit Breaker** around LLM calls for fault tolerance
- **Redis Cache** for product insights and repeated analyses
- **Stateless services** ready for horizontal scaling
- **HNSW index** in pgvector for fast approximate nearest neighbor search

---

## Configuration

Key properties in `application.yml` (can be overridden via environment variables):

| Property                        | Default                  | Description                     |
|---------------------------------|--------------------------|---------------------------------|
| `OPENAI_API_KEY`                | -                        | Required                        |
| `app.rag.top-k`                 | 6                        | Number of similar docs to retrieve |
| `app.rag.similarity-threshold`  | 0.72                     | Minimum similarity score        |
| `spring.ai.openai.chat.options.model` | gpt-4o-mini        | LLM model                       |

---

## Development

```bash
# Run locally (requires local Postgres with pgvector + Redis)
./mvnw spring-boot:run

# Run tests
./mvnw test
```

---

## Future Improvements

- Full Kafka-based bulk processing
- Hybrid search (BM25 + Vector)
- Re-ranking layer
- User authentication & rate limiting
- Advanced evaluation metrics (Faithfulness, Answer Relevance)
- Support for local LLMs via Ollama

---

## License

MIT

---

**Built with Spring Boot + Spring AI + RAG**
