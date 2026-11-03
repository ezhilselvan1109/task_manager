---
name: jenkins-cicd
description: >-
  Use this skill when configuring, editing, or validating the Jenkins declarative pipeline,
  build stages, automated quality gates, and artifact promotions.
---

# Jenkins CI/CD Skill

## Purpose
Directs the configuration and maintenance of the root `Jenkinsfile` declarative pipeline, automated testing, quality gates, and deployment verification.

## When to Use
- Modifying pipeline stages, build triggers, or environment parameters.
- Debugging failed CI runs or integrating test report publishing.
- Configuring SonarQube, Checkstyle, or container vulnerability scans (Trivy).

## Required Inputs
- Root `Jenkinsfile`.
- Maven build goals and test profiles.

## Step-by-Step Procedure
1. **Structure Declarative Pipeline**:
   Ensure root `Jenkinsfile` defines the 7 standard stages:
   ```groovy
   pipeline {
       agent any
       tools {
           maven 'Maven-3.9'
           jdk 'Java-21'
       }
       stages {
           stage('Checkout') {
               steps { checkout scm }
           }
           stage('Lint & Static Analysis') {
               steps { sh 'mvn checkstyle:check' }
           }
           stage('Backend Unit Tests') {
               steps { sh 'mvn clean test' }
           }
           stage('Integration Tests') {
               steps { sh 'mvn verify -DskipUnitTests' }
           }
           stage('Docker Build') {
               steps { sh 'docker compose build' }
           }
           stage('Security Scan') {
               steps { sh 'trivy image task-manager/task-service:latest' }
           }
           stage('Deploy & Healthcheck') {
               steps { sh 'docker compose up -d' }
           }
       }
       post {
           always {
               junit '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
           }
       }
   }
   ```
2. **Configure Pipeline Post Actions**:
   - Publish JUnit XML reports on every run.
   - Send notifications on failure (`post { failure { ... } }`).
3. **Validate Pipeline Syntax**:
   - Verify syntax locally or via Jenkins CLI linter.

## Applicable Project Standards
- [Rule 09: Docker & CI/CD Standards](file:///d:/learning/task_manager/.agents/rules/09-docker-and-cicd.md)
- [Declarative Jenkinsfile Specification](file:///d:/learning/task_manager/documentation/14-devops-deployment/04-jenkinsfile-pipeline-specification.md)

## Validation Checklist
- [ ] Pipeline executes all 7 stages sequentially.
- [ ] Any test or linting failure halts the pipeline immediately.
- [ ] Surefire and Failsafe test reports are archived in `post`.
- [ ] Secrets are injected via `credentials()` helper, never hardcoded.

## Common Mistakes
- Committing plain text tokens in `Jenkinsfile`.
- Skipping integration tests during the pull request validation stage.

## Completion Criteria
Pipeline builds the application, runs unit and integration tests, builds Docker images, and executes health checks cleanly.

