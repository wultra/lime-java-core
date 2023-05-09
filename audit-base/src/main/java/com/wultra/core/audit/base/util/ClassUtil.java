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
    public static String getCallingClass(final List<String> packageFilter) {
        final StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        for (final StackTraceElement t : trace) {
            final String className = t.getClassName();
            if (Thread.class.getName().equals(className) || ClassUtil.class.getName().equals(className)) {
                continue;
            }
            if (!packageMatches(className, packageFilter)) {
                return className;
            }
        }
        return trace[trace.length - 1].getClassName();
    }

    private static boolean packageMatches(final String className, List<String> packageFilter) {
        if (packageFilter == null) {
            return false;
        }
        for (final String pf : packageFilter) {
            if (className.startsWith(pf)) {
                return true;
            }
        }
        return false;
    }

}