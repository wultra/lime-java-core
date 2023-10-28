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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for processing our standard user agent strings.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Slf4j
public final class UserAgent {

    @Data
    public static class Device {
        private String networkVersion;
        private String language;
        private String connection;
        private String product;
        private String version;
        private String platform;
        private String os;
        private String osVersion;
        private String model;
    }

    private UserAgent() {
    }

    private static final Pattern patternPrefix = Pattern.compile("^PowerAuthNetworking/(?<networkVersion>[0-9]+\\.[0-9]+\\.[0-9]+).*");
    private static final Pattern patternV1 = Pattern.compile("^PowerAuthNetworking/(?<networkVersion>[0-9]+\\.[0-9]+\\.[0-9]+) " +
            "\\((?<language>[a-zA-Z]{2}); (?<connection>[a-zA-Z0-9]+)\\) " +
            "(?<product>[a-zA-Z0-9-_.]+)/(?<version>[0-9.]+(-[^ ]*)?) .*" +
            "\\((?<platform>[^;]+); (?<os>[^/]+)/(?<osVersion>[^;]+); (?<model>[^)]+)\\)$");

    /**
     * Parse client user from the HTTP header value.
     *
     * @param userAgent User-Agent Header String
     * @return Parsed device info, or empty if the user agent header cannot be parsed.
     */
    public static Optional<Device> parse(String userAgent) {
        // Identify if the user agent is ours and in what version
        logger.debug("Parsing user agent value: {}", userAgent);
        final Matcher matcherPrefix = patternPrefix.matcher(userAgent);
        if (!matcherPrefix.matches()) {
            return Optional.empty();
        }
        final String networkVersion = matcherPrefix.group("networkVersion");
        logger.debug("Declared networkVersion: {}", networkVersion);
        if (!networkVersion.startsWith("1.")) { // simplistic matching for current v1.x clients
            return Optional.empty();
        }

        // Parse the device object
        return parseUserAgentV1(userAgent);
    }

    /**
     * Private method for parsing client user from the v1.x mobile clients. It is added for convenience
     * when new versions with another formats will be eventually introduced.
     *
     * @param userAgent User-Agent Header String
     * @return Parsed device info, or empty if the user agent header cannot be parsed.
     */
    private static Optional<Device> parseUserAgentV1(String userAgent) {
        final Matcher matcher = patternV1.matcher(userAgent);
        if (matcher.matches()) {
            final Device device = new Device();
            device.setNetworkVersion(matcher.group("networkVersion"));
            device.setLanguage(matcher.group("language"));
            device.setConnection(matcher.group("connection"));
            device.setProduct(matcher.group("product"));
            device.setVersion(matcher.group("version"));
            device.setPlatform(matcher.group("platform"));
            device.setOs(matcher.group("os"));
            device.setOsVersion(matcher.group("osVersion"));
            device.setModel(matcher.group("model"));
            return Optional.of(device);
        }
        logger.debug("The user agent value does not match v1 client format");
        return Optional.empty();
    }

}
