# AI Interview Prep

An adaptive, AI-powered mock interview platform that tailors questions to your resume and the job you're applying for. Submit your answers and receive real-time streaming feedback, scores, and improvement tips — powered by OpenAI GPT.

---

## Features

- **Resume & JD analysis** — Upload a PDF/DOCX resume and paste a job description; GPT extracts relevant skills automatically.
- **Adaptive questioning** — Difficulty adjusts per question based on your previous score (low score → easier, high score → harder).
- **Real-time streaming evaluation** — Feedback streams token-by-token via SSE so you see the AI thinking as it evaluates.
- **Session history** — Every session, question, and evaluation is persisted; review past performance at any time.
- **Session summary** — Score breakdowns by skill and difficulty, strong/weak area identification.
- **JWT authentication** — Signup/login with secure stateless auth.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 24, Spring Boot 3.5, Spring Security, Spring WebFlux |
| AI | OpenAI GPT-4o-mini (chat + streaming) |
| Database | PostgreSQL |
| File parsing | Apache PDFBox, Apache POI |
| Auth | JWT (jjwt) |
| Frontend | Angular 17, TypeScript, RxJS |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Angular Frontend                    │
│  Login / Signup → Dashboard → Interview → History       │
└────────────────────────┬────────────────────────────────┘
                         │ REST + SSE
┌────────────────────────▼────────────────────────────────┐
│              Spring Boot Backend  (:8080)                │
│                                                         │
│  AdaptiveInterviewController                            │
│       │                                                 │
│  InterviewAgent                                         │
│  ├── SkillExtractorGPTImpl   (extract skills from docs) │
│  ├── PlannerAgentSimpleImpl  (build question plan)      │
│  ├── QuestionAgentGPTImpl    (generate questions)       │
│  └── EvaluatorAgentGPTImpl   (score + feedback via SSE) │
│                                                         │
│  GPTClient  →  OpenAI API (gpt-4o-mini)                 │
└────────────────────────┬────────────────────────────────┘
                         │
              ┌──────────▼──────────┐
              │     PostgreSQL       │
              │  interview_sessions  │
              │  interview_questions │
              │  interview_evals     │
              │  users               │
              └─────────────────────┘
```

---

## Getting Started

### Prerequisites

- Java 24+
- Gradle 8+
- PostgreSQL 14+
- Node.js 18+ / npm
- Angular CLI 17+ (`npm install -g @angular/cli`)
- An OpenAI API key

---

### 1. Database

```sql
CREATE DATABASE ai_prep;
```

The schema is managed by Hibernate (`ddl-auto=update`) — no migration scripts needed.

---

### 2. Backend

#### Environment variables

Create a `.env` file in the project root (picked up automatically via `spring-dotenv`):

```env
OPENAI_API_KEY=sk-...
```

#### Configuration (`src/main/resources/application.properties`)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ai_prep
spring.datasource.username=postgres
spring.datasource.password=postgres

openai.chat-model=gpt-4o-mini
openai.base-url=https://api.openai.com/v1

jwt.secret=<your-256-bit-hex-secret>
jwt.expiration=3600000
```

#### Run

```bash
./gradlew bootRun
```

The API is available at `http://localhost:8080`.

---

### 3. Frontend

```bash
cd ../ai-interview-frontend
npm install
ng serve
```

The app is available at `http://localhost:4200`.

The Angular dev server proxies API calls to `http://localhost:8080` — configure in `proxy.conf.json` if the backend port differs.

---

## API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/signup` | Register a new user |
| `POST` | `/auth/login` | Login, returns JWT |
| `POST` | `/adaptive/start` | Start a session (resume text + JD text) |
| `POST` | `/adaptive/stream/answer` | Submit answer — streams SSE tokens then `done` event |
| `GET` | `/adaptive/context/{sessionId}` | Get current session state |
| `GET` | `/adaptive/sessions` | List all sessions for the authenticated user |
| `GET` | `/adaptive/sessions/{sessionId}/summary` | Full summary with skill/difficulty breakdown |

### SSE event format (`/adaptive/stream/answer`)

```
event: token
data: partial feedback text...

event: done
data: {"finished":false,"evaluation":{"score":7,"feedback":"...","improvement":"..."},"nextQuestion":{"id":"...","skill":"Java","difficulty":"medium","text":"Explain the difference between..."}}
```

---

## Project Structure

```
src/main/java/com/ashwin/aiinterviewprep/
├── agents/
│   ├── InterviewAgent.java          # Orchestrates the full interview flow
│   ├── ISkillExtractor.java
│   ├── IPlannerAgent.java
│   ├── IQuestionAgent.java
│   └── IEvaluatorAgent.java
├── impl/
│   ├── SkillExtractorGPTImpl.java
│   ├── PlannerAgentSimpleImpl.java
│   ├── QuestionAgentGPTImpl.java
│   └── EvaluatorAgentGPTImpl.java
├── controller/
│   ├── AdaptiveInterviewController.java
│   ├── AuthController.java
│   └── UserController.java
├── service/
│   ├── GPTClient.java               # OpenAI chat + streaming client
│   └── FileParserService.java       # PDF / DOCX text extraction
├── model/
│   ├── InterviewSession.java
│   ├── InterviewQuestionRecord.java
│   └── InterviewEvaluationRecord.java
├── dto/
├── repository/
├── security/
└── config/
```

---

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `OPENAI_API_KEY` | Yes | Your OpenAI secret key |

---

## Known Limitations

- In-flight SSE streams are dropped on server restart (session state in PostgreSQL is preserved; the user can reload and continue).
- Very large PDF/DOCX files may slow down session start due to server-side text extraction.
- The adaptive difficulty algorithm is intentionally simple (`score ≥ 8 → hard`, `≥ 5 → medium`, else `easy`) and can be tuned in `InterviewAgent.adaptDifficulty()`.