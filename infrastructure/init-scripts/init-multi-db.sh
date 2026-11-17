#!/bin/bash
set -euo pipefail

# Provision strict Database-per-Service schemas and least-privilege accounts in MySQL 8.4 LTS (ADR-009)
create_user_and_database() {
    local database=$1
    local user=$2
    local password=$3
    echo "Provisioning MySQL database '${database}' and user '${user}'..."
    mysql -u root -p"${MYSQL_ROOT_PASSWORD}" <<-EOSQL
        CREATE DATABASE IF NOT EXISTS \`${database}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
        CREATE USER IF NOT EXISTS '${user}'@'%' IDENTIFIED BY '${password}';
        ALTER USER '${user}'@'%' IDENTIFIED BY '${password}';
        GRANT ALL PRIVILEGES ON \`${database}\`.* TO '${user}'@'%';
        FLUSH PRIVILEGES;
EOSQL
}

create_user_and_database "user_db" "user_app" "${DB_USER_PASSWORD:-user_password_secret}"
create_user_and_database "task_db" "task_app" "${DB_TASK_PASSWORD:-task_password_secret}"
create_user_and_database "notification_db" "notification_app" "${DB_NOTIF_PASSWORD:-notification_password_secret}"

echo "MySQL 8.4 multi-database provisioning completed successfully."
