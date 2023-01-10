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

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test for {@link RequestContextConverter}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class RequestContextConverterTest {

    @Test
    void testNullRequest() {
        final RequestContext result = RequestContextConverter.convert(null);

        assertNull(result);
    }

    @Test
    void testUserAgentNull() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final RequestContext result = RequestContextConverter.convert(request);

        assertNull(result.getUserAgent());
    }

    @Test
    void testUserAgent() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0");

        final RequestContext result = RequestContextConverter.convert(request);

        assertEquals("Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0", result.getUserAgent());
    }

    @Test
    void testIpAddress() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final RequestContext result = RequestContextConverter.convert(request);

        assertEquals("127.0.0.1", result.getIpAddress());
    }

    @Test
    void testIpAddressProxy() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Proxy-Client-IP", "\t");
        request.addHeader("HTTP_X_FORWARDED_FOR", "unKNOWN");
        request.addHeader("HTTP_X_FORWARDED", "192.168.1.134");

        final RequestContext result = RequestContextConverter.convert(request);

        assertEquals("192.168.1.134", result.getIpAddress());
    }
}
