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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for the user agent parser.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
class UserAgentTest {

    private static final String[] USER_AGENTS = new String[] {
            "PowerAuthNetworking/1.1.7 (en; cellular) com.wultra.app.Mobile-Token.wultra_test/2.0.0 (Apple; iOS/16.6.1; iphone12,3)",
            "PowerAuthNetworking/1.2.1 (uk; wifi) com.wultra.android.mtoken.gdnexttest/1.0.0-gdnexttest (samsung; Android/13; SM-A047F)",
            "PowerAuthNetworking/1.1.7 (en; unknown) com.wultra.app.MobileToken.wtest/2.0.0 (Apple; iOS/16.6.1; iphone10,6)",
            "PowerAuthNetworking/1.1.7 (en; wifi) com.wultra.app.MobileToken.wtest/2.0.0 (Apple; iOS/16.7.1; iphone10,6)"
    };

    private static final String DEVICES = """
            [
                {
                    "networkVersion": "1.1.7",
                    "language": "en",
                    "connection": "cellular",
                    "product": "com.wultra.app.Mobile-Token.wultra_test",
                    "version": "2.0.0",
                    "platform": "Apple",
                    "os": "iOS",
                    "osVersion": "16.6.1",
                    "model": "iphone12,3"
                },
                {
                    "networkVersion": "1.2.1",
                    "language": "uk",
                    "connection": "wifi",
                    "product": "com.wultra.android.mtoken.gdnexttest",
                    "version": "1.0.0-gdnexttest",
                    "platform": "samsung",
                    "os": "Android",
                    "osVersion": "13",
                    "model": "SM-A047F"
                },
                {
                    "networkVersion": "1.1.7",
                    "language": "en",
                    "connection": "unknown",
                    "product": "com.wultra.app.MobileToken.wtest",
                    "version": "2.0.0",
                    "platform": "Apple",
                    "os": "iOS",
                    "osVersion": "16.6.1",
                    "model": "iphone10,6"
                },
                {
                    "networkVersion": "1.1.7",
                    "language": "en",
                    "connection": "wifi",
                    "product": "com.wultra.app.MobileToken.wtest",
                    "version": "2.0.0",
                    "platform": "Apple",
                    "os": "iOS",
                    "osVersion": "16.7.1",
                    "model": "iphone10,6"
                }
            ]
            """;

    @Test
    void parse() {
        final UserAgent.Device[] expectedDevices = readDevices();
        for (int i = 0; i < USER_AGENTS.length; i++) {
            final Optional<UserAgent.Device> deviceOptional = UserAgent.parse(USER_AGENTS[i]);
            assertTrue(deviceOptional.isPresent());

        final UserAgent.Device device = deviceOptional.get();
            final UserAgent.Device expectedDevice = expectedDevices[i];
            assertEquals(expectedDevice, device);
        }
        //printCurrentDevices(devices);
    }

    private static UserAgent.Device[] readDevices() {
        try {
            final ObjectMapper om = new ObjectMapper();
            return om.readValue(UserAgentTest.DEVICES, UserAgent.Device[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printCurrentDevices(List<UserAgent.Device> devices) {
        try {
            final ObjectMapper om = new ObjectMapper();
            final String s = om.writeValueAsString(devices);
            System.out.println(s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}