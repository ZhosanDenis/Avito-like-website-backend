-- liquibase formatted sql

-- changeset dzhosan:1
CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    email      TEXT         NOT NULL UNIQUE,
    password   BIGINT       NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    phone      TEXT         NOT NULL,
    image_path VARCHAR(255),
    role       TEXT         NOT NULL
);

CREATE TABLE ads
(
    id          SERIAL PRIMARY KEY,
    description TEXT    NOT NULL,
    price       INTEGER NOT NULL,
    title       TEXT    NOT NULL,
    image_path  VARCHAR(255),
    user_id     INTEGER CHECK (user_id > 0) REFERENCES users (id)
);

CREATE TABLE comments
(
    id         SERIAL PRIMARY KEY,
    text       TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL,
    user_id    INTEGER CHECK (user_id > 0) REFERENCES users (id),
    ad_id      INTEGER CHECK (ad_id > 0) REFERENCES ads (id)
);