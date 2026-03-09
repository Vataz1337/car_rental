--liquibase formatted sql
--changeset master:202603072000_seed

INSERT INTO car (id, type, model) VALUES
    (gen_random_uuid(), 'SEDAN', 'Toyota Camry'),
    (gen_random_uuid(), 'SEDAN', 'Honda Accord'),
    (gen_random_uuid(), 'SEDAN', 'Volkswagen Passat'),
    (gen_random_uuid(), 'SUV', 'Ford Explorer'),
    (gen_random_uuid(), 'SUV', 'Toyota RAV4'),
    (gen_random_uuid(), 'VAN', 'Ford Transit'),
    (gen_random_uuid(), 'VAN', 'Mercedes Sprinter');