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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Audit record model class.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuditRecord {

    private final Date timestamp;
    private final AuditLevel level;
    private final Map<String, Object> param;
    private String message;
    private Throwable throwable;
    private Class<?> callingClass;
    private String threadName;

    public AuditRecord(String message, AuditLevel level, Map<String, Object> param, Object[] args) {
        if (message == null) {
            throw new IllegalArgumentException("Audit message is null");
        }
        if (level == null) {
            throw new IllegalArgumentException("Audit level is null");
        }
        this.timestamp = new Date();
        this.level = level;
        if (param == null) {
            this.param = new LinkedHashMap<>();
        } else {
            this.param = param;
        }
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

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public AuditLevel getLevel() {
        return level;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public Class<?> getCallingClass() {
        return callingClass;
    }

    public void setCallingClass(Class<?> callingClass) {
        this.callingClass = callingClass;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AuditRecord that = (AuditRecord) o;
        return timestamp.equals(that.timestamp) && message.equals(that.message) && level == that.level && Objects.equals(throwable, that.throwable) && param.equals(that.param);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, message, level, throwable, param);
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
