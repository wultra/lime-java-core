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
package com.wultra.core.audit.base;

import com.wultra.core.audit.base.model.AuditDetail;
import com.wultra.core.audit.base.model.AuditLevel;

/**
 * Audit interface.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public interface Audit {

    /**
     * Get audit implementation name.
     * @return Audit implementation name.
     */
    String getName();

    /**
     * Get whether error auditing is enabled.
     * @return Whether error auditing is enabled.
     */
    boolean isErrorEnabled();

    /**
     * Create an error audit record.
     * @param message Error message.
     */
    void error(String message);

    /**
     * Create an error audit record.
     * @param message Error message pattern.
     * @param args Error message arguments.
     */
    void error(String message, Object ... args);

    /**
     * Create an error audit record.
     * @param message Error message.
     * @param detail Detailed audit information.
     */
    void error(String message, AuditDetail detail);

    /**
     * Create an error audit record.
     * @param message Error message pattern.
     * @param detail Detailed audit information.
     * @param args Error message arguments.
     */
    void error(String message, AuditDetail detail, Object ... args);

    /**
     * Get whether warning auditing is enabled.
     * @return Whether warning auditing is enabled.
     */
    boolean isWarnEnabled();

    /**
     * Create a warning audit record.
     * @param message Warning message.
     */
    void warn(String message);

    /**
     * Create a warning audit record.
     * @param message Warning message pattern.
     * @param args Warning message arguments.
     */
    void warn(String message, Object ... args);

    /**
     * Create a warning audit record.
     * @param message Warning message.
     * @param detail Detailed audit information.
     */
    void warn(String message, AuditDetail detail);

    /**
     * Create a warning audit record.
     * @param message Warning message pattern.
     * @param detail Detailed audit information.
     * @param args Warning message arguments.
     */
    void warn(String message, AuditDetail detail, Object ... args);

    /**
     * Get whether informational auditing is enabled.
     * @return Whether informational auditing is enabled.
     */
    boolean isInfoEnabled();

    /**
     * Create an informational audit record.
     * @param message Informational message.
     */
    void info(String message);

    /**
     * Create an informational audit record.
     * @param message Informational message pattern.
     * @param args Informational message arguments.
     */
    void info(String message, Object ... args);

    /**
     * Create an informational audit record.
     * @param message Informational message.
     * @param detail Detailed audit information.
     */
    void info(String message, AuditDetail detail);

    /**
     * Create an informational audit record.
     * @param message Informational message pattern.
     * @param detail Detailed audit information.
     * @param args Informational message arguments.
     */
    void info(String message, AuditDetail detail, Object ... args);

    /**
     * Get whether debug auditing is enabled.
     * @return Whether debug auditing is enabled.
     */
    boolean isDebugEnabled();

    /**
     * Create a debug audit record.
     * @param message Debug message.
     */
    void debug(String message);

    /**
     * Create a debug audit record.
     * @param message Debug message pattern.
     * @param args Debug message arguments.
     */
    void debug(String message, Object ... args);

    /**
     * Create a debug audit record.
     * @param message Debug message.
     * @param detail Detailed audit information.
     */
    void debug(String message, AuditDetail detail);

    /**
     * Create a debug audit record.
     * @param message Debug message pattern.
     * @param detail Detailed audit information.
     * @param args Debug message arguments.
     */
    void debug(String message, AuditDetail detail, Object ... args);

    /**
     * Get whether trace auditing is enabled.
     * @return Whether trace auditing is enabled.
     */
    boolean isTraceEnabled();

    /**
     * Create a trace audit record.
     * @param message Trace message.
     */
    void trace(String message);

    /**
     * Create a trace audit record.
     * @param message Trace message pattern.
     * @param args Trace message arguments.
     */
    void trace(String message, Object ... args);

    /**
     * Create a trace audit record.
     * @param message Trace message.
     * @param detail Detailed audit information.
     */
    void trace(String message, AuditDetail detail);

    /**
     * Create a trace audit record.
     * @param message Trace message.
     * @param detail Detailed audit information.
     * @param args Trace message arguments.
     */
    void trace(String message, AuditDetail detail, Object ... args);

    /**
     * Get whether specified auditing level is enabled.
     * @param level Auditing level.
     * @return Whether specified auditing level is enabled.
     */
    boolean isLevelEnabled(AuditLevel level);

    /**
     * Create an audit record.
     * @param message Audit message.
     * @param level Audit level.
     */
    void log(String message, AuditLevel level);

    /**
     * Create an audit record.
     * @param message Audit message pattern.
     * @param level Audit level.
     * @param args Audit message arguments.
     */
    void log(String message, AuditLevel level, Object ... args);

    /**
     * Create an audit record.
     * @param message Audit message.
     * @param level Audit level.
     * @param detail Detailed audit information.
     */
    void log(String message, AuditLevel level, AuditDetail detail);

    /**
     * Crate an audit record.
     * @param message Audit message pattern.
     * @param level Audit level.
     * @param detail Detailed audit information.
     * @param args Audit message arguments.
     */
    void log(String message, AuditLevel level, AuditDetail detail, Object ... args);

    /**
     * Flush the audit record queue.
     */
    void flush();

    /**
     * Perform audit record cleanup.
     */
    void cleanup();

}
