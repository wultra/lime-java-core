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

import com.fasterxml.jackson.databind.type.TypeFactory;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;

import javax.net.ssl.SSLException;
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


    public DefaultRestClient(String baseUrl) throws RestClientException {
        this.config = new RestClientConfiguration(baseUrl);
        // Use default WebClient settings
        initializeWebClient();
    }

    private DefaultRestClient(Builder builder) throws RestClientException {
        this.config = builder.config;
        // Use WebClient settings from the builder
        initializeWebClient();
    }

    /**
     * Initialize WebClient instance and configure it based on client configuration.
     */
    private void initializeWebClient() throws RestClientException {
        if (config.getBaseUrl() == null) {
            throw new RestClientException("Missing parameter baseUrl");
        }
        try {
            new URI(config.getBaseUrl());
        } catch (URISyntaxException ex) {
            throw new RestClientException("Invalid parameter baseUrl");
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
        httpClient = httpClient.tcpConfiguration(tcpClient -> {
                            if (config.getConnectionTimeout() != null) {
                                tcpClient = tcpClient.option(
                                        ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                        config.getConnectionTimeout());
                            }
                            if (config.isProxyEnabled()) {
                                tcpClient = tcpClient.proxy(proxySpec -> {
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
                            return tcpClient;
                        }
                );
        builder.exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(config.getMaxInMemorySize()))
                .build());

        if (config.getBasicAuthUsername() != null) {
            builder.filter(ExchangeFilterFunctions
                    .basicAuthentication(config.getBasicAuthUsername(), config.getBasicAuthPassword()))
                    .build();
        }

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        webClient = builder.baseUrl(config.getBaseUrl()).clientConnector(connector).build();
    }


    @Override
    public <T> ResponseEntity<T> get(String path, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return get(path, null, responseType);
    }

    @Override
    public <T> ResponseEntity<T> get(String path, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException {
        ClientResponse response;
        try {
            response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path(path).build())
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .exchange()
                    .block();
            validateResponse(response, responseType);
            return response.toEntity(responseType).block();
        } catch (RestClientException ex) {
            // Rethrow validation errors
            throw ex;
        } catch (Exception ex) {
            throw new RestClientException("HTTP GET request failed", ex);
        }
    }

    @Override
    public void getNonBlocking(String path, Consumer<ClientResponse> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        getNonBlocking(path, null, onSuccess, onError);
    }

    @Override
    public void getNonBlocking(String path, MultiValueMap<String, String> headers, Consumer<ClientResponse> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        try {
            webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path(path).build())
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .accept(config.getAcceptType())
                    .exchange()
                    .subscribe(onSuccess, onError);
        } catch (Exception ex) {
            throw new RestClientException("HTTP GET request failed", ex);
        }
    }

    @Override
    public <T> ObjectResponse<T> getObject(String path, Class<T> responseType) throws RestClientException {
        return getObject(path, null, responseType);
    }

    @Override
    public <T> ObjectResponse<T> getObject(String path, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException {
        ParameterizedTypeReference<ObjectResponse<T>> typeReference = getTypeReference(responseType);
        ResponseEntity<ObjectResponse<T>> responseEntity = get(path, headers, typeReference);
        return responseEntity.getBody();
    }

    @Override
    public <T> ResponseEntity<T> post(String path, Object request, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return post(path, request, null, responseType);
    }

    @Override
    public <T> ResponseEntity<T> post(String path, Object request, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException {
        ClientResponse response;
        try {
            WebClient.RequestBodySpec spec = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path(path).build())
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(config.getContentType())
                    .accept(config.getAcceptType());
            Mono<ClientResponse> responseMono;
            if (request != null) {
                responseMono = spec.body(BodyInserters.fromValue(request)).exchange();
            } else {
                responseMono = spec.exchange();
            }
            response = responseMono.block();
            validateResponse(response, responseType);
            return response.toEntity(responseType).block();
        } catch (RestClientException ex) {
            // Rethrow validation errors
            throw ex;
        } catch (Exception ex) {
            throw new RestClientException("HTTP POST request failed", ex);
        }
    }

    @Override
    public void postNonBlocking(String path, Object request, Consumer<ClientResponse> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        postNonBlocking(path, request, null, onSuccess, onError);
    }

    @Override
    public void postNonBlocking(String path, Object request, MultiValueMap<String, String> headers, Consumer<ClientResponse> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        try {
            WebClient.RequestBodySpec spec = webClient
                    .post()
                    .uri(uriBuilder -> uriBuilder.path(path).build())
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(config.getContentType())
                    .accept(config.getAcceptType());
            Mono<ClientResponse> responseMono;
            if (request != null) {
                responseMono = spec.body(BodyInserters.fromValue(request)).exchange();
            } else {
                responseMono = spec.exchange();
            }
            responseMono.subscribe(onSuccess, onError);
        } catch (Exception ex) {
            throw new RestClientException("HTTP POST request failed", ex);
        }
    }

    @Override
    public <T> ObjectResponse<T> postObject(String path, ObjectRequest<?> objectRequest, Class<T> responseType) throws RestClientException {
        return postObject(path, objectRequest, null, responseType);
    }

    @Override
    public <T> ObjectResponse<T> postObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException {
        ParameterizedTypeReference<ObjectResponse<T>> typeReference = getTypeReference(responseType);
        ResponseEntity<ObjectResponse<T>> responseEntity = post(path, objectRequest, headers, typeReference);
        return responseEntity.getBody();
    }

    @Override
    public <T> ResponseEntity<T> put(String path, Object request, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return put(path, request, null, responseType);
    }

    @Override
    public <T> ResponseEntity<T> put(String path, Object request, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException {
        ClientResponse response;
        try {
            WebClient.RequestBodySpec spec = webClient.put()
                    .uri(uriBuilder -> uriBuilder.path(path).build())
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(config.getContentType())
                    .accept(config.getAcceptType());
            Mono<ClientResponse> responseMono;
            if (request != null) {
                responseMono = spec.body(BodyInserters.fromValue(request)).exchange();
            } else {
                responseMono = spec.exchange();
            }
            response = responseMono.block();
            validateResponse(response, responseType);
            return response.toEntity(responseType).block();
        } catch (RestClientException ex) {
            // Rethrow validation errors
            throw ex;
        } catch (Exception ex) {
            throw new RestClientException("HTTP PUT request failed", ex);
        }
    }

    @Override
    public void putNonBlocking(String path, Object request, Consumer<ClientResponse> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        putNonBlocking(path, request, null, onSuccess, onError);
    }

    @Override
    public void putNonBlocking(String path, Object request, MultiValueMap<String, String> headers, Consumer<ClientResponse> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        try {
            WebClient.RequestBodySpec spec = webClient
                    .put()
                    .uri(uriBuilder -> uriBuilder.path(path).build())
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(config.getContentType())
                    .accept(config.getAcceptType());
            Mono<ClientResponse> responseMono;
            if (request != null) {
                responseMono = spec.body(BodyInserters.fromValue(request)).exchange();
            } else {
                responseMono = spec.exchange();
            }
            responseMono.subscribe(onSuccess, onError);
        } catch (Exception ex) {
            throw new RestClientException("HTTP PUT request failed", ex);
        }
    }

    @Override
    public <T> ObjectResponse<T> putObject(String path, ObjectRequest<?> objectRequest, Class<T> responseType) throws RestClientException {
        return putObject(path, objectRequest, null ,responseType);
    }

    @Override
    public <T> ObjectResponse<T> putObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException {
        ParameterizedTypeReference<ObjectResponse<T>> typeReference = getTypeReference(responseType);
        ResponseEntity<ObjectResponse<T>> responseEntity = put(path, objectRequest, headers, typeReference);
        return responseEntity.getBody();
    }

    @Override
    public <T> ResponseEntity<T> delete(String path, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return delete(path, null, responseType);
    }

    @Override
    public <T> ResponseEntity<T> delete(String path, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException {
        ClientResponse response;
        try {
            response = webClient
                    .delete()
                    .uri(uriBuilder -> uriBuilder.path(path).build())
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .exchange()
                    .block();
            validateResponse(response, responseType);
            return response.toEntity(responseType).block();
        } catch (RestClientException ex) {
            // Rethrow validation errors
            throw ex;
        } catch (Exception ex) {
            throw new RestClientException("HTTP DELETE request failed", ex);
        }
    }

    @Override
    public void deleteNonBlocking(String path, Consumer<ClientResponse> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        deleteNonBlocking(path, null, onSuccess, onError);
    }

    @Override
    public void deleteNonBlocking(String path, MultiValueMap<String, String> headers, Consumer<ClientResponse> onSuccess, Consumer<Throwable> onError) throws RestClientException {
        try {
            webClient
                    .put()
                    .uri(uriBuilder -> uriBuilder.path(path).build())
                    .headers(h -> {
                        if (headers != null) {
                            h.addAll(headers);
                        }
                    })
                    .contentType(config.getContentType())
                    .accept(config.getAcceptType())
                    .exchange()
                    .subscribe(onSuccess, onError);
        } catch (Exception ex) {
            throw new RestClientException("HTTP DELETE request failed", ex);
        }
    }

    @Override
    public <T> ObjectResponse<T> deleteObject(String path, Class<T> responseType) throws RestClientException {
        return deleteObject(path, null, responseType);
    }

    @Override
    public <T> ObjectResponse<T> deleteObject(String path, MultiValueMap<String, String> headers,Class<T> responseType) throws RestClientException {
        ResponseEntity<ObjectResponse<T>> responseEntity = delete(path, headers, getTypeReference(responseType));
        return responseEntity.getBody();
    }

    private <T> ParameterizedTypeReference<ObjectResponse<T>> getTypeReference(Class<T> responseType) {
        return new ParameterizedTypeReference<ObjectResponse<T>>(){
            @Override
            public Type getType() {
                return TypeFactory.defaultInstance().constructParametricType(ObjectResponse.class, responseType);
            }
        };
    }

    private void validateResponse(ClientResponse response, ParameterizedTypeReference<?> responseType) throws RestClientException {
        if (response == null) {
            throw new RestClientException("Missing response");
        }
        if (responseType == null) {
            throw new RestClientException("Missing response type");
        }
        if (response.statusCode().isError()) {
            throw new RestClientException("HTTP error occurred: " + response.statusCode(), response.statusCode());
        }
    }

    public static class Builder {

        private final RestClientConfiguration config;

        public Builder(String baseUrl) {
            config = new RestClientConfiguration(baseUrl);
        }

        public DefaultRestClient build() throws RestClientException {
            return new DefaultRestClient(this);
        }

        public Builder contentType(MediaType contentType) {
            config.setContentType(contentType);
            return this;
        }

        public Builder acceptType(MediaType acceptType) {
            config.setAcceptType(acceptType);
            return this;
        }

        // TODO - proxy() builder
        public Builder proxyEnabled(boolean proxyEnabled) {
            config.setProxyEnabled(proxyEnabled);
            return this;
        }

        public Builder proxyHost(String proxyHost) {
            config.setProxyHost(proxyHost);
            return this;
        }

        public Builder proxyPort(int proxyPort) {
            config.setProxyPort(proxyPort);
            return this;
        }

        public Builder proxyUsername(String proxyUsername) {
            config.setProxyUsername(proxyUsername);
            return this;
        }

        public Builder proxyPassword(String proxyPassword) {
            config.setProxyPassword(proxyPassword);
            return this;
        }

        public Builder connectionTimeout(Integer connectionTimeout) {
            config.setConnectionTimeout(connectionTimeout);
            return this;
        }

        public Builder acceptInvalidCertificate(boolean acceptInvalidSslCertificate) {
            config.setAcceptInvalidSslCertificate(acceptInvalidSslCertificate);
            return this;
        }

        public Builder maxInMemorySize(Integer maxInMemorySize) {
            config.setMaxInMemorySize(maxInMemorySize);
            return this;
        }

        // TODO - httpBasicAuth() builder
        public Builder basicAuthUsername(String basicAuthUsername) {
            config.setBasicAuthUsername(basicAuthUsername);
            return this;
        }

        public Builder basicAuthPassword(String basicAuthPassword) {
            config.setBasicAuthPassword(basicAuthPassword);
            return this;
        }
    }
}
