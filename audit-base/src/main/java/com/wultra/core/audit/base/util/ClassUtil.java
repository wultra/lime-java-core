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

/**
 * Utility for obtaining information about calling class.
 */
public class ClassUtil extends SecurityManager {

    private static final ClassUtil INSTANCE = new ClassUtil();

    public static Class<?> getCallingClass(String packageFilter) {
        Class<?>[] trace = INSTANCE.getClassContext();
        for (Class<?> cl : trace) {
            if (cl.getName().equals(ClassUtil.class.getName())) {
                continue;
            }
            if (packageFilter == null || !cl.getPackage().getName().startsWith(packageFilter)) {
                return cl;
            }
        }
        return trace[trace.length - 1];
    }

    private static final class ClassContextSecurityManager extends SecurityManager {
        private ClassContextSecurityManager() {
        }

        protected Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }

}