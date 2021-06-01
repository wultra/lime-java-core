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
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * Default REST client implementation based on WebClient.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class DefaultRestClient implements RestClient {

    private WebClient webClient;
    private final RestClientConfiguration config;

    /**
     * Construct default REST client without any additional configuration.
     * @param baseUrl Base URL.
     * @throws RestClientException Thrown in case client initialization fails.
     */
    public DefaultRestClient(String baseUrl) throws RestClientException {
        this.config = new RestClientConfiguration();
        this.config.setBaseUrl(baseUrl);
        // Use default WebClient settings
        initializeWebClient();
    }

    /**
     * Construct default REST client with specified configuration.
     * @param config REST client configuration.
     * @throws RestClientException Thrown in case client initialization fails.
     */
    public DefaultRestClient(RestClientConfiguration config) throws RestClientException {
        // Use WebClient configuration from the config constructor parameter
        this.config = config;
        initializeWebClient();
    }

    /**
     * Private constructor for builder.
     * @param builder REST client builder.
     * @throws RestClientException Thrown in case client initialization fails.
     */
    private DefaultRestClient(Builder builder) throws RestClientException {
        // Use WebClient settings from the builder
        this.config = builder.config;
        initializeWebClient();
    }

    /**
     * Initialize WebClient instance and configure it based on client configuration.
     */
    private void initializeWebClient() throws RestClientException {
        if (config.getBaseUrl() != null) {
            try {
                new URI(config.getBaseUrl());
            } catch (URISyntaxException ex) {
                throw new RestClientException("Invalid parameter baseUrl");
            }
        }
        WebClient.Builder builder = WebClient.builder();
        HttpClient httpClient = HttpClient.create();
        SslContext sslContext;
        try {
            if (config.isAcceptInvalidSslCertificate()) {
                sslContext = SslContextBuilder
                        .forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
                httpClient = httpClient.secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            }
        } catch (SSLException ex) {
            throw new RestClientException("SSL error occurred: " + ex.getMessage(), ex);
        }
        if (config.getConnectionTimeout() != null) {
            httpClient.option(
                    ChannelOption.CONNECT_TIMEOUT_MILLIS,
                    config.getConnectionTimeout());
        }
        if (config.isProxyEnabled()) {
            httpClient.proxy(proxySpec -> {
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

        final ObjectMapper objectMapper = config.getObjectMapper();
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    ClientCodecConfigurer.ClientDefaultCodecs defaultCodecs = configurer.defaultCodecs();
                    if (objectMapper != null) {
                        defaultCodecs.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                        defaultCodecs.jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                    }
                    defaultCodecs.maxInMemorySize(config.getMaxInMemorySize());
                })
                .build();
        builder.exchangeStrategies(exchangeStrategies);

        if (config.getHttpBasicAuthUsername() != null) {
            builder.filter(ExchangeFilterFunctions
                    .basicAuthentication(config.getHttpBasicAuthUsername(), config.getHttpBasicAuthPassword()));
        }

        if (config.getFilter() != null) {
            builder.filter(config.getFilter());
        }

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        webClient = builder.baseUrl(config.getBaseUrl()).clientConnector(connector).build();
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
                    .contentType(config.getContentType())
                    .accept(config.getAcceptType());
            return buildRequest(spec, request)
                    .exchangeToMono(rs -> handleResponse(rs, responseType))
                    .block();
        } catch (Exception ex) {
            if (ex.getCause() instanceof RestClientException) {
                // Throw exceptions created by REST client
                throw (RestClientException) ex.getCause();
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
                    .contentType(config.getContentType())
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
                    .contentType(config.getContentType())
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
                    .contentType(config.getContentType())
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

    /**
     * Convert response type to parameterized type reference of ObjectResponse.
     * @param responseType Object response type.
     * @param <T> Response type.
     * @return Parameterized type reference of ObjectResponse.
     */
    private <T> ParameterizedTypeReference<ObjectResponse<T>> getTypeReference(Class<T> responseType) {
        return new ParameterizedTypeReference<ObjectResponse<T>>(){
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
            if (request instanceof Publisher) {
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

        /**
         * Construct new builder with given base URL.
         */
        public Builder() {
            config = new RestClientConfiguration();
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
        public Builder connectionTimeout(Integer connectionTimeout) {
            config.setConnectionTimeout(connectionTimeout);
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
}
