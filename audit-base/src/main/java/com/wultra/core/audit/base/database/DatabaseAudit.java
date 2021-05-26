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
import com.wultra.core.audit.base.model.AuditLevel;
import com.wultra.core.audit.base.model.AuditRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * Database audit implementation.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class DatabaseAudit implements Audit {

    private final AuditWriter writer;
    private final AuditConfiguration configuration;

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
        errorInternal(message, Collections.emptyMap(), null);
    }

    @Override
    public void error(String message, Object... args) {
        errorInternal(message, Collections.emptyMap(), args);
    }

    @Override
    public void error(String message, Map<String, Object> param) {
        errorInternal(message, param, null);
    }

    @Override
    public void error(String message, Map<String, Object> param, Object... args) {
        errorInternal(message, param, args);
    }

    private void errorInternal(String message, Map<String, Object> param, Object[] args) {
        if (!isErrorEnabled()) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, AuditLevel.ERROR, param, args);
        writer.write(auditRecord);
    }

    @Override
    public boolean isWarnEnabled() {
        return configuration.getMinimumLevel().intValue() <= AuditLevel.WARN.intValue();
    }

    @Override
    public void warn(String message) {
        warnInternal(message, Collections.emptyMap(), null);
    }

    @Override
    public void warn(String message, Object... args) {
        warnInternal(message, Collections.emptyMap(), args);
    }

    @Override
    public void warn(String message, Map<String, Object> param) {
        warnInternal(message, param, null);
    }

    @Override
    public void warn(String message, Map<String, Object> param, Object... args) {
        warnInternal(message, param, args);
    }

    private void warnInternal(String message, Map<String, Object> param, Object[] args) {
        if (!isWarnEnabled()) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, AuditLevel.WARN, param, args);
        writer.write(auditRecord);
    }

    @Override
    public boolean isInfoEnabled() {
        return configuration.getMinimumLevel().intValue() <= AuditLevel.INFO.intValue();
    }

    @Override
    public void info(String message) {
        infoInternal(message, Collections.emptyMap(), null);
    }

    @Override
    public void info(String message, Object... args) {
        infoInternal(message, Collections.emptyMap(), args);
    }

    @Override
    public void info(String message, Map<String, Object> param) {
        infoInternal(message, param, null);
    }

    @Override
    public void info(String message, Map<String, Object> param, Object... args) {
        infoInternal(message, param, args);
    }

    private void infoInternal(String message, Map<String, Object> param, Object[] args) {
        if (!isInfoEnabled()) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, AuditLevel.INFO, param, args);
        writer.write(auditRecord);
    }

    @Override
    public boolean isDebugEnabled() {
        return configuration.getMinimumLevel().intValue() <= AuditLevel.DEBUG.intValue();
    }

    @Override
    public void debug(String message) {
        debugInternal(message, Collections.emptyMap(), null);
    }

    @Override
    public void debug(String message, Object... args) {
        debugInternal(message, Collections.emptyMap(), args);
    }

    @Override
    public void debug(String message, Map<String, Object> param) {
        debugInternal(message, param, null);
    }

    @Override
    public void debug(String message, Map<String, Object> param, Object... args) {
        debugInternal(message, param, args);
    }

    private void debugInternal(String message, Map<String, Object> param, Object[] args) {
        if (!isDebugEnabled()) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, AuditLevel.DEBUG, param, args);
        writer.write(auditRecord);
    }

    @Override
    public boolean isTraceEnabled() {
        return configuration.getMinimumLevel().intValue() <= AuditLevel.TRACE.intValue();
    }

    @Override
    public void trace(String message) {
        traceInternal(message, Collections.emptyMap(), null);
    }

    @Override
    public void trace(String message, Object... args) {
        traceInternal(message, Collections.emptyMap(), args);
    }

    @Override
    public void trace(String message, Map<String, Object> param) {
        traceInternal(message, param, null);
    }

    @Override
    public void trace(String message, Map<String, Object> param, Object... args) {
        traceInternal(message, param, args);
    }

    private void traceInternal(String message, Map<String, Object> param, Object[] args) {
        if (!isTraceEnabled()) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, AuditLevel.TRACE, param, args);
        writer.write(auditRecord);
    }

    @Override
    public boolean isLevelEnabled(@NonNull AuditLevel level) {
        return configuration.getMinimumLevel().intValue() >= level.intValue();
    }

    @Override
    public void log(String message, AuditLevel level) {
        logInternal(message, level, Collections.emptyMap(), null);
    }

    @Override
    public void log(String message, AuditLevel level, Object... args) {
        logInternal(message, level, Collections.emptyMap(), args);
    }

    @Override
    public void log(String message, AuditLevel level, Map<String, Object> param) {
        logInternal(message, level, param, null);
    }

    @Override
    public void log(String message, AuditLevel level, Map<String, Object> param, Object... args) {
        logInternal(message, level, param, args);
    }

    private void logInternal(String message, AuditLevel level, Map<String, Object> param, Object[] args) {
        if (!isLevelEnabled(level)) {
            return;
        }
        final AuditRecord auditRecord = new AuditRecord(message, level, param, args);
        writer.write(auditRecord);
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
