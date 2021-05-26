/*
 * Copyright 2021 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wultra.core.audit.base.database;

import com.wultra.core.audit.base.AuditWriter;
import com.wultra.core.audit.base.configuration.AuditConfiguration;
import com.wultra.core.audit.base.model.AuditRecord;
import com.wultra.core.audit.base.util.ClassUtil;
import com.wultra.core.audit.base.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Database audit writer.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class DatabaseAuditWriter implements AuditWriter {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAuditWriter.class);

    private static final String SQL_INSERT_INTO = "INSERT INTO ";
    private static final String SQL_MANDATORY_COLUMNS = "application_name, audit_level, timestamp_created, message, exception_message, stack_trace, param, calling_class, thread_name, version, build_time";
    private static final String SQL_VALUES = " VALUES ";
    private static final String SQL_DELETE_FROM = "DELETE FROM ";
    private static final String SQL_CLEANUP_CONDITION = " WHERE timestamp_created < ?";

    private final BlockingQueue<AuditRecord> queue;
    private final JdbcTemplate jdbcTemplate;

    private final String tableName;
    private final int batchSize;
    private final int cleanupDays;
    private final String applicationName;
    private final String version;
    private final Instant buildTime;

    private List<String> paramColumnNames;
    private String queryParam;
    private String queryPlaceholders;

    private final JsonUtil jsonUtil = new JsonUtil();

    private final Object FLUSH_LOCK = new Object();
    private final Object CLEANUP_LOCK = new Object();

    @Autowired
    public DatabaseAuditWriter(AuditConfiguration configuration, JdbcTemplate jdbcTemplate) {
        this.queue = new LinkedBlockingDeque<>(configuration.getEventQueueSize());
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = configuration.getDbTableName();
        this.batchSize = configuration.getBatchSize();
        this.cleanupDays = configuration.getDbCleanupDays();
        this.applicationName = configuration.getApplicationName();
        this.version = configuration.getVersion();
        this.buildTime = configuration.getBuildTime();
    }

    private void analyzeDbTable() {
        if (jdbcTemplate.getDataSource() == null) {
            logger.error("Data source is not available");
            return;
        }
        try {
            paramColumnNames = new ArrayList<>();
            DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
            ResultSet rs = metaData.getColumns(null, null, tableName.toUpperCase(), null);
            StringBuilder paramBuilder = new StringBuilder();
            StringBuilder placeHolderBuilder = new StringBuilder();
            while (rs.next()) {
                String columnName = rs.getString("column_name").toLowerCase();
                if (placeHolderBuilder.length() == 0) {
                    placeHolderBuilder.append("?");
                } else {
                    placeHolderBuilder.append(", ?");
                }
                if (columnName.matches("param_.*")) {
                    paramColumnNames.add(columnName);
                    paramBuilder.append(", ");
                    paramBuilder.append(columnName);
                }
            }
            queryParam = paramBuilder.toString();
            queryPlaceholders = placeHolderBuilder.toString();
        } catch (SQLException ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    public void write(AuditRecord auditRecord) {
        auditRecord.setCallingClass(ClassUtil.getCallingClass(this.getClass().getPackage().getName()));
        auditRecord.setThreadName(Thread.currentThread().getName());
        try {
            queue.put(auditRecord);
        } catch (InterruptedException ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    public void flush() {
        if (jdbcTemplate.getDataSource() == null) {
            logger.error("Data source is not available");
            return;
        }

        synchronized (FLUSH_LOCK) {
            if (paramColumnNames == null) {
                analyzeDbTable();
            }
            while (!queue.isEmpty()) {
                try {
                    List<AuditRecord> recordsToPersist = new ArrayList<>(batchSize);
                    for (int i = 0; i < batchSize; i++) {
                        recordsToPersist.add(queue.take());
                        if (queue.isEmpty()) {
                            break;
                        }
                    }
                    int[] insertCounts = jdbcTemplate.batchUpdate(
                            SQL_INSERT_INTO
                                    + tableName
                                    + "("
                                    + SQL_MANDATORY_COLUMNS
                                    + queryParam
                                    + ")"
                                    + SQL_VALUES
                                    + "("
                                    + queryPlaceholders
                                    + ")",
                            new BatchPreparedStatementSetter() {
                                public void setValues(PreparedStatement ps, int i) throws SQLException {
                                    AuditRecord record = recordsToPersist.get(i);
                                    ps.setString(1, applicationName);
                                    ps.setString(2, record.getLevel().toString());
                                    ps.setTimestamp(3, new Timestamp(record.getTimestamp().getTime()));
                                    ps.setString(4, record.getMessage());
                                    Throwable throwable = record.getThrowable();
                                    if (throwable == null) {
                                        ps.setNull(5, Types.VARCHAR);
                                        ps.setNull(6, Types.VARCHAR);
                                    } else {
                                        StringWriter sw = new StringWriter();
                                        PrintWriter pw = new PrintWriter(sw);
                                        throwable.printStackTrace(pw);
                                        ps.setString(5, throwable.getMessage());
                                        ps.setString(6, sw.toString());
                                    }
                                    ps.setString(7, jsonUtil.serializeMap(record.getParam()));
                                    ps.setString(8, record.getCallingClass().getName());
                                    ps.setString(9, record.getThreadName());
                                    ps.setString(10, version);
                                    ps.setTimestamp(11, new Timestamp(buildTime.toEpochMilli()));
                                    for (int j = 0; j < paramColumnNames.size(); j++) {
                                        int index = 12 + j;
                                        String paramName = paramColumnNames.get(j).replace("param_", "");
                                        Object paramValue = record.getParam().get(paramName);
                                        if (paramValue == null) {
                                            ps.setNull(index, Types.VARCHAR);
                                        } else {
                                            if (paramValue instanceof CharSequence) {
                                                ps.setString(index, paramValue.toString());
                                            } else {
                                                ps.setString(index, jsonUtil.serializeObject(paramValue));
                                            }
                                        }
                                    }
                                }

                                public int getBatchSize() {
                                    return recordsToPersist.size();
                                }
                            });
                    logger.debug("Audit log batch insert succeeded, record count: {}", insertCounts.length);
                } catch (InterruptedException ex) {
                    logger.warn(ex.getMessage(), ex);
                }
            }
        }
    }

    public void cleanup() {
        if (jdbcTemplate.getDataSource() == null) {
            logger.error("Data source is not available");
            return;
        }
        LocalDateTime cleanupLimit = LocalDateTime.now().minusDays(cleanupDays);
        synchronized (CLEANUP_LOCK) {
            jdbcTemplate.execute( SQL_DELETE_FROM + tableName + SQL_CLEANUP_CONDITION, (PreparedStatementCallback<Boolean>) ps -> {
                ps.setTimestamp(1, Timestamp.valueOf(cleanupLimit));
                return ps.execute();
            });
            logger.debug("Audit records older than {} were deleted", cleanupLimit);
        }
    }

    @Scheduled(fixedDelayString = "${audit.flush.delay.fixed:1000}", initialDelayString = "${powerauth.audit.flush.delay.initial:1000}")
    public void scheduledFlush() {
        logger.debug("Scheduled audit log flush called");
        flush();
    }

    @Scheduled(fixedDelayString = "${audit.cleanup.delay.fixed:3600000}", initialDelayString = "${powerauth.audit.cleanup.delay.initial:1000}")
    public void scheduledCleanup() {
        logger.debug("Scheduled audit log cleanup called");
        cleanup();
    }

    @PreDestroy
    public void destroy() {
        flush();
    }

}
