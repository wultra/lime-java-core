/*
 * Copyright 2020 Wultra s.r.o.
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
package com.wultra.core.rest.client.base;

import org.springframework.http.MediaType;

/**
 * REST client configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class RestClientConfiguration {

    public RestClientConfiguration(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // Basic settings
    private final String baseUrl;
    private MediaType contentType = MediaType.APPLICATION_JSON;
    private MediaType acceptType = MediaType.APPLICATION_JSON;

    // HTTP proxy settings
    private boolean proxyEnabled = false;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    // HTTP connection timeout
    private Integer connectionTimeout = 5000;

    // TLS certificate settings
    private boolean acceptInvalidSslCertificate = false;

    // HTTP message settings
    private Integer maxInMemorySize = 1024 * 1024;

    // HTTP basic authentication
    private boolean httpBasicAuthEnabled = false;
    private String httpBasicAuthUsername;
    private String httpBasicAuthPassword;

    public String getBaseUrl() {
        return baseUrl;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    public MediaType getAcceptType() {
        return acceptType;
    }

    public void setAcceptType(MediaType acceptType) {
        this.acceptType = acceptType;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public void setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public boolean isAcceptInvalidSslCertificate() {
        return acceptInvalidSslCertificate;
    }

    public void setAcceptInvalidSslCertificate(boolean acceptInvalidSslCertificate) {
        this.acceptInvalidSslCertificate = acceptInvalidSslCertificate;
    }

    public Integer getMaxInMemorySize() {
        return maxInMemorySize;
    }

    public void setMaxInMemorySize(Integer maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    public boolean isHttpBasicAuthEnabled() {
        return httpBasicAuthEnabled;
    }

    public void setHttpBasicAuthEnabled(boolean httpBasicAuthEnabled) {
        this.httpBasicAuthEnabled = httpBasicAuthEnabled;
    }

    public String getHttpBasicAuthUsername() {
        return httpBasicAuthUsername;
    }

    public void setHttpBasicAuthUsername(String httpBasicAuthUsername) {
        this.httpBasicAuthUsername = httpBasicAuthUsername;
    }

    public String getHttpBasicAuthPassword() {
        return httpBasicAuthPassword;
    }

    public void setHttpBasicAuthPassword(String httpBasicAuthPassword) {
        this.httpBasicAuthPassword = httpBasicAuthPassword;
    }
}
