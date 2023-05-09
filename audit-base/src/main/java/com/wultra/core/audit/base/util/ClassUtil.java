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
package com.wultra.core.audit.base.util;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Utility for obtaining information about calling class.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Slf4j
public final class ClassUtil {

    private ClassUtil() {
        throw new IllegalStateException("Should not be instantiated");
    }

    /**
     * Get calling class.
     * @param packageFilter Packages to filter out when resolving calling class.
     * @return Calling class.
     */
    public static Class<?> getCallingClass(final List<String> packageFilter) {
        final StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        for (final StackTraceElement t : trace) {
            final Class<?> clazz = fechClass(t);
            if (clazz == null || clazz.isAssignableFrom(Thread.class) || clazz.isAssignableFrom(ClassUtil.class)) {
                continue;
            }
            if (!packageMatches(clazz.getPackage().getName(), packageFilter)) {
                return clazz;
            }
        }
        return trace[trace.length - 1].getClass();
    }

    private static Class<?> fechClass(final StackTraceElement t) {
        final String className = t.getClassName();

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.warn("Unable to create class for name: {}, {}", className, e.getMessage());
            logger.debug("Unable to create class for name: {}", className, e);
            return null;
        }
    }

    private static boolean packageMatches(String pkg, List<String> packageFilter) {
        if (packageFilter == null) {
            return false;
        }
        for (final String pf : packageFilter) {
            if (pkg.startsWith(pf)) {
                return true;
            }
        }
        return false;
    }

}