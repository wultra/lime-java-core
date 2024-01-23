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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * REST client configuration.
 * This class is safe to use as {@code org.springframework.boot.context.properties.ConfigurationProperties}.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class RestClientConfiguration {

    // Basic settings
    private String baseUrl;
    private MediaType contentType = MediaType.APPLICATION_JSON;
    private MediaType acceptType = MediaType.APPLICATION_JSON;
    protected static final String TRACEPARENT_HEADER_KEY = "traceparent";

    // HTTP proxy settings
    private boolean proxyEnabled = false;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    // HTTP connection timeout
    private Duration connectionTimeout = Duration.ofMillis(5000);
    private Duration responseTimeout;

    private Duration maxIdleTime;
    private Duration maxLifeTime;

    private boolean keepAliveEnabled;
    private Duration keepAliveIdle;
    private Duration keepAliveInterval;
    private Integer keepAliveCount;

    // TLS certificate settings
    private boolean acceptInvalidSslCertificate = false;
    private Duration handshakeTimeout;

    // HTTP message settings
    private Integer maxInMemorySize = 1024 * 1024;

    // HTTP basic authentication
    private boolean httpBasicAuthEnabled = false;
    private String httpBasicAuthUsername;
    private String httpBasicAuthPassword;

    // HTTP Digest Authentication
    private boolean httpDigestAuthEnabled = false;
    private String httpDigestAuthUsername;
    private String httpDigestAuthPassword;

    // TLS client certificate authentication
    private boolean certificateAuthEnabled = false;
    private boolean useCustomKeyStore = false;
    private byte[] keyStoreBytes;
    // Location uses Spring resource format
    private String keyStoreLocation;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;
    private boolean useCustomTrustStore = false;
    private byte[] trustStoreBytes;
    // Location uses Spring resource format
    private String trustStoreLocation;
    private String trustStorePassword;

    private JacksonConfiguration jacksonConfiguration;

    // Custom default HTTP headers
    private HttpHeaders defaultHttpHeaders;

    // Custom filter
    private ExchangeFilterFunction filter;

    // Handling responses settings
    /**
     * Enables/disables auto-redirect of HTTP 30x statuses.
     */
    private boolean followRedirectEnabled = false;

    /**
     * Enables/disables simple one-line logging of HTTP requests and responses.
     */
    private boolean simpleLoggingEnabled = false;

    /**
     * Enables/disables usage of WARNING level in simple one-line logging.
     */
    private boolean logErrorResponsesAsWarnings = true;

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
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Set connection timeout.
     *
     * @param connectionTimeout Connection timeout as a Duration object.
     */
    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }


    /**
     * Get max idle time.
     *
     * @return Max idle time.
     */
    public Duration getMaxIdleTime() {
        return maxIdleTime;
    }

    /**
     * Set the options to use for configuring ConnectionProvider max idle time.
     * {@code Null} means no max idle time.
     *
     * @param maxIdleTime Max idle time.
     */
    public void setMaxIdleTime(Duration maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    /**
     * Get max life time.
     *
     * @return Max life time.
     */
    public Duration getMaxLifeTime() {
        return maxLifeTime;
    }

    /**
     * Set the options to use for configuring ConnectionProvider max life time.
     * {@code Null} means no max life time.
     *
     * @param maxLifeTime Max life time.
     */
    public void setMaxLifeTime(Duration maxLifeTime) {
        this.maxLifeTime = maxLifeTime;
    }

    /**
     * Return whether Keep-Alive is enabled.
     * @return {@code True} if keep-alive enabled-
     */
    public boolean isKeepAliveEnabled() {
        return keepAliveEnabled;
    }

    /**
     * Set whether Keep-Alive is enabled
     * @param keepAliveEnabled Keep-Alive.
     */
    public void setKeepAliveEnabled(boolean keepAliveEnabled) {
        this.keepAliveEnabled = keepAliveEnabled;
    }

    /**
     * Get Keep-Alive idle time.
     * @return Keep-Alive idle time.
     */
    public Duration getKeepAliveIdle() {
        return keepAliveIdle;
    }

    /**
     * Set Keep-Alive idle time.
     * @param keepAliveIdle Keep-Alive idle time.
     */
    public void setKeepAliveIdle(Duration keepAliveIdle) {
        this.keepAliveIdle = keepAliveIdle;
    }

    /**
     * Get Keep-Alive retransmission interval time.
     * @return Keep-Alive retransmission interval time.
     */
    public Duration getKeepAliveInterval() {
        return keepAliveInterval;
    }

    /**
     * Set Keep-Alive retransmission interval time.
     * @param keepAliveInterval Keep-Alive retransmission interval time.
     */
    public void setKeepAliveInterval(Duration keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    /**
     * Get Keep-Alive retransmission limit.
     * @return Keep-Alive retransmission limit.
     */
    public Integer getKeepAliveCount() {
        return keepAliveCount;
    }

    /**
     * Set Keep-Alive retransmission limit.
     * @param keepAliveCount Keep-Alive retransmission limit.
     */
    public void setKeepAliveCount(Integer keepAliveCount) {
        this.keepAliveCount = keepAliveCount;
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
     * Get whether digest HTTP authentication is enabled.
     * @return Whether digest HTTP authentication is enabled.
     */
    public boolean isHttpDigestAuthEnabled() {
        return httpDigestAuthEnabled;
    }

    /**
     * Set whether digest HTTP authentication is enabled.
     * @param httpDigestAuthEnabled Whether digest HTTP authentication is enabled.
     */
    public void setHttpDigestAuthEnabled(boolean httpDigestAuthEnabled) {
        this.httpDigestAuthEnabled = httpDigestAuthEnabled;
    }

    /**
     * Get username for digest HTTP authentication.
     * @return Username for digest HTTP authentication.
     */
    public String getHttpDigestAuthUsername() {
        return httpDigestAuthUsername;
    }

    /**
     * Set username for digest HTTP authentication.
     * @param httpDigestAuthUsername Username for digest HTTP authentication.
     */
    public void setHttpDigestAuthUsername(String httpDigestAuthUsername) {
        this.httpDigestAuthUsername = httpDigestAuthUsername;
    }

    /**
     * Get password for digest HTTP authentication.
     * @return Password for digest HTTP authentication.
     */
    public String getHttpDigestAuthPassword() {
        return httpDigestAuthPassword;
    }

    /**
     * Set password for digest HTTP authentication.
     * @param httpDigestAuthPassword Password for digest HTTP authentication.
     */
    public void setHttpDigestAuthPassword(String httpDigestAuthPassword) {
        this.httpDigestAuthPassword = httpDigestAuthPassword;
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
     * Get byte data with the key store.
     * @return Byte data with the key store.
     */
    @Nullable
    public byte[] getKeyStoreBytes() {
        if (keyStoreBytes == null) {
            return null;
        }
        return Arrays.copyOf(keyStoreBytes, keyStoreBytes.length);
    }

    /**
     * Set byte data with the key store.
     * @param keyStoreBytes Byte data with the key store.
     */
    public void setKeyStoreBytes(byte[] keyStoreBytes) {
        this.keyStoreBytes = Arrays.copyOf(keyStoreBytes, keyStoreBytes.length);
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
     * Get byte data with the trust store.
     * @return Byte data with the trust store
     */
    @Nullable
    public byte[] getTrustStoreBytes() {
        if (trustStoreBytes == null) {
            return null;
        }
        return Arrays.copyOf(trustStoreBytes, trustStoreBytes.length);
    }

    /**
     * Set byte data with the trust store.
     * @param trustStoreBytes Byte data with the trust store.
     */
    public void setTrustStoreBytes(byte[] trustStoreBytes) {
        this.trustStoreBytes = Arrays.copyOf(trustStoreBytes, trustStoreBytes.length);
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
     * Get the jackson configuration.
     * @return Object mapper.
     */
    public JacksonConfiguration getJacksonConfiguration() {
        return jacksonConfiguration;
    }

    /**
     * Set the jackson configuration.
     * @param jacksonConfiguration jacksonConfiguration.
     */
    public void setJacksonConfiguration(JacksonConfiguration jacksonConfiguration) {
        this.jacksonConfiguration = jacksonConfiguration;
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

    /**
     * Get whether following of redirections is enabled.
     * @return Whether following of redirections is enabled.
     */
    public boolean isFollowRedirectEnabled() {
        return followRedirectEnabled;
    }

    /**
     * Sets whether following of redirections is enabled
     *
     * @param followRedirectEnabled Whether following of redirections is enabled
     */
    public void setFollowRedirectEnabled(boolean followRedirectEnabled) {
        this.followRedirectEnabled = followRedirectEnabled;
    }

    /**
     * Get whether simple one-line logging of HTTP requests and responses is enabled.
     * @return Whether simple logging is enabled.
     */
    public boolean isSimpleLoggingEnabled() {
        return simpleLoggingEnabled;
    }

    /**
     * Set whether simple one-line logging of HTTP requests and responses is enabled.
     * @param simpleLoggingEnabled Whether simple logging is enabled.
     */
    public void setSimpleLoggingEnabled(boolean simpleLoggingEnabled) {
        this.simpleLoggingEnabled = simpleLoggingEnabled;
    }

    /**
     * Get whether error HTTP responses are logged as warnings.
     * @return Whether error HTTP responses are logged as warnings.
     */
    public boolean isLogErrorResponsesAsWarnings() {
        return logErrorResponsesAsWarnings;
    }

    /**
     * Set whether error HTTP responses are logged as warnings.
     * @param logErrorResponsesAsWarnings Whether error HTTP responses are logged as warnings.
     */
    public void setLogErrorResponsesAsWarnings(boolean logErrorResponsesAsWarnings) {
        this.logErrorResponsesAsWarnings = logErrorResponsesAsWarnings;
    }

    @Getter
    @Setter
    public static class JacksonConfiguration {
        /**
         * Jackson on/off features that affect the way Java objects are serialized.
         */
        private final Map<SerializationFeature, Boolean> serialization = new EnumMap<>(SerializationFeature.class);

        /**
         * Jackson on/off features that affect the way Java objects are deserialized.
         */
        private final Map<DeserializationFeature, Boolean> deserialization = new EnumMap<>(DeserializationFeature.class);
    }
}
