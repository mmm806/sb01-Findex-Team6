-- 데이터베이스 생성
CREATE DATABASE findex;

-- 유저 생성
CREATE USER findex_user WITH PASSWORD 'findex1234';

GRANT ALL PRIVILEGES ON DATABASE findex TO findex_user;

-- 유저에게 데이터베이스에 대한 연결 권한 부여
GRANT CONNECT ON DATABASE findex TO findex_user;

-- 사용자 정의 타입 생성
CREATE TYPE source_type AS ENUM (
    'USER',
    'OPEN_API'
    );

CREATE TYPE job_type AS ENUM (
    'INDEX_INFO',
    'INDEX_DATA'
    );

-- 테이블 생성
CREATE TABLE index (
                       id BIGSERIAL PRIMARY KEY,
                       index_classification VARCHAR NOT NULL,
                       index_name VARCHAR UNIQUE NOT NULL,
                       employed_items_count INTEGER NOT NULL,
                       base_date TIMESTAMP NOT NULL,
                       base_index NUMERIC NOT NULL,
                       source_type source_type NOT NULL,
                       favorite BOOLEAN NOT NULL
);

CREATE TABLE index_val (
                           id BIGSERIAL PRIMARY KEY,
                           index_id BIGINT NOT NULL UNIQUE REFERENCES index(id),
                           date TIMESTAMP NOT NULL UNIQUE,
                           source_type source_type NOT NULL,
                           market_price NUMERIC NOT NULL,
                           close_price NUMERIC NOT NULL,
                           high_price NUMERIC NOT NULL,
                           low_price NUMERIC NOT NULL,
                           versus NUMERIC NOT NULL,
                           fluctuation_rate NUMERIC NOT NULL,
                           trading_quantity INTEGER NOT NULL,
                           trading_price NUMERIC NOT NULL,
                           market_total_count NUMERIC NOT NULL
);

CREATE TABLE index_data_link (
                                 id BIGSERIAL PRIMARY KEY,
                                 index_val_id BIGINT NOT NULL REFERENCES index_val(id),
                                 index_id BIGINT NOT NULL REFERENCES index(id),
                                 source_type job_type NOT NULL,
                                 target_date TIMESTAMP NOT NULL,
                                 worker VARCHAR NOT NULL,
                                 job_time TIMESTAMP NOT NULL,
                                 result BOOLEAN NOT NULL
);

CREATE TABLE dashboard (
                           id BIGSERIAL PRIMARY KEY,
                           index_id BIGINT NOT NULL REFERENCES index(id),
                           performance_data_id BIGINT NOT NULL REFERENCES index_val(id),
                           favorite BOOLEAN NOT NULL
);

CREATE TABLE auto_integration (
                                  id BIGSERIAL PRIMARY KEY,
                                  index_id BIGINT NOT NULL REFERENCES index(id),
                                  enabled BOOLEAN NOT NULL
);