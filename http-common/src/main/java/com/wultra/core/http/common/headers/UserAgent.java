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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for processing our standard user agent strings.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class UserAgent {

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

    private static final String USER_AGENT_TEMPLATE_PREFIX_V1 = "^PowerAuthNetworking/(?<networkVersion>[0-9]+\\.[0-9]+\\.[0-9]+).*";
    private static final String USER_AGENT_TEMPLATE_V1 = "^PowerAuthNetworking/(?<networkVersion>[0-9]+\\.[0-9]+\\.[0-9]+) " +
            "\\((?<language>[a-zA-Z]{2}); (?<connection>[a-zA-Z0-9]+)\\) " +
            "(?<product>[a-zA-Z0-9-_.]+)/(?<version>[0-9.]+) .*" +
            "\\((?<platform>[^;]+); (?<os>[^/]+)/(?<osVersion>[^;]+); (?<model>[^)]+)\\)$";

    public static Device parse(String userAgent) {
        // Identify if the user agent is ours and in what version
        final Pattern patternPrefix = Pattern.compile(USER_AGENT_TEMPLATE_PREFIX_V1);
        final Matcher matcherPrefix = patternPrefix.matcher(userAgent);
        if (!matcherPrefix.matches()) {
            return null;
        }
        final String networkVersion = matcherPrefix.group("networkVersion");
        if (!networkVersion.startsWith("1.")) { // simplistic matching for current v1.x clients
            return null;
        }

        // Parse the device object
        return parseUserAgentV1(userAgent);

    }

    /**
     * Private method for parsing client user from the v1.x mobile clients. It is added for convenience
     * when new versions with another formats will be eventually introduced.
     *
     * @param userAgent User-Agent Header String
     * @return Parsed device info, or null if the user agent header cannot be parsed.
     */
    private static Device parseUserAgentV1(String userAgent) {
        final Pattern pattern = Pattern.compile(USER_AGENT_TEMPLATE_V1);
        final Matcher matcher = pattern.matcher(userAgent);
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
            return device;
        }
        return null;
    }

}
