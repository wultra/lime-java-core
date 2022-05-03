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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.time.Duration;
import java.util.Optional;

/**
 * REST client configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class RestClientConfiguration {

    /**
     * Constructor of REST client configuration.
     */
    public RestClientConfiguration() {
    }

    // Basic settings
    private String baseUrl;
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
    private Duration responseTimeout;

    // TLS certificate settings
    private boolean acceptInvalidSslCertificate = false;
    private Duration handshakeTimeout;

    // HTTP message settings
    private Integer maxInMemorySize = 1024 * 1024;

    // HTTP basic authentication
    private boolean httpBasicAuthEnabled = false;
    private String httpBasicAuthUsername;
    private String httpBasicAuthPassword;

    // TLS client certificate authentication
    private boolean certificateAuthEnabled = false;
    private boolean useCustomKeyStore = false;
    // Location uses Spring resource format
    private String keyStoreLocation;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;
    private boolean useCustomTrustStore = false;
    // Location uses Spring resource format
    private String trustStoreLocation;
    private String trustStorePassword;

    // Custom object mapper
    private ObjectMapper objectMapper;

    private HttpHeaders defaultHttpHeaders;

    // Custom filter
    private ExchangeFilterFunction filter;

    /**
     * Get base URL.
     * @return Base URL.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Set base URL.
     * @param baseUrl Base URL.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Get content type.
     * @return Content type.
     */
    public MediaType getContentType() {
        return contentType;
    }

    /**
     * Set content type.
     * @param contentType Content type.
     */
    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    /**
     * Get accept type.
     * @return Accept type.
     */
    public MediaType getAcceptType() {
        return acceptType;
    }

    /**
     * Set accept type.
     * @param acceptType Accepty type.
     */
    public void setAcceptType(MediaType acceptType) {
        this.acceptType = acceptType;
    }

    /**
     * Get whether proxy is enabled.
     * @return Whether proxy is enabled.
     */
    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    /**
     * Set whether proxy is enabled.
     * @param proxyEnabled Whether proxy is enabled.
     */
    public void setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    /**
     * Get proxy host.
     * @return Proxy host.
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Set proxy host.
     * @param proxyHost Proxy host.
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * Get proxy port.
     * @return Proxy port.
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Set proxy port.
     * @param proxyPort Proxy port.
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * Get proxy username.
     * @return Proxy username.
     */
    public String getProxyUsername() {
        return proxyUsername;
    }

    /**
     * Set proxy username.
     * @param proxyUsername Proxy username.
     */
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    /**
     * Get proxy password.
     * @return Proxy password.
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Set proxy password.
     * @param proxyPassword Proxy password.
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * Get connection timeout in milliseconds.
     *
     * @return Connection timeout.
     */
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Set connection timeout in milliseconds.
     *
     * @param connectionTimeout Connection timeout in milliseconds.
     */
    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Get whether invalid SSL certificate is accepted.
     * @return Whether invalid SSL certificate is accepted.
     */
    public boolean isAcceptInvalidSslCertificate() {
        return acceptInvalidSslCertificate;
    }

    /**
     * Set whether invalid SSL certificate is accepted.
     * @param acceptInvalidSslCertificate Whether invalid SSL certificate is accepted.
     */
    public void setAcceptInvalidSslCertificate(boolean acceptInvalidSslCertificate) {
        this.acceptInvalidSslCertificate = acceptInvalidSslCertificate;
    }

    /**
     * Get maximum in memory request size.
     * @return Maximum in memory request size.
     */
    public Integer getMaxInMemorySize() {
        return maxInMemorySize;
    }

    /**
     * Set maximum in memory request size.
     * @param maxInMemorySize Maximum in memory request size.
     */
    public void setMaxInMemorySize(Integer maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    /**
     * Get whether basic HTTP authentication is enabled.
     * @return Whether basic HTTP authentication is enabled.
     */
    public boolean isHttpBasicAuthEnabled() {
        return httpBasicAuthEnabled;
    }

    /**
     * Set whether basic HTTP authentication is enabled.
     * @param httpBasicAuthEnabled Whether basic HTTP authentication is enabled.
     */
    public void setHttpBasicAuthEnabled(boolean httpBasicAuthEnabled) {
        this.httpBasicAuthEnabled = httpBasicAuthEnabled;
    }

    /**
     * Get username for basic HTTP authentication.
     * @return Username for basic HTTP authentication.
     */
    public String getHttpBasicAuthUsername() {
        return httpBasicAuthUsername;
    }

    /**
     * Set username for basic HTTP authentication.
     * @param httpBasicAuthUsername Username for basic HTTP authentication.
     */
    public void setHttpBasicAuthUsername(String httpBasicAuthUsername) {
        this.httpBasicAuthUsername = httpBasicAuthUsername;
    }

    /**
     * Get password for basic HTTP authentication.
     * @return Password for basic HTTP authentication.
     */
    public String getHttpBasicAuthPassword() {
        return httpBasicAuthPassword;
    }

    /**
     * Set password for basic HTTP authentication.
     * @param httpBasicAuthPassword Password for basic HTTP authentication.
     */
    public void setHttpBasicAuthPassword(String httpBasicAuthPassword) {
        this.httpBasicAuthPassword = httpBasicAuthPassword;
    }

    /**
     * Get whether client TLS certificate authentication is enabled.
     * @return Whether client TLS certificate authentication is enabled.
     */
    public boolean isCertificateAuthEnabled() {
        return certificateAuthEnabled;
    }

    /**
     * Set whether client TLS certificate authentication is enabled.
     * @param certificateAuthEnabled Whether client TLS certificate authentication is enabled.
     */
    public void setCertificateAuthEnabled(boolean certificateAuthEnabled) {
        this.certificateAuthEnabled = certificateAuthEnabled;
    }

    /**
     * Get whether custom keystore is used for client TLS certificate authentication.
     * @return Whether custom keystore is used for client TLS certificate authentication.
     */
    public boolean useCustomKeyStore() {
        return useCustomKeyStore;
    }

    /**
     * Set whether custom keystore is used for client TLS certificate authentication.
     * @param useCustomKeyStore Whether custom keystore is used for client TLS certificate authentication.
     */
    public void setUseCustomKeyStore(boolean useCustomKeyStore) {
        this.useCustomKeyStore = useCustomKeyStore;
    }

    /**
     * Get keystore resource location for client TLS certificate authentication.
     * @return Keystore resource location for client TLS certificate authentication.
     */
    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    /**
     * Set keystore resource location for client TLS certificate authentication.
     * @param keyStoreLocation Keystore resource location for client TLS certificate authentication.
     */
    public void setKeyStoreLocation(String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    /**
     * Get keystore password for client TLS certificate authentication.
     * @return Keystore password for client TLS certificate authentication.
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * Set keystore password for client TLS certificate authentication.
     * @param keyStorePassword Keystore password for client TLS certificate authentication.
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Get private key alias for client TLS certificate authentication.
     * @return Private key alias for client TLS certificate authentication.
     */
    public String getKeyAlias() {
        return keyAlias;
    }

    /**
     * Set private key alias for client TLS certificate authentication.
     * @param keyAlias Private key alias for client TLS certificate authentication.
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    /**
     * Get private key password for client TLS certificate authentication.
     * @return Private key password for client TLS certificate authentication.
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * Set private key password for client TLS certificate authentication.
     * @param keyPassword Private key password for client TLS certificate authentication.
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * Get whether custom truststore is used for client TLS certificate authentication.
     * @return Whether custom truststore is used for client TLS certificate authentication.
     */
    public boolean useCustomTrustStore() {
        return useCustomTrustStore;
    }

    /**
     * Set whether custom truststore is used for client TLS certificate authentication.
     * @param useCustomTrustStore Whether custom truststore is used for client TLS certificate authentication.
     */
    public void setUseCustomTrustStore(boolean useCustomTrustStore) {
        this.useCustomTrustStore = useCustomTrustStore;
    }

    /**
     * Get truststore resource location for client TLS certificate authentication.
     * @return Truststore resource location for client TLS certificate authentication.
     */
    public String getTrustStoreLocation() {
        return trustStoreLocation;
    }

    /**
     * Set truststore resource location for client TLS certificate authentication.
     * @param trustStoreLocation Truststore resource location for client TLS certificate authentication.
     */
    public void setTrustStoreLocation(String trustStoreLocation) {
        this.trustStoreLocation = trustStoreLocation;
    }

    /**
     * Get truststore password for client TLS certificate authentication.
     * @return Truststore password for client TLS certificate authentication.
     */
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * Set truststore password for client TLS certificate authentication.
     * @param trustStorePassword Truststore password for client TLS certificate authentication.
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * Get the object mapper.
     * @return Object mapper.
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Set the object mapper.
     * @param objectMapper Object mapper.
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Get the exchange filter function.
     * @return Exchange filter function.
     */
    public ExchangeFilterFunction getFilter() {
        return filter;
    }

    /**
     * Set the exchange filter function.
     * @param filter Exchange filter function.
     */
    public void setFilter(ExchangeFilterFunction filter) {
        this.filter = filter;
    }

    /**
     * Get the default HTTP headers.
     * @return Default HTTP headers.
     */
    public HttpHeaders getDefaultHttpHeaders() {
        return defaultHttpHeaders;
    }

    /**
     * Set the default HTTP headers.
     * @param headers Default HTTP headers.
     */
    public void setDefaultHttpHeaders(HttpHeaders headers) {
        this.defaultHttpHeaders = headers;
    }

    /**
     * Get the SSL handshake timeout.
     *
     * @return timeout duration
     */
    public Duration getHandshakeTimeout() {
        return handshakeTimeout;
    }

    /**
     * Set the SSL handshake timeout. Default to 10000 ms.
     *
     * @param handshakeTimeout timeout duration
     */
    public void setHandshakeTimeout(Duration handshakeTimeout) {
        this.handshakeTimeout = handshakeTimeout;
    }

    /**
     * Get the maximum duration allowed between each network-level read operations (resolution: ms).
     *
     * @return response timeout
     */
    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    /**
     * Set the maximum duration allowed between each network-level read operations (resolution: ms).
     *
     * @param responseTimeout response timeout (resolution: ms)
     */
    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }
}
