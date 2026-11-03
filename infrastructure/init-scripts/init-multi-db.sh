#!/bin/bash
set -e

# Creates dedicated roles and databases for each microservice
function create_user_and_database() {
    local database=$1
    local user=$2
    local password=$3
    echo "Creating database '$database' and user '$user'..."
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        DO \$\$
        BEGIN
            IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$user') THEN
                CREATE USER $user WITH PASSWORD '$password';
            END IF;
        END
        \$\$;
        SELECT 'CREATE DATABASE $database OWNER $user'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec
        GRANT ALL PRIVILEGES ON DATABASE $database TO $user;
EOSQL
}

# Execute creation for all 3 microservices
create_user_and_database "user_db" "user_app" "${DB_USER_PASSWORD:-user_password_secret}"
create_user_and_database "task_db" "task_app" "${DB_TASK_PASSWORD:-task_password_secret}"
create_user_and_database "notification_db" "notification_app" "${DB_NOTIF_PASSWORD:-notification_password_secret}"

echo "Multi-database provisioning completed successfully."

