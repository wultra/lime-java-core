/*
 * Copyright 2023 Wultra s.r.o.
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
package com.wultra.core.http.common.request;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Converter for HTTP request context information.
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
public final class RequestContextConverter {

    /**
     * List of HTTP headers that may contain the actual IP address
     * when hidden behind a proxy component.
     */
    private static final List<String> HTTP_HEADERS_IP_ADDRESS = Collections.unmodifiableList(Arrays.asList(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    ));

    private static final String HTTP_HEADER_USER_AGENT = "User-Agent";

    private RequestContextConverter() {
        throw new IllegalStateException("Should not be instantiated");
    }

    /**
     * Convert HTTP Servlet Request to request context representation.
     *
     * @param source HttpServletRequest instance.
     * @return Request context data.
     */
    public static RequestContext convert(final HttpServletRequest source) {
        if (source == null) {
            return null;
        }
        return RequestContext.builder()
                .userAgent(source.getHeader(HTTP_HEADER_USER_AGENT))
                .ipAddress(getClientIpAddress(source))
                .build();
    }

    /**
     * Obtain the best-effort guess of the client IP address.
     * @param request HttpServletRequest instance.
     * @return Best-effort information about the client IP address.
     */
    private static String getClientIpAddress(final HttpServletRequest request) {
        if (request == null) { // safety null check
            return null;
        }
        for (String header : HTTP_HEADERS_IP_ADDRESS) {
            final String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}
