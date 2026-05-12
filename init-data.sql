-- ================== USER SERVICE DB ==================
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

-- Insert Users (Password: Hashed with bcrypt - "password123")
INSERT INTO userentity (user_id, name, email, contact_no, password, role) VALUES 
(1, 'Raj Patel', 'raj@example.com', '9876543210', '$2a$10$slYQmyNdGzin7olVN3p5Be7DQH4mj8W/qUSTyZKx1eZQzVH37vU/i', 'USER'),
(2, 'Priya Sharma', 'priya@example.com', '9876543211', '$2a$10$slYQmyNdGzin7olVN3p5Be7DQH4mj8W/qUSTyZKx1eZQzVH37vU/i', 'USER'),
(3, 'Admin User', 'admin@example.com', '9876543212', '$2a$10$slYQmyNdGzin7olVN3p5Be7DQH4mj8W/qUSTyZKx1eZQzVH37vU/i', 'ADMIN')
ON CONFLICT (user_id) DO NOTHING;
