#!/bin/bash
set -e

create_database() {
  local database="$1"
  echo "Creating database '${database}'"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-EOSQL
    SELECT 'CREATE DATABASE "${database}"'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${database}')\gexec
EOSQL
}

create_database "userservicedb"
create_database "rechargeprocessingdb"
create_database "paymentservicedb"
create_database "operatorPlan"

# Create SonarQube user and database
echo "Creating SonarQube database..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-EOSQL
  DO \$\$
  BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'sonar') THEN
      CREATE USER sonar WITH ENCRYPTED PASSWORD 'sonar';
    END IF;
  END
  \$\$;
  SELECT 'CREATE DATABASE "sonarqube" OWNER sonar'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sonarqube')\gexec
  ALTER DATABASE sonarqube OWNER TO sonar;
EOSQL

echo "Databases created successfully"

# Run initialization SQL in userservicedb
echo "Loading initial data into userservicedb..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname userservicedb << 'ENDSQL'
-- Create Tables
CREATE TABLE IF NOT EXISTS operator_entity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS plan_entity (
    id BIGSERIAL PRIMARY KEY,
    amount DOUBLE PRECISION,
    validity VARCHAR(255),
    description VARCHAR(500),
    operator_id BIGINT REFERENCES operator_entity(id)
);

CREATE TABLE IF NOT EXISTS userentity (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    contact_no VARCHAR(20) UNIQUE,
    password VARCHAR(500),
    role VARCHAR(50) DEFAULT 'USER'
);

-- Insert Operators
INSERT INTO operator_entity (id, name) VALUES 
(1, 'Jio'),
(2, 'Airtel'),
(3, 'Vodafone'),
(4, 'BSNL')
ON CONFLICT (id) DO NOTHING;

-- Insert Plans
INSERT INTO plan_entity (id, amount, validity, description, operator_id) VALUES 
(1, 149, '28 days', 'Jio Basic Plan - 2GB/day', 1),
(2, 299, '56 days', 'Jio Premium Plan - 3GB/day', 1),
(3, 99, '28 days', 'Airtel Basic Plan - 1.5GB/day', 2),
(4, 199, '28 days', 'Vodafone Plan - 2GB/day', 3),
(5, 79, '28 days', 'BSNL Plan - 1GB/day', 4)
ON CONFLICT (id) DO NOTHING;

-- Insert Users
-- Password hashes (bcrypt):
--   password123: $2a$10$slYQmyNdGzin7olVN3p5Be7DQH4mj8W/qUSTyZKx1eZQzVH37vU/i
--   admin123:    $2a$10$DZlHpJB5VHJQZ6oGYQ2Hj.9YJPJLpEqXI/T0ZFnP02E8bZgOaDqOK
INSERT INTO userentity (user_id, name, email, contact_no, password, role) VALUES 
(1, 'Raj Patel', 'raj@example.com', '9876543210', '$2a$10$slYQmyNdGzin7olVN3p5Be7DQH4mj8W/qUSTyZKx1eZQzVH37vU/i', 'USER'),
(2, 'Priya Sharma', 'priya@example.com', '9876543211', '$2a$10$slYQmyNdGzin7olVN3p5Be7DQH4mj8W/qUSTyZKx1eZQzVH37vU/i', 'USER'),
(3, 'Admin User', 'admin@example.com', '9876543212', '$2a$10$DZlHpJB5VHJQZ6oGYQ2Hj.9YJPJLpEqXI/T0ZFnP02E8bZgOaDqOK', 'ADMIN')
ON CONFLICT (user_id) DO NOTHING;

-- Reset sequences so next insert uses ID=4
SELECT setval('operator_entity_id_seq', (SELECT MAX(id) FROM operator_entity));
SELECT setval('plan_entity_id_seq', (SELECT MAX(id) FROM plan_entity));
SELECT setval('userentity_user_id_seq', (SELECT MAX(user_id) FROM userentity));

ENDSQL

# Run initialization SQL in operatorPlan database (for OperatorPlanManagement service)
echo "Loading initial data into operatorPlan..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname operatorPlan << 'ENDSQL'
-- Create Tables
CREATE TABLE IF NOT EXISTS operator_entity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS plan_entity (
    id BIGSERIAL PRIMARY KEY,
    amount DOUBLE PRECISION,
    validity VARCHAR(255),
    description VARCHAR(500),
    operator_id BIGINT REFERENCES operator_entity(id)
);

-- Insert Operators
INSERT INTO operator_entity (id, name) VALUES 
(1, 'Jio'),
(2, 'Airtel'),
(3, 'Vodafone'),
(4, 'BSNL')
ON CONFLICT (id) DO NOTHING;

-- Insert Plans
INSERT INTO plan_entity (id, amount, validity, description, operator_id) VALUES 
(1, 149, '28 days', 'Jio Basic Plan - 2GB/day', 1),
(2, 299, '56 days', 'Jio Premium Plan - 3GB/day', 1),
(3, 99, '28 days', 'Airtel Basic Plan - 1.5GB/day', 2),
(4, 199, '28 days', 'Vodafone Plan - 2GB/day', 3),
(5, 79, '28 days', 'BSNL Plan - 1GB/day', 4)
ON CONFLICT (id) DO NOTHING;

-- Reset sequences so next insert uses ID >= 5
SELECT setval('operator_entity_id_seq', (SELECT MAX(id) FROM operator_entity));
SELECT setval('plan_entity_id_seq', (SELECT MAX(id) FROM plan_entity));

ENDSQL

echo "Initial data loaded successfully"
