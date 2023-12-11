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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link UserAgent}.
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class UserAgentTest {

    @ParameterizedTest
    @MethodSource("provideUserAgents")
    void testParse(final String userAgent, final UserAgent.Device expectedDevice) {
        final Optional<UserAgent.Device> deviceOptional = UserAgent.parse(userAgent);
        assertTrue(deviceOptional.isPresent(), "Unable to parse user-agent: " + userAgent);
        assertEquals(expectedDevice, deviceOptional.get());
    }

    private static Stream<Arguments> provideUserAgents() throws JsonProcessingException {
        return Stream.of(
                Arguments.of("PowerAuthNetworking/1.1.7 (en; cellular) com.wultra.app.Mobile-Token.wultra_test/2.0.0 (Apple; iOS/16.6.1; iphone12,3)", readDevice("""
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
                        }
                        """)),
                Arguments.of("PowerAuthNetworking/1.2.1 (uk; wifi) com.wultra.android.mtoken.gdnexttest/1.0.0-gdnexttest (samsung; Android/13; SM-A047F)", readDevice("""
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
                        }
                        """)),
                Arguments.of("PowerAuthNetworking/1.1.7 (en; unknown) com.wultra.app.MobileToken.wtest/2.0.0 (Apple; iOS/16.6.1; iphone10,6)", readDevice("""
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
                        }
                        """)),
                Arguments.of("PowerAuthNetworking/1.1.7 (en; wifi) com.wultra.app.MobileToken.wtest/2.0.0 (Apple; iOS/16.7.1; iphone10,6)", readDevice("""
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
                        """)),
                // MainBundle/Version PowerAuth2/Version (iOS Version, deviceString)
                Arguments.of("PowerAuth2TestsHostApp-ios/1.0 PowerAuth2/1.7.8 (iOS 17.0, simulator)", readDevice("""
                        {
                            "networkVersion": "1.7.8",
                            "os": "iOS",
                            "osVersion": "17.0",
                            "model": "simulator"
                        }
                        """)),
                // PowerAuth2/Version (Android Version, Build.MANUFACTURER Build.MODEL)
                Arguments.of("PowerAuth2/1.7.8 (Android 13, Google Pixel 4)", readDevice("""
                        {
                            "networkVersion": "1.7.8",
                            "os": "Android",
                            "osVersion": "13",
                            "model": "Google Pixel 4"
                        }
                        """)),
                Arguments.of("MobileToken/1.2.0 PowerAuth2/1.7.8 (iOS 15.7.9, iPhone9,3)", readDevice("""
                        {
                            "networkVersion": "1.7.8",
                            "os": "iOS",
                            "osVersion": "15.7.9",
                            "model": "iPhone9,3"
                        }
                        """))
        );
    }

    private static UserAgent.Device readDevice(final String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, UserAgent.Device.class);
    }

}
