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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility for obtaining information about calling class.
 */
public class ClassUtil extends SecurityManager {

    private static final ClassUtil INSTANCE = new ClassUtil();

    /**
     * Get calling class.
     * @param packageFilter Packages to filter out when resolving calling class.
     * @return Calling class.
     */
    public static Class<?> getCallingClass(List<String> packageFilter) {
        final Class<?>[] trace = INSTANCE.getClassContext();
        if (trace == null) {
            return null;
        }
        if (packageFilter == null) {
            packageFilter = Collections.emptyList();
        }
        final List<String> filterFinal = packageFilter;
        return Arrays.stream(trace)
                .filter(it -> !it.isAssignableFrom(ClassUtil.class))
                .filter(it -> !packageMatches(it.getPackage().getName(), filterFinal))
                .findFirst()
                .orElse(trace[trace.length - 1]);
    }

    private static boolean packageMatches(String pkg, List<String> packageFilter) {
        return packageFilter.stream().anyMatch(pkg::startsWith);
    }

}