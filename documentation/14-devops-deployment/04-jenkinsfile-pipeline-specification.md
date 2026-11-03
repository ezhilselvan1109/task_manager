# Declarative Jenkinsfile Pipeline Specification

**Document Identifier:** DOC-OPS-004  
**Traceability References:** NFR-007, DOC-OPS-001, DOC-OPS-002, DOC-OPS-003  
**Target File:** `Jenkinsfile` (Repository Root)  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document provides the complete, production-ready declarative `Jenkinsfile` specification for the Task Management System. The pipeline automates the entire CI/CD lifecycle from commit ingestion to containerized deployment and smoke verification.

---

## 2. Complete Declarative `Jenkinsfile`

```groovy
pipeline {
    agent any

    options {
        timeout(time: 15, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    environment {
        JAVA_HOME = '/usr/lib/jvm/temurin-21-jdk'
        MAVEN_OPTS = '-Xms256m -Xmx512m'
        DOCKER_BUILDKIT = '1'
        COMPOSE_DOCKER_CLI_BUILD = '1'
    }

    stages {

        // =====================================================================
        // STAGE 1: Source Checkout & Credential Binding
        // =====================================================================
        stage('Checkout') {
            steps {
                echo 'Checking out source code from GitHub...'
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                }
            }
        }

        // =====================================================================
        // STAGE 2: Backend Compilation & Unit Testing
        // =====================================================================
        stage('Backend Build & Unit Tests') {
            steps {
                echo 'Compiling Java 21 microservices and running JUnit 5 unit tests...'
                sh 'mvn clean test -B'
            }
        }

        // =====================================================================
        // STAGE 3: Static Analysis & Code Quality Gate
        // =====================================================================
        stage('Static Analysis') {
            steps {
                echo 'Executing Checkstyle and SpotBugs static analysis...'
                sh 'mvn checkstyle:check spotbugs:check -B'
            }
        }

        // =====================================================================
        // STAGE 4: Testcontainers Integration Testing
        // =====================================================================
        stage('Integration Tests') {
            steps {
                echo 'Running Testcontainers integration tests against real PostgreSQL & Kafka...'
                sh 'mvn verify -DskipUnitTests=true -B'
            }
        }

        // =====================================================================
        // STAGE 5: Frontend Build & Linting
        // =====================================================================
        stage('Frontend Build & Lint') {
            steps {
                echo 'Linting and building React 18 TypeScript SPA...'
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run lint'
                    sh 'npm run build'
                }
            }
        }

        // =====================================================================
        // STAGE 6: Multi-Stage Docker Builds (develop / main branches only)
        // =====================================================================
        stage('Docker Packaging') {
            when {
                branch pattern: "develop|main", comparator: "REGEXP"
            }
            steps {
                echo "Packaging multi-stage Docker images for commit ${env.GIT_COMMIT_SHORT}..."
                sh 'docker compose -f infrastructure/docker-compose.yml build'
            }
        }

        // =====================================================================
        // STAGE 7: Staging Deployment & Smoke Testing
        // =====================================================================
        stage('Staging Deployment & Health Probing') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying containerized microservices mesh to staging environment...'
                withCredentials([string(credentialsId: 'JWT_SECRET', variable: 'JWT_SECRET'),
                                 string(credentialsId: 'POSTGRES_ROOT_PASSWORD', variable: 'POSTGRES_ROOT_PASSWORD')]) {
                    sh 'docker compose -f infrastructure/docker-compose.yml up -d'
                }

                echo 'Waiting 30 seconds for Eureka registration and container health probes...'
                sleep 30

                echo 'Verifying Edge Gateway and core microservices health...'
                sh 'curl -f http://localhost:8080/actuator/health || exit 1'
                sh 'curl -f http://localhost:8761/actuator/health || exit 1'
            }
        }
    }

    // =========================================================================
    // POST-BUILD ACTIONS: Reporting & Artifact Archival
    // =========================================================================
    post {
        always {
            echo 'Archiving test results and code coverage metrics...'
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            jacoco(
                execPattern: '**/target/jacoco.exec',
                classPattern: '**/target/classes',
                sourcePattern: '**/src/main/java',
                exclusionPattern: '**/*Test*.class,**/config/**,**/dto/**',
                minimumLineCoverage: '80',
                minimumBranchCoverage: '75'
            )
            cleanWs notFailBuild: true
        }
        success {
            echo "CI/CD Pipeline Completed Successfully for commit ${env.GIT_COMMIT_SHORT}."
        }
        failure {
            echo "CI/CD Pipeline FAILED for commit ${env.GIT_COMMIT_SHORT}. Check logs for details."
        }
    }
}
```

