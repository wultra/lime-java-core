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
package com.wultra.core.audit.base.model;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Audit record model class.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuditRecord {

    private final String id;
    private final Date timestamp;
    private final AuditLevel level;
    private final Map<String, Object> param;
    private String message;
    private Throwable throwable;
    private Class<?> callingClass;
    private String threadName;

    /**
     * Audit record constructor.
     * @param message Audit message or message pattern in case message arguments are present.
     * @param level Audit level.
     * @param param Audit parameters.
     * @param args Message arguments.
     */
    public AuditRecord(@NonNull String message, @NonNull AuditLevel level, @NonNull Map<String, Object> param, Object[] args) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date();
        this.level = level;
        this.param = param;
        if (args != null) {
            parseArgs(message, args);
        } else {
            this.message = message;
        }
    }

    private void parseArgs(String messagePattern, Object[] args) {
        final FormattingTuple formattingTuple = MessageFormatter.arrayFormat(messagePattern, args);
        this.message = formattingTuple.getMessage();
        this.throwable = formattingTuple.getThrowable();
    }

    /**
     * Get audit record identifier.
     * @return Audit record identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Get timestamp when audit record was created.
     * @return Timestamp when audit record was created.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Get audit message.
     * @return Audit message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get audit level.
     * @return Audit level.
     */
    public AuditLevel getLevel() {
        return level;
    }

    /**
     * Get throwable.
     * @return Throwable.
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Get audit parameters.
     * @return Audit parameters.
     */
    public Map<String, Object> getParam() {
        return param;
    }

    /**
     * Get calling class.
     * @return Calling class.
     */
    public Class<?> getCallingClass() {
        return callingClass;
    }

    /**
     * Set calling class.
     * @param callingClass Calling class.
     */
    public void setCallingClass(Class<?> callingClass) {
        this.callingClass = callingClass;
    }

    /**
     * Get thread name.
     * @return Thread name.
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Set thread name.
     * @param threadName Thread name.
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditRecord that = (AuditRecord) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuditRecord{" +
                "timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", level=" + level +
                ", throwable=" + throwable +
                ", param=" + param +
                '}';
    }
}
