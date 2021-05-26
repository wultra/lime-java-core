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

import com.wultra.core.audit.base.model.AuditLevel;

import java.util.Map;

/**
 * Audit interface.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public interface Audit {

    String getName();

    boolean isErrorEnabled();

    void error(String message);

    void error(String message, Object ... args);

    void error(String message, Map<String, Object> param);

    void error(String message, Map<String, Object> param, Object ... args);

    boolean isWarnEnabled();

    void warn(String message);

    void warn(String message, Object ... args);

    void warn(String message, Map<String, Object> param);

    void warn(String message, Map<String, Object> param, Object ... args);

    boolean isInfoEnabled();

    void info(String message);

    void info(String message, Object ... args);

    void info(String message, Map<String, Object> param);

    void info(String message, Map<String, Object> param, Object ... args);

    boolean isDebugEnabled();

    void debug(String message);

    void debug(String message, Object ... args);

    void debug(String message, Map<String, Object> param);

    void debug(String message, Map<String, Object> param, Object ... args);

    boolean isTraceEnabled();

    void trace(String message);

    void trace(String message, Object ... args);

    void trace(String message, Map<String, Object> param);

    void trace(String message, Map<String, Object> param, Object ... args);

    boolean isLevelEnabled(AuditLevel level);

    void log(String message, AuditLevel level);

    void log(String message, AuditLevel level, Object ... args);

    void log(String message, AuditLevel level, Map<String, Object> param);

    void log(String message, AuditLevel level, Map<String, Object> param, Object ... args);

    void flush();

    void cleanup();

}
