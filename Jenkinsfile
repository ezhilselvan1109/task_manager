pipeline {
    agent any

    options {
        timeout(time: 25, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        MAVEN_OPTS = '-Xms256m -Xmx768m'
        DOCKER_BUILDKIT = '1'
        COMPOSE_DOCKER_CLI_BUILD = '1'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out Task Management System source code...'
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                }
                echo "Building commit: ${env.GIT_COMMIT_SHORT}"
            }
        }

        stage('Backend Build & Unit Tests') {
            steps {
                echo 'Compiling Java 21 microservices and executing JUnit 5 tests...'
                sh 'chmod +x ./mvnw && ./mvnw clean test -B'
            }
        }

        stage('Frontend Test & Production Build') {
            steps {
                echo 'Testing and building React 18 TypeScript SPA...'
                dir('frontend') {
                    sh 'npm ci --prefer-offline --no-audit'
                    sh 'npm test'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker Packaging') {
            steps {
                echo "Building production Docker images for commit ${env.GIT_COMMIT_SHORT}..."
                sh 'docker compose -f docker-compose.prod.yml build'
            }
        }

        stage('Deploy to Docker Production Stack') {
            steps {
                echo 'Deploying updated containers from host-aligned /opt/taskmanager directory...'
                sh '''
                    git config --global --add safe.directory /opt/taskmanager
                    cd /opt/taskmanager
                    git pull origin main
                    docker compose -f docker-compose.prod.yml up -d
                    docker exec taskmanager-nginx nginx -s reload || true
                '''
            }
        }

        stage('Health Verification') {
            steps {
                echo 'Verifying microservice mesh and reverse proxy health...'
                sh '''
                    sleep 10
                    curl -fsS http://taskmanager-nginx/healthz
                    cd /opt/taskmanager && docker compose -f docker-compose.prod.yml ps
                '''
            }
        }
    }

    post {
        always {
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        }
        success {
            echo "CI/CD Pipeline SUCCEEDED for commit ${env.GIT_COMMIT_SHORT}."
        }
        failure {
            echo "CI/CD Pipeline FAILED for commit ${env.GIT_COMMIT_SHORT}."
        }
    }
}

