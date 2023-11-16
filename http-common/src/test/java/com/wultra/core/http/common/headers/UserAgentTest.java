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
package com.wultra.core.http.common.headers;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for the user agent parser.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
class UserAgentTest {

    @Test
    void parse() {
        final String sample = "PowerAuthNetworking/1.1.7 (en; cellular) com.wultra.app.Mobile-Token.wultra_test/2.0.0 (Apple; iOS/16.6.1; iphone12,3)";
        final Optional<UserAgent.Device> deviceOptional = UserAgent.parse(sample);
        assertTrue(deviceOptional.isPresent());

        final UserAgent.Device device = deviceOptional.get();
        assertEquals("1.1.7", device.getNetworkVersion());
        assertEquals("en", device.getLanguage());
        assertEquals("cellular", device.getConnection());
        assertEquals("com.wultra.app.Mobile-Token.wultra_test", device.getProduct());
        assertEquals("2.0.0", device.getVersion());
        assertEquals("Apple", device.getPlatform());
        assertEquals("iOS", device.getOs());
        assertEquals("16.6.1", device.getOsVersion());
        assertEquals("iphone12,3", device.getModel());
    }
}