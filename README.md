# 🏢 Smart Staff Attendance System

A **production-grade** full-stack attendance management platform built with **Spring Boot 3**, **React (Vite)**, and **MySQL** — featuring JWT authentication, database-level audit triggers, and token-bucket rate limiting.

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Spring Boot 3.3, Spring Security, Spring Data JPA |
| **Frontend** | React 18, Vite, Nginx |
| **Database** | MySQL 8 with audit triggers |
| **Auth** | JWT (JJWT 0.12) |
| **Rate Limiting** | Bucket4j |
| **Reports** | Apache POI (Excel export) |
| **Containerization** | Docker & Docker Compose |

---

## ✨ Key Features

- 🔐 **JWT-based Authentication** — stateless, secure login/logout
- 🗂️ **Database Auditing** — MySQL triggers log every INSERT/UPDATE/DELETE
- 🚦 **Rate Limiting** — Bucket4j token-bucket protects all API endpoints
- 📊 **Excel Reports** — Admin can export attendance data as `.xlsx`
- 🐳 **Docker-ready** — single `docker compose up` spins everything up
- 🔒 **Role-based Access Control** — Admin vs. Staff permission separation

---

## 🛠️ Local Development Setup

### Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 20+ & npm
- MySQL 8 (or Docker)

---

### Option 1 — Docker Compose (Recommended)

```bash
# 1. Clone the repo
git clone https://github.com/YOUR_GITHUB_USERNAME/smart-staff-attendance.git
cd smart-staff-attendance

# 2. Copy environment file and fill in your secrets
cp .env.example .env
# Edit .env with real values

# 3. Start all services (MySQL + Backend + Frontend)
docker compose up --build
```

App will be available at **http://localhost** (Nginx) and the backend API at **http://localhost:8080**.

---

### Option 2 — Manual (without Docker)

#### Backend (Spring Boot)

```bash
# From the project root
./mvnw spring-boot:run
# Or on Windows:
mvnw.cmd spring-boot:run
```

Backend starts on **http://localhost:8080**

#### Frontend (Vite)

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on **http://localhost:5173**

---

## 🔑 Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```env
MYSQL_ROOT_PASSWORD=your-strong-root-password
MYSQL_PASSWORD=your-strong-app-password
APP_JWT_SECRET=your-base64-encoded-256-bit-secret
```

> **Generate a JWT secret:**
> ```bash
> openssl rand -base64 32
> ```

---

## 📁 Project Structure

```
smart-staff-attendance/
├── src/                    # Spring Boot backend source
│   ├── main/
│   │   ├── java/           # Controllers, Services, Repositories, Security
│   │   └── resources/      # application.yml, DB migration scripts
│   └── test/               # Unit & integration tests
├── frontend/               # React + Vite frontend
│   ├── src/
│   └── Dockerfile          # Nginx production build
├── data/                   # MySQL Docker volume (gitignored)
├── docker-compose.yml      # Orchestrates all services
├── Dockerfile              # Backend Spring Boot image
├── pom.xml                 # Maven dependencies
└── .env.example            # Environment variable template
```

---

## 🔒 Security Highlights

| Feature | Implementation |
|---|---|
| Password storage | BCrypt hashing |
| Token signing | HS256 with 256-bit secret |
| API protection | Spring Security filter chain |
| Rate limiting | Bucket4j per-IP token bucket |
| Audit trail | MySQL `AFTER INSERT/UPDATE/DELETE` triggers |

---

## 📜 License

MIT — see [LICENSE](LICENSE) for details.
