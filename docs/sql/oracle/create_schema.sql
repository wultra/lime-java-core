--
-- Create audit table
--

CREATE TABLE audit_log (
    application_name VARCHAR2(256 CHAR) NOT NULL,
    audit_level VARCHAR2(32 CHAR) NOT NULL,
    timestamp_created TIMESTAMP,
    message CLOB NOT NULL,
    exception_message CLOB,
    stack_trace CLOB,
    extras CLOB,
    calling_class VARCHAR2(256 CHAR) NOT NULL,
    thread_name VARCHAR2(256 CHAR) NOT NULL,
    version VARCHAR2(256 CHAR),
    build_time TIMESTAMP
);

CREATE INDEX audit_log_timestamp ON audit_log (timestamp_created);