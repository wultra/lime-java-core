--
-- Create audit table
--

CREATE TABLE audit_log (
    audit_log_id       VARCHAR2(36 CHAR) PRIMARY KEY,
    application_name   VARCHAR2(256 CHAR) NOT NULL,
    audit_level        VARCHAR2(32 CHAR) NOT NULL,
    timestamp_created  TIMESTAMP,
    message            CLOB NOT NULL,
    exception_message  CLOB,
    stack_trace        CLOB,
    param              TEXT,
    calling_class      VARCHAR2(256 CHAR) NOT NULL,
    thread_name        VARCHAR2(256 CHAR) NOT NULL,
    version            VARCHAR2(256 CHAR),
    build_time         TIMESTAMP
);

CREATE TABLE audit_param (
    audit_log_id       VARCHAR2(36 CHAR),
    timestamp_created  TIMESTAMP,
    param_key          VARCHAR2(256 CHAR),
    param_value        VARCHAR2(3072 CHAR)
);

CREATE INDEX audit_log_timestamp ON audit_log (timestamp_created);
CREATE INDEX audit_param_log ON audit_param (audit_log_id);
CREATE INDEX audit_param_timestamp ON audit_param (timestamp_created);
CREATE INDEX audit_param_key ON audit_param (param_key);
CREATE INDEX audit_param_value ON audit_param (param_value);