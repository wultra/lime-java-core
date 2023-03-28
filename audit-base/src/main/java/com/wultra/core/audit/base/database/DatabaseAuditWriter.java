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
import com.wultra.core.audit.base.model.AuditParam;
import com.wultra.core.audit.base.model.AuditRecord;
import com.wultra.core.audit.base.util.ClassUtil;
import com.wultra.core.audit.base.util.JsonUtil;
import com.wultra.core.audit.base.util.StringUtil;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private static final String SPRING_FRAMEWORK_PACKAGE_PREFIX = "org.springframework";

    private final BlockingQueue<AuditRecord> queue;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    private final String tableNameAudit;
    private final String tableNameParam;
    private final int batchSize;
    private final int cleanupDays;
    private final String applicationName;
    private final String version;
    private final Instant buildTime;

    private final boolean paramLoggingEnabled;

    private String insertAuditLog;
    private String insertAuditParam;

    private String dbSchema;

    private final JsonUtil jsonUtil = new JsonUtil();

    private final Object FLUSH_LOCK = new Object();
    private final Object CLEANUP_LOCK = new Object();

    /**
     * Service constructor.
     *
     * @param configuration Audit configuration.
     * @param jdbcTemplate  Spring JDBC template.
     * @param transactionTemplate Transaction template.
     */
    @Autowired
    public DatabaseAuditWriter(
            final AuditConfiguration configuration,
            final JdbcTemplate jdbcTemplate,
            final TransactionTemplate transactionTemplate) {

        this.queue = new LinkedBlockingDeque<>(configuration.getEventQueueSize());
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.dbSchema = configuration.getDbDefaultSchema();
        this.tableNameAudit = addDbSchema(dbSchema, configuration.getDbTableNameAudit());
        this.tableNameParam = addDbSchema(dbSchema, configuration.getDbTableNameParam());
        this.batchSize = configuration.getBatchSize();
        this.paramLoggingEnabled = configuration.isDbTableParamLoggingEnabled();
        this.cleanupDays = configuration.getDbCleanupDays();
        this.applicationName = StringUtil.trim(configuration.getApplicationName(), 256);
        this.version = StringUtil.trim(configuration.getVersion(), 256);
        this.buildTime = configuration.getBuildTime();
        prepareSqlInsertQueries();
    }

    private String addDbSchema(String dbSchema, String tableName) {
        if (StringUtils.hasLength(this.dbSchema) && !tableName.contains(".")) {
            return dbSchema + "." + tableName;
        }
        return tableName;
    }

    private void prepareSqlInsertQueries() {
        insertAuditLog = "INSERT INTO " +
                tableNameAudit +
                "(audit_log_id, application_name, audit_level, audit_type, timestamp_created, message, exception_message, stack_trace, param, calling_class, thread_name, version, build_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        insertAuditParam = "INSERT INTO " +
                tableNameParam +
                "(audit_log_id, timestamp_created, param_key, param_value) " +
                "VALUES (?, ?, ?, ?)";
    }

    @Override
    public void write(AuditRecord auditRecord) {
        List<String> packageFilter = new ArrayList<>();
        packageFilter.add(this.getClass().getPackage().getName());
        packageFilter.add(SPRING_FRAMEWORK_PACKAGE_PREFIX);
        auditRecord.setCallingClass(ClassUtil.getCallingClass(packageFilter));
        auditRecord.setThreadName(Thread.currentThread().getName());
        try {
            if (queue.remainingCapacity() == 0) {
                flush();
            }
            queue.put(auditRecord);
        } catch (InterruptedException ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    @Override
    public void flush() {
        if (transactionTemplate == null) {
            logger.error("Transaction template is not available");
            return;
        }
        if (jdbcTemplate.getDataSource() == null) {
            logger.error("Data source is not available");
            return;
        }

        synchronized (FLUSH_LOCK) {
            transactionTemplate.executeWithoutResult(status -> {
                while (!queue.isEmpty()) {
                    try {
                        final List<AuditRecord> auditsToPersist = new ArrayList<>(batchSize);
                        final List<AuditParam> paramsToPersist = new ArrayList<>();
                        for (int i = 0; i < batchSize; i++) {
                            AuditRecord record = queue.take();
                            auditsToPersist.add(record);
                            for (Map.Entry<String, Object> entry : record.getParam().entrySet()) {
                                paramsToPersist.add(new AuditParam(record.getId(), record.getTimestamp(), entry.getKey(), entry.getValue()));
                            }
                            if (queue.isEmpty()) {
                                break;
                            }
                        }
                        final int[] insertCountsLog = jdbcTemplate.batchUpdate(insertAuditLog,
                                new BatchPreparedStatementSetter() {
                                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                                        AuditRecord record = auditsToPersist.get(i);
                                        ps.setString(1, record.getId());
                                        ps.setString(2, applicationName);
                                        ps.setString(3, record.getLevel().toString());
                                        String auditType = record.getType();
                                        if (auditType == null) {
                                            ps.setNull(4, Types.VARCHAR);
                                        } else {
                                            ps.setString(4, StringUtil.trim(record.getType(), 256));
                                        }
                                        ps.setTimestamp(5, new Timestamp(record.getTimestamp().getTime()));
                                        ps.setString(6, record.getMessage());
                                        Throwable throwable = record.getThrowable();
                                        if (throwable == null) {
                                            ps.setNull(7, Types.VARCHAR);
                                            ps.setNull(8, Types.VARCHAR);
                                        } else {
                                            StringWriter sw = new StringWriter();
                                            PrintWriter pw = new PrintWriter(sw);
                                            throwable.printStackTrace(pw);
                                            ps.setString(7, throwable.getMessage());
                                            ps.setString(8, sw.toString());
                                        }
                                        ps.setString(9, jsonUtil.serializeMap(record.getParam()));
                                        ps.setString(10, StringUtil.trim(record.getCallingClass().getName(), 256));
                                        ps.setString(11, StringUtil.trim(record.getThreadName(), 256));
                                        ps.setString(12, version);
                                        ps.setTimestamp(13, new Timestamp(buildTime.toEpochMilli()));
                                    }

                                    public int getBatchSize() {
                                        return auditsToPersist.size();
                                    }
                                });
                        if (!paramLoggingEnabled) {
                            logger.debug("Audit log batch insert succeeded, audit record count: {}, audit param is disabled", insertCountsLog.length);
                            continue;
                        }
                        final int[] insertCountsParam = jdbcTemplate.batchUpdate(insertAuditParam,
                                new BatchPreparedStatementSetter() {
                                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                                        AuditParam record = paramsToPersist.get(i);
                                        ps.setString(1, record.getAuditLogId());
                                        ps.setTimestamp(2, new Timestamp(record.getTimestamp().getTime()));
                                        ps.setString(3, StringUtil.trim(record.getKey(), 256));
                                        Object value = record.getValue();
                                        if (value == null) {
                                            ps.setNull(4, Types.VARCHAR);
                                        } else if (value instanceof CharSequence) {
                                            ps.setString(4, StringUtil.trim(value.toString(), 4000));
                                        } else {
                                            ps.setString(4, StringUtil.trim(jsonUtil.serializeObject(value), 4000));
                                        }
                                    }

                                    public int getBatchSize() {
                                        return paramsToPersist.size();
                                    }
                                });
                        logger.debug("Audit log batch insert succeeded, audit record count: {}, audit param count: {}", insertCountsLog.length, insertCountsParam.length);
                    } catch (InterruptedException ex) {
                        logger.warn(ex.getMessage(), ex);
                    }
                }
            });
        }
    }

    @Override
    public void cleanup() {
        if (transactionTemplate == null) {
            logger.error("Transaction template is not available");
            return;
        }
        if (jdbcTemplate.getDataSource() == null) {
            logger.error("Data source is not available");
            return;
        }
        final LocalDateTime cleanupLimit = LocalDateTime.now().minusDays(cleanupDays);
        synchronized (CLEANUP_LOCK) {
            transactionTemplate.executeWithoutResult(status -> {
                jdbcTemplate.execute("DELETE FROM " + tableNameAudit + " WHERE timestamp_created < ?", (PreparedStatementCallback<Boolean>) ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(cleanupLimit));
                    return ps.execute();
                });
                jdbcTemplate.execute("DELETE FROM " + tableNameParam + " WHERE timestamp_created < ?", (PreparedStatementCallback<Boolean>) ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(cleanupLimit));
                    return ps.execute();
                });
                logger.debug("Audit records older than {} were deleted", cleanupLimit);
            });
        }
    }

    /**
     * Scheduled flush of persistence of audit data.
     */
    @Scheduled(fixedDelayString = "${audit.flush.delay.fixed:1000}", initialDelayString = "${powerauth.audit.flush.delay.initial:1000}")
    public void scheduledFlush() {
        logger.debug("Scheduled audit log flush called");
        flush();
    }

    /**
     * Scheduled cleanup of audit data in database.
     */
    @Scheduled(fixedDelayString = "${audit.cleanup.delay.fixed:3600000}", initialDelayString = "${powerauth.audit.cleanup.delay.initial:1000}")
    public void scheduledCleanup() {
        logger.debug("Scheduled audit log cleanup called");
        cleanup();
    }

    /**
     * Flush audit data before application exit.
     */
    @PreDestroy
    public void destroy() {
        flush();
    }

}
