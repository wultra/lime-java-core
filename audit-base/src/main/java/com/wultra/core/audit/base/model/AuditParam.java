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

import java.util.Date;
import java.util.Objects;

/**
 * Audit parameter model class.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuditParam {

    private final String auditLogId;
    private final Date timestamp;
    private final String key;
    private final Object value;

    public AuditParam(String auditLogId, Date timestamp, String key, Object value) {
        this.auditLogId = auditLogId;
        this.timestamp = timestamp;
        this.key = key;
        this.value = value;
    }

    public String getAuditLogId() {
        return auditLogId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditParam that = (AuditParam) o;
        return auditLogId.equals(that.auditLogId) && timestamp.equals(that.timestamp) && key.equals(that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auditLogId, timestamp, key, value);
    }

    @Override
    public String toString() {
        return "AuditParam{" +
                "auditLogId='" + auditLogId + '\'' +
                ", timestamp=" + timestamp +
                ", key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
