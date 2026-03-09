--liquibase formatted sql
--changeset master:202603062000_init

CREATE TABLE car
(
    id UUID PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    model VARCHAR(100) NOT NULL
);

CREATE TABLE renter
(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE reservation
(
    id UUID PRIMARY KEY,
    car_id UUID NOT NULL REFERENCES car (id),
    renter_id UUID NOT NULL REFERENCES renter (id),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_end_after_start CHECK (end_date > start_date),
    CONSTRAINT chk_future_start CHECK (start_date > created_at)
);