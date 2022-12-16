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

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.AuditWriter;
import com.wultra.core.audit.base.configuration.AuditConfiguration;
import com.wultra.core.audit.base.model.AuditDetail;
import com.wultra.core.audit.base.model.AuditLevel;
import com.wultra.core.audit.base.model.AuditRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * Database audit implementation.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
@Slf4j
public class DatabaseAudit implements Audit {

    private final AuditWriter writer;
    private final AuditConfiguration configuration;

    /**
     * Service constructor.
     * @param writer Database audit writer.
     * @param configuration Audit configuration.
     */
    @Autowired
    public DatabaseAudit(DatabaseAuditWriter writer, AuditConfiguration configuration) {
        this.writer = writer;
        this.configuration = configuration;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean isErrorEnabled() {
        return configuration.getMinimumLevel().intValue() <= AuditLevel.ERROR.intValue();
    }

    @Override
    public void error(String message) {
        errorInternal(message, new AuditDetail(), null);
    }

    @Override
    public void error(String message, Object... args) {
        errorInternal(message, new AuditDetail(), args);
    }

    @Override
    public void error(String message, AuditDetail detail) {
        errorInternal(message, detail, null);
    }

    @Override
    public void error(String message, AuditDetail detail, Object... args) {
        errorInternal(message, detail, args);
    }

    private void errorInternal(String message, AuditDetail detail, Object[] args) {
        if (!isErrorEnabled()) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, AuditLevel.ERROR, detail.getType(), detail.getParam(), args);
        write(auditRecord);
    }

    @Override
    public boolean isWarnEnabled() {
        return configuration.getMinimumLevel().intValue() <= AuditLevel.WARN.intValue();
    }

    @Override
    public void warn(String message) {
        warnInternal(message, new AuditDetail(), null);
    }

    @Override
    public void warn(String message, Object... args) {
        warnInternal(message, new AuditDetail(), args);
    }

    @Override
    public void warn(String message, AuditDetail detail) {
        warnInternal(message, detail, null);
    }

    @Override
    public void warn(String message, AuditDetail detail, Object... args) {
        warnInternal(message, detail, args);
    }

    private void warnInternal(String message, AuditDetail detail, Object[] args) {
        if (!isWarnEnabled()) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, AuditLevel.WARN, detail.getType(), detail.getParam(), args);
        write(auditRecord);
    }

    @Override
    public boolean isInfoEnabled() {
        return configuration.getMinimumLevel().intValue() <= AuditLevel.INFO.intValue();
    }

    @Override
    public void info(String message) {
        infoInternal(message, new AuditDetail(), null);
    }

    @Override
    public void info(String message, Object... args) {
        infoInternal(message, new AuditDetail(), args);
    }

    @Override
    public void info(String message, AuditDetail detail) {
        infoInternal(message, detail, null);
    }

    @Override
    public void info(String message, AuditDetail detail, Object... args) {
        infoInternal(message, detail, args);
    }

    private void infoInternal(String message, AuditDetail detail, Object[] args) {
        if (!isInfoEnabled()) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, AuditLevel.INFO, detail.getType(), detail.getParam(), args);
        write(auditRecord);
    }

    @Override
    public boolean isDebugEnabled() {
        return configuration.getMinimumLevel().intValue() <= AuditLevel.DEBUG.intValue();
    }

    @Override
    public void debug(String message) {
        debugInternal(message, new AuditDetail(), null);
    }

    @Override
    public void debug(String message, Object... args) {
        debugInternal(message, new AuditDetail(), args);
    }

    @Override
    public void debug(String message, AuditDetail detail) {
        debugInternal(message, detail, null);
    }

    @Override
    public void debug(String message, AuditDetail detail, Object... args) {
        debugInternal(message, detail, args);
    }

    private void debugInternal(String message, AuditDetail detail, Object[] args) {
        if (!isDebugEnabled()) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, AuditLevel.DEBUG, detail.getType(), detail.getParam(), args);
        write(auditRecord);
    }

    @Override
    public boolean isTraceEnabled() {
        return configuration.getMinimumLevel().intValue() <= AuditLevel.TRACE.intValue();
    }

    @Override
    public void trace(String message) {
        traceInternal(message, new AuditDetail(), null);
    }

    @Override
    public void trace(String message, Object... args) {
        traceInternal(message, new AuditDetail(), args);
    }

    @Override
    public void trace(String message, AuditDetail detail) {
        traceInternal(message, detail, null);
    }

    @Override
    public void trace(String message, AuditDetail detail, Object... args) {
        traceInternal(message, detail, args);
    }

    private void traceInternal(String message, AuditDetail detail, Object[] args) {
        if (!isTraceEnabled()) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, AuditLevel.TRACE, detail.getType(), detail.getParam(), args);
        write(auditRecord);
    }

    @Override
    public boolean isLevelEnabled(@NonNull AuditLevel level) {
        return configuration.getMinimumLevel().intValue() <= level.intValue();
    }

    @Override
    public void log(String message, AuditLevel level) {
        logInternal(message, level, new AuditDetail(), null);
    }

    @Override
    public void log(String message, AuditLevel level, Object... args) {
        logInternal(message, level, new AuditDetail(), args);
    }

    @Override
    public void log(String message, AuditLevel level, AuditDetail detail) {
        logInternal(message, level, detail, null);
    }

    @Override
    public void log(String message, AuditLevel level, AuditDetail detail, Object... args) {
        logInternal(message, level, detail, args);
    }

    private void logInternal(String message, AuditLevel level, AuditDetail detail, Object[] args) {
        if (!isLevelEnabled(level)) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, level, detail.getType(), detail.getParam(), args);
        write(auditRecord);
    }

    private void write(AuditRecord auditRecord) {
        try {
            writer.write(auditRecord);
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            logger.warn("Audit failed, error: {}", ex.getMessage());
        }
    }

    @Override
    public void flush() {
        writer.flush();
    }

    @Override
    public void cleanup() {
        writer.cleanup();
    }

}
