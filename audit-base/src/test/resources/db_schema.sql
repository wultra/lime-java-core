--
-- Create audit table
--

DROP TABLE IF EXISTS audit_log;

CREATE TABLE audit_log(
    application_name   VARCHAR(256) NOT NULL,
    audit_level              VARCHAR(32) NOT NULL,
    timestamp_created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message            TEXT NOT NULL,
    exception_message  TEXT,
    stack_trace        TEXT,
    param              TEXT,
    calling_class      VARCHAR(256) NOT NULL,
    thread_name        VARCHAR(256) NOT NULL,
    version            VARCHAR(256),
    build_time         TIMESTAMP,
    param_user_id      VARCHAR(256),
    param_operation_id VARCHAR(256)
);

CREATE INDEX audit_log_timestamp ON audit_log (timestamp_created);