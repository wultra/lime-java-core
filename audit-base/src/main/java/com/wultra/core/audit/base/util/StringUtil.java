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
 * String utilities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class StringUtil {

    /**
     * Trim the input string.
     * @param str String to trim.
     * @param maxLength Maximum String length.
     * @return Trimmed string.
     */
    public static String trim(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        str = str.trim();
        if (str.length() > maxLength) {
            str = str.substring(0, maxLength);
        }
        return str;
    }

}
