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

/**
 * Audit level enumeration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum AuditLevel {

    /**
     * No auditing.
     */
    NONE(99, "NONE"),

    /**
     * Error level.
     */
    ERROR(40, "ERROR"),

    /**
     * Warning level.
     */
    WARN(30, "WARN"),

    /**
     * Informational level.
     */
    INFO(20, "INFO"),

    /**
     * Debug level.
     */
    DEBUG(10, "DEBUG"),

    /**
     * Trace level.
     */
    TRACE(0, "TRACE"),

    /**
     * All levels.
     */
    ALL(0, "ALL");

    private final int level;
    private final String name;

    /**
     * Audit level constructor.
     * @param level Audit level numeric value.
     * @param name Audit level name.
     */
    AuditLevel(int level, String name) {
        this.level = level;
        this.name = name;
    }

    /**
     * Get numeric value of audit level.
     * @return Numeric value of audit level.
     */
    public int intValue() {
        return this.level;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
