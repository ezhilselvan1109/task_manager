# Development Setup Guide

**Document Identifier:** DOC-INF-002  
**Traceability References:** NFR-007, DOC-INF-001  
**Target Audience:** Software Engineers, QA Engineers, DevOps Engineers  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document provides a comprehensive step-by-step guide for configuring a local workstation to develop, test, and run the Task Management Microservices platform.

---

## 2. Software Prerequisites

Verify each prerequisite from a terminal before proceeding:

| Tool | Minimum Version | Verification Command | Expected Output Example |
|---|---|---|---|
| **Java Development Kit** | JDK 21 LTS | `java -version` | `openjdk version "21.0.2" 2024-01-16 LTS` |
| **Apache Maven** | 3.9.x | `mvn -version` | `Apache Maven 3.9.6 ...` |
| **Node.js** | 20 LTS (Active) | `node -v` | `v20.12.0` |
| **npm** | 10.x | `npm -v` | `10.5.0` |
| **Docker Desktop** | 4.28+ | `docker --version` | `Docker version 25.0.3, build 4debf41` |
| **Docker Compose** | Compose v2 | `docker compose version`| `Docker Compose version v2.24.6` |
| **Git** | 2.40+ | `git --version` | `git version 2.43.0.windows.1` |

> [!IMPORTANT]
> Verify that Docker Desktop has allocated at least **6 GB of RAM** and **4 CPU cores** in Docker Settings $\rightarrow$ Resources.

---

## 3. Workstation Initialization Workflow

### Step 1: Clone Repository
```powershell
cd D:\learning
git clone https://github.com/ezhilselvan1109/task_manager.git
cd task_manager
```

### Step 2: Environment File Initialization
Copy the template configuration into a local git-ignored `.env` file:
```powershell
Copy-Item infrastructure/.env.example infrastructure/.env
```
Inspect `infrastructure/.env` and verify default local secrets (`JWT_SECRET`, database passwords).

### Step 3: IDE Setup (IntelliJ IDEA Recommended)
1. Open IntelliJ IDEA $\rightarrow$ **File** $\rightarrow$ **Open** $\rightarrow$ Select `D:\learning\task_manager`.
2. Configure Project SDK:
   - Go to **File** $\rightarrow$ **Project Structure** $\rightarrow$ **Project**.
   - Set **SDK** to **Java 21 (Eclipse Temurin 21 or Corretto 21)**.
   - Set **Language Level** to **21 - Pattern matching, record patterns**.
3. Enable Annotation Processing:
   - Go to **Settings** $\rightarrow$ **Build, Execution, Deployment** $\rightarrow$ **Compiler** $\rightarrow$ **Annotation Processors**.
   - Check **Enable annotation processing** (mandatory for Lombok and MapStruct).
4. Install Required Plugins:
   - *Lombok*
   - *Spring Boot Helper / Tools*
   - *Tailwind CSS*
   - *Prettier*

### Step 4: Verify Root Maven Build
```powershell
mvn clean install -DskipTests
```
Ensures parent POM and all child module skeletons resolve successfully.

