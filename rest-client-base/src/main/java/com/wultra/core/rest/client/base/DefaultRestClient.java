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

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.wultra.core.rest.client.base.util.SslUtils;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import jdk.net.ExtendedSocketOptions;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.SslProvider;
import reactor.netty.transport.ProxyProvider;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

/**
 * Default REST client implementation based on WebClient.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class DefaultRestClient implements RestClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRestClient.class);

    private WebClient webClient;
    private final RestClientConfiguration config;
    private final Collection<Module> modules;

    /**
     * Construct default REST client without any additional configuration.
     * @param baseUrl Base URL.
     * @throws RestClientException Thrown in case client initialization fails.
     */
    public DefaultRestClient(String baseUrl) throws RestClientException {
        this.config = new RestClientConfiguration();
        this.config.setBaseUrl(baseUrl);
        this.modules = Collections.emptyList();
        // Use default WebClient settings
        initializeWebClient();
    }

    /**
     * Construct default REST client with specified configuration.
     * @param config REST client configuration.
     * @param modules jackson modules
     * @throws RestClientException Thrown in case client initialization fails.
     */
    public DefaultRestClient(final RestClientConfiguration config, final Module... modules) throws RestClientException {
        // Use WebClient configuration from the config constructor parameter
        this.config = config;
        this.modules = modules == null ? Collections.emptyList() : Arrays.asList(modules);
        initializeWebClient();
    }

    /**
     * Private constructor for builder.
     * @param builder REST client builder.
     * @throws RestClientException Thrown in case client initialization fails.
     */
    private DefaultRestClient(final Builder builder) throws RestClientException {
        // Use WebClient settings from the builder
        this.config = builder.config;
        this.modules = builder.modules;
        initializeWebClient();
    }

    /**
     * Initialize WebClient instance and configure it based on client configuration.
     */
    private void initializeWebClient() throws RestClientException {
        validateConfiguration(config);
        if (config.getBaseUrl() != null) {
            try {
                new URI(config.getBaseUrl());
            } catch (URISyntaxException ex) {
                throw new RestClientException("Invalid parameter baseUrl");
            }
        }
        final WebClient.Builder builder = WebClient.builder();
        final SslContext sslContext = SslUtils.prepareSslContext(config);
        HttpClient httpClient = createHttpClient(config)
                .wiretap(this.getClass().getCanonicalName(), LogLevel.TRACE, AdvancedByteBufFormat.TEXTUAL)
                .followRedirect(config.isFollowRedirectEnabled());
        if (sslContext != null) {
            httpClient = httpClient.secure(sslContextSpec -> {
                final SslProvider.Builder sslProviderBuilder = sslContextSpec.sslContext(sslContext);
                final Duration handshakeTimeout = config.getHandshakeTimeout();
                if (handshakeTimeout != null) {
                    logger.debug("Setting handshake timeout {}", handshakeTimeout);
                    sslProviderBuilder.handshakeTimeout(handshakeTimeout);
                }
            });
        }
        if (config.getConnectionTimeout() != null) {
            httpClient = httpClient.option(
                    ChannelOption.CONNECT_TIMEOUT_MILLIS,
                    Math.toIntExact(config.getConnectionTimeout().toMillis()));
        }
        if (config.isKeepAliveEnabled()) {
            httpClient = configureKeepAlive(httpClient, config);
        }

        final Duration responseTimeout = config.getResponseTimeout();
        if (responseTimeout != null) {
            logger.debug("Setting response timeout {}", responseTimeout);
            httpClient = httpClient.responseTimeout(responseTimeout);
        }
        if (config.isProxyEnabled()) {
            httpClient = httpClient.proxy(proxySpec -> {
                ProxyProvider.Builder proxyBuilder = proxySpec
                        .type(ProxyProvider.Proxy.HTTP)
                        .host(config.getProxyHost())
                        .port(config.getProxyPort());
                if (config.getProxyUsername() != null && !config.getProxyUsername().isEmpty()) {
                    proxyBuilder.username(config.getProxyUsername());
                    proxyBuilder.password(s -> config.getProxyPassword());
                }
                proxyBuilder.build();
            });
        }

        final Optional<ObjectMapper> objectMapperOptional = createObjectMapper(config, modules);
        final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    ClientCodecConfigurer.ClientDefaultCodecs defaultCodecs = configurer.defaultCodecs();
                    objectMapperOptional.ifPresent(objectMapper -> {
                        defaultCodecs.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                        defaultCodecs.jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                    });
                    defaultCodecs.maxInMemorySize(config.getMaxInMemorySize());
                })
                .build();
        builder.exchangeStrategies(exchangeStrategies);

        if (config.isHttpBasicAuthEnabled()) {
            if (config.getHttpBasicAuthUsername() != null && config.getHttpBasicAuthPassword() != null) {
                logger.info("Configuring HTTP Basic Authentication");
                builder.filter(ExchangeFilterFunctions
                        .basicAuthentication(config.getHttpBasicAuthUsername(), config.getHttpBasicAuthPassword()));
            } else {
                logger.warn("HTTP Basic Authentication is enabled but username or password is null, baseUrl: {}", config.getBaseUrl());
            }
        }
        if (config.isHttpDigestAuthEnabled() && config.getHttpDigestAuthUsername() != null) {
            logger.info("Configuring HTTP Digest Authentication");
            builder.filter(DigestAuthenticationFilterFunction
                    .digestAuthentication(config.getHttpDigestAuthUsername(), config.getHttpDigestAuthPassword()));
        }
        if (config.getFilter() != null) {
            builder.filter(config.getFilter());
        }
        if (config.getDefaultHttpHeaders() != null) {
            builder.defaultHeaders(httpHeaders -> httpHeaders.addAll(config.getDefaultHttpHeaders()));
        }
        if (config.isSimpleLoggingEnabled()) {
            builder.filter((request, next) -> {
                final String requestLogMessage = "RestClient " + request.method() + " " + request.url();
                return next.exchange(request)
                        .doOnNext(response -> {
                            final HttpStatusCode statusCode = response.statusCode();
                            if (config.isLogErrorResponsesAsWarnings() && statusCode.isError()) {
                                logger.warn("{}: {}", requestLogMessage, statusCode);
                            } else {
                                logger.info("{}: {}", requestLogMessage, statusCode);
                            }
                        });
            });
        }

        final ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        webClient = builder.baseUrl(config.getBaseUrl()).clientConnector(connector).build();
    }

    private static HttpClient configureKeepAlive(final HttpClient httpClient, final RestClientConfiguration config) throws RestClientException {
        final Duration keepAliveIdle = config.getKeepAliveIdle();
        final Duration keepAliveInterval = config.getKeepAliveInterval();
        final Integer keepAliveCount = config.getKeepAliveCount();
        logger.info("Configuring Keep-Alive, idle={}, interval={}, count={}", keepAliveIdle, keepAliveInterval, keepAliveCount);
        if (keepAliveIdle == null || keepAliveInterval == null || keepAliveCount == null) {
            throw new RestClientException("All Keep-Alive properties must be specified.");
        }

        final int keepIdleSeconds = Math.toIntExact(keepAliveIdle.toSeconds());
        final int keepIntervalSeconds = Math.toIntExact(keepAliveInterval.toSeconds());

        return httpClient.option(ChannelOption.SO_KEEPALIVE, true)
                .option(NioChannelOption.of(ExtendedSocketOptions.TCP_KEEPIDLE), keepIdleSeconds)
                .option(NioChannelOption.of(ExtendedSocketOptions.TCP_KEEPINTERVAL), keepIntervalSeconds)
                .option(NioChannelOption.of(ExtendedSocketOptions.TCP_KEEPCOUNT), keepAliveCount)
                .option(EpollChannelOption.TCP_KEEPIDLE, keepIdleSeconds)
                .option(EpollChannelOption.TCP_KEEPINTVL, keepIntervalSeconds)
                .option(EpollChannelOption.TCP_KEEPCNT, keepAliveCount);
    }

    /**
     * Create HttpClient with default HttpConnectionProvider or custom one, if specified in the given config.
     * @param config Config to create connection provider if specified.
     * @return Http client.
     */
    private static HttpClient createHttpClient(final RestClientConfiguration config) {
        final Duration maxIdleTime = config.getMaxIdleTime();
        final Duration maxLifeTime = config.getMaxLifeTime();
        if (maxIdleTime != null || maxLifeTime != null) {
            logger.info("Configuring custom connection provider, maxIdleTime={}, maxLifeTime={}", maxIdleTime, maxLifeTime);
            final ConnectionProvider.Builder providerBuilder = ConnectionProvider.builder("custom");
            if (maxIdleTime != null) {
                providerBuilder.maxIdleTime(maxIdleTime);
            }
            if (maxLifeTime != null) {
                providerBuilder.maxLifeTime(maxLifeTime);
            }
            return HttpClient.create(providerBuilder.build());
        } else {
            return HttpClient.create();
        }
    }

    private static Optional<ObjectMapper> createObjectMapper(final RestClientConfiguration config, Collection<Module> modules) {
        final RestClientConfiguration.JacksonConfiguration jacksonConfiguration = config.getJacksonConfiguration();
        if (jacksonConfiguration == null && modules.isEmpty()) {
            return Optional.empty();
        }

        logger.debug("Configuring object mapper");
        final ObjectMapper objectMapper = new ObjectMapper();
        if (jacksonConfiguration != null) {
            jacksonConfiguration.getDeserialization().forEach(objectMapper::configure);
            jacksonConfiguration.getSerialization().forEach(objectMapper::configure);
        }
        objectMapper.registerModules(modules);

        return Optional.of(objectMapper);
    }

    private static void validateConfiguration(final RestClientConfiguration config) throws RestClientException {
        if (config.isHttpBasicAuthEnabled() && config.isHttpDigestAuthEnabled()) {
            throw new RestClientException("Both HTTP Basic and Digest authentication is enabled");
        }
    }

    @Override
    public <T> ResponseEntity<T> get(String path, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return get(path, null, null, responseType);
    }

    @Override
    public <T> ResponseEntity<T> get(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException {
        try {
            return buildUri(webClient.get(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .block();
        } catch (Exception ex) {
            if (ex.getCause() instanceof RestClientException) {
                // Throw exceptions created by REST client
                throw (RestClientException) ex.getCause();
            }
            throw new RestClientException("HTTP GET request failed", ex);
        }
    }

    @Override
    public <T> void getNonBlocking(String path, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        getNonBlocking(path, null, null, responseType, onSuccess, onError);
    }

    @Override
    public <T> void getNonBlocking(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        try {
            buildUri(webClient.get(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .accept(config.getAcceptType())
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .subscribe(onSuccess, onError);
        } catch (Exception ex) {
            throw new RestClientException("HTTP GET request failed", ex);
        }
    }

    @Override
    public Response getObject(String path) throws RestClientException {
        return get(path, new ParameterizedTypeReference<Response>(){}).getBody();
    }

    @Override
    public Response getObject(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers) throws RestClientException {
        return get(path, queryParams, headers, new ParameterizedTypeReference<Response>(){}).getBody();
    }

    @Override
    public <T> ObjectResponse<T> getObject(String path, Class<T> responseType) throws RestClientException {
        return getObject(path, null, null, responseType);
    }

    @Override
    public <T> ObjectResponse<T> getObject(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException {
        ParameterizedTypeReference<ObjectResponse<T>> typeReference = getTypeReference(responseType);
        ResponseEntity<ObjectResponse<T>> responseEntity = get(path, queryParams, headers, typeReference);
        return responseEntity.getBody();
    }

    @Override
    public <T> ResponseEntity<T> post(String path, Object request, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return post(path, request, null, null, responseType);
    }

    @Override
    public <T> ResponseEntity<T> post(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException {
        try {
            WebClient.RequestBodySpec spec = buildUri(webClient.post(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(resolveContentType(config, headers))
                    .accept(config.getAcceptType());
            return buildRequest(spec, request)
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .block();
        } catch (Exception ex) {
            if (ex.getCause() instanceof RestClientException) {
                // Throw exceptions created by REST client
                throw (RestClientException) ex.getCause();
            }

            if (ex instanceof DataBufferLimitException) {
                // Log error for large server response for closer inspection
                logger.warn("Error while retrieving large server response", ex);
            }


            throw new RestClientException("HTTP POST request failed", ex);
        }
    }

    @Override
    public <T> void postNonBlocking(String path, Object request,  ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        postNonBlocking(path, request, null, null, responseType, onSuccess, onError);
    }

    @Override
    public <T> void postNonBlocking(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers,  ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        try {
            WebClient.RequestBodySpec spec = buildUri(webClient.post(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(resolveContentType(config, headers))
                    .accept(config.getAcceptType());
            buildRequest(spec, request)
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .subscribe(onSuccess, onError);
        } catch (Exception ex) {
            throw new RestClientException("HTTP POST request failed", ex);
        }
    }

    @Override
    public Response postObject(String path, ObjectRequest<?> objectRequest) throws RestClientException {
        return post(path, objectRequest, new ParameterizedTypeReference<Response>(){}).getBody();
    }

    @Override
    public Response postObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers) throws RestClientException {
        return post(path, objectRequest, queryParams, headers, new ParameterizedTypeReference<Response>(){}).getBody();
    }

    @Override
    public <T> ObjectResponse<T> postObject(String path, ObjectRequest<?> objectRequest, Class<T> responseType) throws RestClientException {
        return postObject(path, objectRequest, null, null, responseType);
    }

    @Override
    public <T> ObjectResponse<T> postObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException {
        ParameterizedTypeReference<ObjectResponse<T>> typeReference = getTypeReference(responseType);
        ResponseEntity<ObjectResponse<T>> responseEntity = post(path, objectRequest, queryParams, headers, typeReference);
        return responseEntity.getBody();
    }

    @Override
    public <T> ResponseEntity<T> put(String path, Object request, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return put(path, request, null, null, responseType);
    }

    @Override
    public <T> ResponseEntity<T> put(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException {
        try {
            WebClient.RequestBodySpec spec = buildUri(webClient.put(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(resolveContentType(config, headers))
                    .accept(config.getAcceptType());
            return buildRequest(spec, request)
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .block();
        } catch (Exception ex) {
            if (ex.getCause() instanceof RestClientException) {
                // Throw exceptions created by REST client
                throw (RestClientException) ex.getCause();
            }
            throw new RestClientException("HTTP PUT request failed", ex);
        }
    }

    @Override
    public <T> void putNonBlocking(String path, Object request,  ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        putNonBlocking(path, request, null, null, responseType, onSuccess, onError);
    }

    @Override
    public <T> void putNonBlocking(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers,  ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        try {
            WebClient.RequestBodySpec spec = buildUri(webClient.put(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(resolveContentType(config, headers))
                    .accept(config.getAcceptType());
            buildRequest(spec, request)
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .subscribe(onSuccess, onError);
        } catch (Exception ex) {
            throw new RestClientException("HTTP PUT request failed", ex);
        }
    }

    @Override
    public Response putObject(String path, ObjectRequest<?> objectRequest) throws RestClientException {
        return put(path, objectRequest, new ParameterizedTypeReference<Response>(){}).getBody();
    }

    @Override
    public Response putObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers) throws RestClientException {
        return put(path, objectRequest, queryParams, headers, new ParameterizedTypeReference<Response>(){}).getBody();
    }

    @Override
    public <T> ObjectResponse<T> putObject(String path, ObjectRequest<?> objectRequest, Class<T> responseType) throws RestClientException {
        return putObject(path, objectRequest, null, null ,responseType);
    }

    @Override
    public <T> ObjectResponse<T> putObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException {
        ParameterizedTypeReference<ObjectResponse<T>> typeReference = getTypeReference(responseType);
        ResponseEntity<ObjectResponse<T>> responseEntity = put(path, objectRequest, queryParams, headers, typeReference);
        return responseEntity.getBody();
    }

    @Override
    public <T> ResponseEntity<T> delete(String path, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return delete(path, null, null, responseType);
    }

    @Override
    public <T> ResponseEntity<T> delete(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException {
        try {
            return buildUri(webClient.delete(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .block();
        } catch (Exception ex) {
            if (ex.getCause() instanceof RestClientException) {
                // Throw exceptions created by REST client
                throw (RestClientException) ex.getCause();
            }
            throw new RestClientException("HTTP DELETE request failed", ex);
        }
    }

    @Override
    public <T> void deleteNonBlocking(String path,  ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        deleteNonBlocking(path, null, null, responseType, onSuccess, onError);
    }

    @Override
    public <T> void deleteNonBlocking(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        try {
            buildUri(webClient.delete(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .accept(config.getAcceptType())
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .subscribe(onSuccess, onError);
        } catch (Exception ex) {
            throw new RestClientException("HTTP DELETE request failed", ex);
        }
    }

    @Override
    public Response deleteObject(String path) throws RestClientException {
        return delete(path, new ParameterizedTypeReference<Response>(){}).getBody();
    }

    @Override
    public Response deleteObject(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers) throws RestClientException {
        return delete(path, queryParams, headers, new ParameterizedTypeReference<Response>(){}).getBody();
    }

    @Override
    public <T> ObjectResponse<T> deleteObject(String path, Class<T> responseType) throws RestClientException {
        return deleteObject(path, null, null, responseType);
    }

    @Override
    public <T> ObjectResponse<T> deleteObject(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException {
        ResponseEntity<ObjectResponse<T>> responseEntity = delete(path, queryParams, headers, getTypeReference(responseType));
        return responseEntity.getBody();
    }

    @Override
    public <T> ResponseEntity<T> patch(String path, Object request, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return patch(path, request, null, null, responseType);
    }

    @Override
    public <T> ResponseEntity<T> patch(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException {
        try {
            WebClient.RequestBodySpec spec = buildUri(webClient.patch(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(resolveContentType(config, headers))
                    .accept(config.getAcceptType());
            return buildRequest(spec, request)
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .block();
        } catch (Exception ex) {
            if (ex.getCause() instanceof RestClientException) {
                // Throw exceptions created by REST client
                throw (RestClientException) ex.getCause();
            }
            throw new RestClientException("HTTP PATCH request failed", ex);
        }
    }

    @Override
    public <T> void patchNonBlocking(String path, Object request,  ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        patchNonBlocking(path, request, null, null, responseType, onSuccess, onError);
    }

    @Override
    public <T> void patchNonBlocking(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers,  ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        try {
            WebClient.RequestBodySpec spec = buildUri(webClient.patch(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(resolveContentType(config, headers))
                    .accept(config.getAcceptType());
            buildRequest(spec, request)
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .subscribe(onSuccess, onError);
        } catch (Exception ex) {
            throw new RestClientException("HTTP PATCH request failed", ex);
        }
    }

    @Override
    public Response patchObject(String path, ObjectRequest<?> objectRequest) throws RestClientException {
        return patch(path, objectRequest, new ParameterizedTypeReference<Response>(){}).getBody();
    }

    @Override
    public Response patchObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers) throws RestClientException {
        return patch(path, objectRequest, queryParams, headers, new ParameterizedTypeReference<Response>(){}).getBody();
    }

    @Override
    public <T> ObjectResponse<T> patchObject(String path, ObjectRequest<?> objectRequest, Class<T> responseType) throws RestClientException {
        return patchObject(path, objectRequest, null, null ,responseType);
    }

    @Override
    public <T> ObjectResponse<T> patchObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException {
        ParameterizedTypeReference<ObjectResponse<T>> typeReference = getTypeReference(responseType);
        ResponseEntity<ObjectResponse<T>> responseEntity = patch(path, objectRequest, queryParams, headers, typeReference);
        return responseEntity.getBody();
    }

    @Override
    public <T> ResponseEntity<T> head(String path, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return head(path, null, null, responseType);
    }

    @Override
    public <T> ResponseEntity<T> head(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException {
        try {
            return buildUri(webClient.head(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .defaultIfEmpty(new ResponseEntity<>(HttpStatus.ACCEPTED))
                    .block();
        } catch (Exception ex) {
            if (ex.getCause() instanceof RestClientException) {
                // Throw exceptions created by REST client
                throw (RestClientException) ex.getCause();
            }
            throw new RestClientException("HTTP HEAD request failed", ex);
        }
    }

    @Override
    public <T> void headNonBlocking(String path, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        headNonBlocking(path, null, null, responseType, onSuccess, onError);
    }

    @Override
    public <T> void headNonBlocking(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        try {
            buildUri(webClient.head(), path, queryParams)
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .accept(config.getAcceptType())
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .subscribe(onSuccess, onError);
        } catch (Exception ex) {
            throw new RestClientException("HTTP HEAD request failed", ex);
        }
    }

    @Override
    public Response headObject(String path) throws RestClientException {
        head(path, new ParameterizedTypeReference<Response>(){});
        return new Response();
    }

    @Override
    public Response headObject(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers) throws RestClientException {
        head(path, queryParams, headers, new ParameterizedTypeReference<Response>(){});
        return new Response();
    }

    @Override
    public <T> ObjectResponse<T> headObject(String path, Class<T> responseType) throws RestClientException {
        return headObject(path, null, null, responseType);
    }

    @Override
    public <T> ObjectResponse<T> headObject(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException {
        ParameterizedTypeReference<ObjectResponse<T>> typeReference = getTypeReference(responseType);
        head(path, queryParams, headers, typeReference);
        return new ObjectResponse<>();
    }

    /**
     * Convert response type to parameterized type reference of ObjectResponse.
     * @param responseType Object response type.
     * @param <T> Response type.
     * @return Parameterized type reference of ObjectResponse.
     */
    private <T> ParameterizedTypeReference<ObjectResponse<T>> getTypeReference(Class<T> responseType) {
        return new ParameterizedTypeReference<>() {
            @Override
            public Type getType() {
                return TypeFactory.defaultInstance().constructParametricType(ObjectResponse.class, responseType);
            }
        };
    }

    /**
     * Handle response using non-blocking calls.
     * @param response Client response.
     * @param responseType Expected response type.
     * @return Mono with response entity or error.
     */
    private <T> Mono<ResponseEntity<T>> handleResponse(ClientResponse response, ParameterizedTypeReference<T> responseType) {
        if (!response.statusCode().isError()) {
            // OK response
            return response.toEntity(responseType);
        }
        // Error handling
        return response.toEntity(String.class).flatMap(rawResponseEntity -> {
            String rawResponse = null;
            HttpHeaders rawResponseHeaders = null;
            if (rawResponseEntity != null) {
                rawResponse = rawResponseEntity.getBody();
                rawResponseHeaders = rawResponseEntity.getHeaders();
            }
            // Try to parse ErrorResponse in case expected response type is ObjectResponse
            Class<?> clazz = TypeFactory.rawClass(responseType.getType());
            if (clazz.isAssignableFrom(ObjectResponse.class)) {
                try {
                    // Use an ObjectMapper to deserialize the error response
                    ObjectMapper objectMapper = new ObjectMapper();
                    ErrorResponse errorResponse = objectMapper.readValue(rawResponse, ErrorResponse.class);
                    if (errorResponse != null) {
                        return Mono.error(new RestClientException("HTTP error occurred: " + response.statusCode(), response.statusCode(), rawResponse, rawResponseHeaders, errorResponse));
                    }
                } catch (IOException ex) {
                    // Exception is handled silently, ErrorResponse is not available, use a regular error with raw response
                }
            }
            return Mono.error(new RestClientException("HTTP error occurred: " + response.statusCode(), response.statusCode(), rawResponse, rawResponseHeaders));
        });
    }

    private static MediaType resolveContentType(final RestClientConfiguration config, final MultiValueMap<String, String> headers) {
        if (headers != null && headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            final MediaType contentType = MediaType.valueOf(headers.getFirst(HttpHeaders.CONTENT_TYPE));
            logger.debug("Overriding content type {} from config by {} from the given headers", config.getContentType(), contentType);
            return contentType;
        }
        return config.getContentType();
    }

    /**
     * In case base URL is specified, append the path to complete the URL. Otherwise use the path as the URL specification.
     * @param uriSpec Request headers URI specification.
     * @param path URI path.
     * @param queryParams Query parameters.
     * @return Request header specification.
     */
    private WebClient.RequestHeadersSpec<?> buildUri(WebClient.RequestHeadersUriSpec<?> uriSpec, String path, MultiValueMap<String, String> queryParams) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (queryParams != null) {
            params.addAll(queryParams);
        }
        if (config.getBaseUrl() == null) {
            return uriSpec.uri(path, params);
        } else {
            return uriSpec.uri(uriBuilder -> uriBuilder.path(path).queryParams(params).build());
        }
    }

    /**
     * In case base URL is specified, append the path to complete the URL. Otherwise use the path as the URL specification.
     * @param uriSpec Request headers URI specification.
     * @param path URI path.
     * @param queryParams Query parameters.
     * @return Request header specification.
     */
    private WebClient.RequestBodySpec buildUri(WebClient.RequestBodyUriSpec uriSpec, String path, MultiValueMap<String, String> queryParams) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (queryParams != null) {
            params.addAll(queryParams);
        }
        if (config.getBaseUrl() == null) {
            return uriSpec.uri(path, params);
        } else {
            return uriSpec.uri(uriBuilder -> uriBuilder.path(path).queryParams(params).build());
        }
    }

    /**
     * Build request for various request types.
     * @param requestSpec Request body specification.
     * @param request Request data.
     * @return Updated header specification.
     */
    @SuppressWarnings("unchecked")
    private WebClient.RequestHeadersSpec<?> buildRequest(WebClient.RequestBodySpec requestSpec, Object request) {
        if (request != null) {
            if (request instanceof MultiValueMap) {
                return requestSpec.body(BodyInserters.fromMultipartData(((MultiValueMap<String, ?>) request)));
            } else if (request instanceof Publisher) {
                return requestSpec.body(BodyInserters.fromDataBuffers((Publisher<DataBuffer>) request));
            }
            return requestSpec.body(BodyInserters.fromValue(request));
        } else {
            return requestSpec;
        }
    }

    /**
     * Construct a new rest client builder.
     * @return Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder used for configuration of the default rest client.
     */
    public static class Builder {

        private final RestClientConfiguration config;

        private final Collection<Module> modules;

        /**
         * Construct new builder with given base URL.
         */
        public Builder() {
            config = new RestClientConfiguration();
            modules = new HashSet<>();
        }

        /**
         * Get the built default rest client instance.
         * @return Default rest client instance.
         * @throws RestClientException Thrown in case rest client initialization fails.
         */
        public DefaultRestClient build() throws RestClientException {
            return new DefaultRestClient(this);
        }

        /**
         * Set base URL.
         * @param baseUrl Base URL.
         * @return Builder.
         */
        public Builder baseUrl(String baseUrl) {
            config.setBaseUrl(baseUrl);
            return this;
        }

        /**
         * Configure content type.
         * @param contentType Content type.
         * @return Builder.
         */
        public Builder contentType(MediaType contentType) {
            config.setContentType(contentType);
            return this;
        }

        /**
         * Configure the accept type.
         * @param acceptType Accept type.
         * @return Builder.
         */
        public Builder acceptType(MediaType acceptType) {
            config.setAcceptType(acceptType);
            return this;
        }

        /**
         * Configure proxy.
         * @return ProxyBuilder.
         */
        public ProxyBuilder proxy() {
            config.setProxyEnabled(true);
            return new ProxyBuilder(this);
        }

        /**
         * Configure connection timeout.
         * @param connectionTimeout Connection timeout.
         * @return Builder.
         */
        public Builder connectionTimeout(Duration connectionTimeout) {
            config.setConnectionTimeout(connectionTimeout);
            return this;
        }


        /**
         * Configure response timeout. {@code Null} means no response timeout.
         * @param responseTimeout Response timeout.
         * @return Builder.
         */
        public Builder responseTimeout(final Duration responseTimeout) {
            config.setResponseTimeout(responseTimeout);
            return this;
        }

        /**
         * Configure ConnectionProvider max idle time. {@code Null} means no max idle time.
         * @param maxIdleTime Max idle time.
         * @return Builder.
         */
        public Builder maxIdleTime(final Duration maxIdleTime) {
            config.setMaxIdleTime(maxIdleTime);
            return this;
        }

        /**
         * Configure ConnectionProvider max life time. {@code Null} means no max life time.
         * @param maxLifeTime Max life time.
         * @return Builder.
         */
        public Builder maxLifeTime(Duration maxLifeTime) {
            config.setMaxLifeTime(maxLifeTime);
            return this;
        }

        /**
         * Configure Keep-Alive probe.
         * @param keepAliveEnabled Keep-Alive enabled.
         * @return Builder.
         */
        public Builder keepAliveEnabled(boolean keepAliveEnabled) {
            config.setKeepAliveEnabled(keepAliveEnabled);
            return this;
        }

        /**
         * Configure Keep-Alive idle interval.
         * @param keepAliveIdle Keep-Alive idle interval.
         * @return Builder.
         */
        public Builder keepAliveIdle(Duration keepAliveIdle) {
            config.setKeepAliveIdle(keepAliveIdle);
            return this;
        }

        /**
         * Configure Keep-Alive retransmission interval.
         * @param keepAliveInterval Keep-Alive retransmission interval.
         * @return Builder.
         */
        public Builder keepAliveInterval(Duration keepAliveInterval) {
            config.setKeepAliveInterval(keepAliveInterval);
            return this;
        }

        /**
         * Configure Keep-Alive retransmission limit.
         * @param keepAliveCount Keep-Alive retransmission limit.
         * @return Builder.
         */
        public Builder keepAliveCount(Integer keepAliveCount) {
            config.setKeepAliveCount(keepAliveCount);
            return this;
        }

        /**
         * Configure whether invalid SSL certificate is accepted.
         * @param acceptInvalidSslCertificate Whether invalid SSL certificate is accepted.
         * @return Builder.
         */
        public Builder acceptInvalidCertificate(boolean acceptInvalidSslCertificate) {
            config.setAcceptInvalidSslCertificate(acceptInvalidSslCertificate);
            return this;
        }

        /**
         * Configure maximum in memory request size.
         * @param maxInMemorySize Maximum in memory request size.
         * @return Builder.
         */
        public Builder maxInMemorySize(Integer maxInMemorySize) {
            config.setMaxInMemorySize(maxInMemorySize);
            return this;
        }

        /**
         * Configure HTTP basic authentication.
         * @return Builder.
         */
        public HttpBasicAuthBuilder httpBasicAuth() {
            config.setHttpBasicAuthEnabled(true);
            return new HttpBasicAuthBuilder(this);
        }

        /**
         * Configure HTTP digest authentication.
         *
         * @return Builder.
         */
        public HttpDigestAuthBuilder httpDigestAuth() {
            config.setHttpDigestAuthEnabled(true);
            return new HttpDigestAuthBuilder(this);
        }

        /**
         * Configure certificate authentication.
         * @return Builder.
         */
        public CertificateAuthBuilder certificateAuth() {
            config.setCertificateAuthEnabled(true);
            return new CertificateAuthBuilder(this);
        }

        /**
         * Configure default HTTP headers.
         * @param defaultHttpHeaders Default HTTP headers.
         * @return Builder.
         */
        public Builder defaultHttpHeaders(HttpHeaders defaultHttpHeaders) {
            config.setDefaultHttpHeaders(defaultHttpHeaders);
            return this;
        }

        /**
         * Configure filter function.
         * @param filter Filter function.
         * @return Builder.
         */
        public Builder filter(ExchangeFilterFunction filter) {
            config.setFilter(filter);
            return this;
        }

        /**
         * Configure jackson.
         * @return JacksonConfigurationBuilder.
         */
        public Builder jacksonConfiguration(RestClientConfiguration.JacksonConfiguration jacksonConfiguration) {
            config.setJacksonConfiguration(jacksonConfiguration);
            return this;
        }

        /**
         * Configure jackson modules.
         * @param modules Jackson modules.
         * @return Builder.
         */
        public Builder modules(Collection<Module> modules) {
            modules.addAll(modules);
            return this;
        }

    }

    /**
     * Proxy builder.
     */
    public static class ProxyBuilder {

        private final Builder mainBuilder;

        /**
         * Proxy builder constructor.
         * @param mainBuilder Parent builder.
         */
        private ProxyBuilder(Builder mainBuilder) {
            this.mainBuilder = mainBuilder;
        }

        /**
         * Configure proxy host.
         * @param proxyHost Proxy host.
         * @return ProxyBuilder.
         */
        public ProxyBuilder host(String proxyHost) {
            mainBuilder.config.setProxyHost(proxyHost);
            return this;
        }

        /**
         * Configure proxy port.
         * @param proxyPort Proxy port.
         * @return ProxyBuilder.
         */
        public ProxyBuilder port(int proxyPort) {
            mainBuilder.config.setProxyPort(proxyPort);
            return this;
        }

        /**
         * Configure proxy username.
         * @param proxyUsername Proxy username.
         * @return ProxyBuilder.
         */
        public ProxyBuilder username(String proxyUsername) {
            mainBuilder.config.setProxyUsername(proxyUsername);
            return this;
        }

        /**
         * Configure proxy password.
         * @param proxyPassword Proxy password.
         * @return ProxyBuilder.
         */
        public ProxyBuilder password(String proxyPassword) {
            mainBuilder.config.setProxyPassword(proxyPassword);
            return this;
        }

        /**
         * Build the builder.
         * @return Builder.
         */
        public Builder build() {
            return mainBuilder;
        }
    }

    /**
     * HTTP basic authentication builder.
     */
    public static class HttpBasicAuthBuilder {

        private final Builder mainBuilder;

        /**
         * HTTP basic authentication builder constructor.
         *
         * @param mainBuilder Parent builder.
         */
        private HttpBasicAuthBuilder(Builder mainBuilder) {
            this.mainBuilder = mainBuilder;
        }

        /**
         * Configure HTTP basic authentication username.
         * @param basicAuthUsername HTTP basic authentication username.
         * @return Builder.
         */
        public HttpBasicAuthBuilder username(String basicAuthUsername) {
            mainBuilder.config.setHttpBasicAuthUsername(basicAuthUsername);
            return this;
        }

        /**
         * Configure HTTP basic authentication password.
         * @param basicAuthPassword HTTP basic authentication password.
         * @return Builder.
         */
        public HttpBasicAuthBuilder password(String basicAuthPassword) {
            mainBuilder.config.setHttpBasicAuthPassword(basicAuthPassword);
            return this;
        }

        /**
         * Build the builder.
         *
         * @return Builder.
         */
        public Builder build() {
            return mainBuilder;
        }
    }

    /**
     * HTTP digest authentication builder.
     */
    public static class HttpDigestAuthBuilder {

        private final Builder mainBuilder;

        /**
         * HTTP digest authentication builder constructor.
         *
         * @param mainBuilder Parent builder.
         */
        private HttpDigestAuthBuilder(Builder mainBuilder) {
            this.mainBuilder = mainBuilder;
        }

        /**
         * Configure HTTP digest authentication username.
         *
         * @param digestAuthUsername HTTP digest authentication username.
         * @return Builder.
         */
        public HttpDigestAuthBuilder username(String digestAuthUsername) {
            mainBuilder.config.setHttpDigestAuthUsername(digestAuthUsername);
            return this;
        }

        /**
         * Configure HTTP digest authentication password.
         *
         * @param digestAuthPassword HTTP digest authentication password.
         * @return Builder.
         */
        public HttpDigestAuthBuilder password(String digestAuthPassword) {
            mainBuilder.config.setHttpDigestAuthPassword(digestAuthPassword);
            return this;
        }

        /**
         * Build the builder.
         *
         * @return Builder.
         */
        public Builder build() {
            return mainBuilder;
        }
    }

    /**
     * Certificate authentication builder.
     */
    public static class CertificateAuthBuilder {

        private final Builder mainBuilder;

        /**
         * Certificate authentication builder constructor.
         *
         * @param mainBuilder Parent builder.
         */
        private CertificateAuthBuilder(Builder mainBuilder) {
            this.mainBuilder = mainBuilder;
        }

        /**
         * Enable custom keystore.
         * @return Builder.
         */
        public CertificateAuthBuilder enableCustomKeyStore() {
            mainBuilder.config.setUseCustomKeyStore(true);
            return this;
        }

        /**
         * Set keystore location.
         * @param keyStoreLocation Keystore location.
         * @return Builder.
         */
        public CertificateAuthBuilder keyStoreLocation(String keyStoreLocation) {
            mainBuilder.config.setKeyStoreLocation(keyStoreLocation);
            return this;
        }

        /**
         * Set keystore password.
         * @param keyStorePassword Keystore password.
         * @return Builder.
         */
        public CertificateAuthBuilder keyStorePassword(String keyStorePassword) {
            mainBuilder.config.setKeyStorePassword(keyStorePassword);
            return this;
        }

        /**
         * Set key alias.
         * @param keyAlias Key alias.
         * @return Builder.
         */
        public CertificateAuthBuilder keyAlias(String keyAlias) {
            mainBuilder.config.setKeyAlias(keyAlias);
            return this;
        }

        /**
         * Set key password.
         * @param keyPassword Key password.
         * @return Builder.
         */
        public CertificateAuthBuilder keyPassword(String keyPassword) {
            mainBuilder.config.setKeyPassword(keyPassword);
            return this;
        }

        /**
         * Enable custom truststore.
         * @return Builder.
         */
        public CertificateAuthBuilder enableCustomTruststore() {
            mainBuilder.config.setUseCustomTrustStore(true);
            return this;
        }

        /**
         * Set truststore location.
         * @param trustStoreLocation Truststore location.
         * @return Builder.
         */
        public CertificateAuthBuilder trustStoreLocation(String trustStoreLocation) {
            mainBuilder.config.setTrustStoreLocation(trustStoreLocation);
            return this;
        }

        /**
         * Set truststore password.
         * @param trustStorePassword Truststore password.
         * @return Builder.
         */
        public CertificateAuthBuilder trustStorePassword(String trustStorePassword) {
            mainBuilder.config.setTrustStorePassword(trustStorePassword);
            return this;
        }

        /**
         * Build the builder.
         *
         * @return Builder.
         */
        public Builder build() {
            return mainBuilder;
        }
    }

}
