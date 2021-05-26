--
-- Create audit table
--

CREATE TABLE audit_log (
    application_name VARCHAR(256) NOT NULL,
    audit_level VARCHAR(32) NOT NULL,
    timestamp_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message TEXT NOT NULL,
    exception_message TEXT,
    stack_trace TEXT,
    extras TEXT,
    calling_class VARCHAR(256) NOT NULL,
    thread_name VARCHAR(256) NOT NULL,
    version VARCHAR(256),
    build_time TIMESTAMP
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE INDEX audit_log_timestamp ON audit_log (timestamp_created);