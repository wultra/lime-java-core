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

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.util.function.Consumer;

/**
 * REST client interface for both generic requests / responses as well as for the ObjectRequest / ObjectResponse types.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public interface RestClient {

    /**
     * Execute a blocking HTTP GET request.
     * @param path Request path.
     * @param responseType Parameterized response type.
     * @param <T> Response type.
     * @return HTTP GET response.
     * @throws RestClientException Thrown in case HTTP GET request fails.
     */
    <T> ResponseEntity<T> get(String path, ParameterizedTypeReference<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP GET request with specified HTTP headers.
     * @param path Request path.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Parameterized response type.
     * @param <T> Response type.
     * @return HTTP GET response.
     * @throws RestClientException Thrown in case HTTP GET request fails.
     */
    <T> ResponseEntity<T> get(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException;

    /**
     * Execute a non-blocking HTTP GET request.
     * @param path Request path.
     * @param responseType Parameterized response type.
     * @param onSuccess Consumer used in case of success.
     * @param onError Consumer used in case of failure.
     * @throws RestClientException Thrown in case HTTP GET request fails.
     */
    <T> void getNonBlocking(String path, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException;

    /**
     * Execute a non-blocking HTTP GET request with specified HTTP headers.
     * @param path Request path.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Parameterized response type.
     * @param onSuccess Consumer used in case of success.
     * @param onError Consumer used in case of failure.
     * @param <T> Response type.
     * @throws RestClientException Thrown in case HTTP GET request fails.
     */
    <T> void getNonBlocking(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException;

    /**
     * Execute a blocking HTTP GET request with ObjectRequest / Response types.
     * @param path Request path.
     * @return Response.
     * @throws RestClientException Thrown in case HTTP GET request fails.
     */
    Response getObject(String path) throws RestClientException;

    /**
     * Execute a blocking HTTP GET request with ObjectRequest / Response types and specified query parameters and HTTP headers.
     * @param path Request path.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @return Response.
     * @throws RestClientException Thrown in case HTTP GET request fails.
     */
    Response getObject(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers) throws RestClientException;

    /**
     * Execute a blocking HTTP GET request with ObjectRequest / ObjectResponse types.
     * @param path Request path.
     * @param responseType Object response type.
     * @param <T> Response type.
     * @return Object response.
     * @throws RestClientException Thrown in case HTTP GET request fails.
     */
    <T> ObjectResponse<T> getObject(String path, Class<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP GET request with ObjectRequest / ObjectResponse types and specified query parameters and HTTP headers.
     * @param path Request path.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Object response type.
     * @param <T> Response type.
     * @return Object response.
     * @throws RestClientException Thrown in case HTTP GET request fails.
     */
    <T> ObjectResponse<T> getObject(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP POST request.
     * @param path Request path.
     * @param request Request object.
     * @param responseType Parameterized response type.
     * @param <T> Response type.
     * @return HTTP POST response.
     * @throws RestClientException Thrown in case HTTP POST request fails.
     */
    <T> ResponseEntity<T> post(String path, Object request, ParameterizedTypeReference<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP POST request with specified HTTP headers.
     * @param path Request path.
     * @param request Request object.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Parameterized response type.
     * @param <T> Response type.
     * @return HTTP POST response.
     * @throws RestClientException Thrown in case HTTP POST request fails.
     */
    <T> ResponseEntity<T> post(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException;

    /**
     * Execute a non-blocking HTTP POST request.
     * @param path Request path.
     * @param request Request object.
     * @param responseType Parameterized response type.
     * @param onSuccess Consumer used in case of success.
     * @param onError Consumer used in case of failure.
     * @param <T> Response type.
     * @throws RestClientException Thrown in case HTTP POST request fails.
     */
    <T> void postNonBlocking(String path, Object request, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException;

    /**
     * Execute a non-blocking HTTP POST request with specified HTTP headers.
     * @param path Request path.
     * @param request Request object.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Parameterized response type.
     * @param onSuccess Consumer used in case of success.
     * @param onError Consumer used in case of failure.
     * @param <T> Response type.
     * @throws RestClientException Thrown in case HTTP POST request fails.
     */
    <T> void postNonBlocking(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException;

    /**
     * Execute a blocking HTTP POST request with ObjectRequest / Response types.
     * @param path Request path.
     * @param objectRequest Object request.
     * @return Response.
     * @throws RestClientException Thrown in case HTTP POST request fails.
     */
    Response postObject(String path, ObjectRequest<?> objectRequest) throws RestClientException;

    /**
     * Execute a blocking HTTP POST request with ObjectRequest / Response types and specified query parameters and HTTP headers.
     * @param path Request path.
     * @param objectRequest Object request.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @return Response.
     * @throws RestClientException Thrown in case HTTP POST request fails.
     */
    Response postObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers) throws RestClientException;

    /**
     * Execute a blocking HTTP POST request with ObjectRequest / ObjectResponse types.
     * @param path Request path.
     * @param objectRequest Object request.
     * @param responseType Object response type.
     * @param <T> Response type.
     * @return Object response.
     * @throws RestClientException Thrown in case HTTP POST request fails.
     */
    <T> ObjectResponse<T> postObject(String path, ObjectRequest<?> objectRequest, Class<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP POST request with ObjectRequest / ObjectResponse types and specified query parameters and HTTP headers.
     * @param path Request path.
     * @param objectRequest Object request.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Object response type.
     * @param <T> Response type.
     * @return Object response.
     * @throws RestClientException Thrown in case HTTP POST request fails.
     */
    <T> ObjectResponse<T> postObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP PUT request.
     * @param path Request path.
     * @param request Request object.
     * @param responseType Parameterized response type.
     * @param <T> Response type.
     * @return HTTP PUT response.
     * @throws RestClientException Thrown in case HTTP PUT request fails.
     */
    <T> ResponseEntity<T> put(String path, Object request, ParameterizedTypeReference<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP PUT request with specified HTTP headers.
     * @param path Request path.
     * @param request Request object.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Parameterized response type.
     * @param <T> Response type.
     * @return HTTP PUT response.
     * @throws RestClientException Thrown in case HTTP PUT request fails.
     */
    <T> ResponseEntity<T> put(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException;

    /**
     * Execute a non-blocking HTTP PUT request.
     * @param path Request path.
     * @param request Request object.
     * @param responseType Parameterized response type.
     * @param onSuccess Consumer used in case of success.
     * @param onError Consumer used in case of failure.
     * @param <T> Response type.
     * @throws RestClientException Thrown in case HTTP PUT request fails.
     */
    <T> void putNonBlocking(String path, Object request, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException;

    /**
     * Execute a non-blocking HTTP PUT request with specified HTTP headers.
     * @param path Request path.
     * @param request Request object.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Parameterized response type.
     * @param onSuccess Consumer used in case of success.
     * @param onError Consumer used in case of failure.
     * @param <T> Response type.
     * @throws RestClientException Thrown in case HTTP PUT request fails.
     */
    <T> void putNonBlocking(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException;

    /**
     * Execute a blocking HTTP PUT request with ObjectRequest / Response types.
     * @param path Request path.
     * @param objectRequest Object request.
     * @return Response.
     * @throws RestClientException Thrown in case HTTP PUT request fails.
     */
    Response putObject(String path, ObjectRequest<?> objectRequest) throws RestClientException;

    /**
     * Execute a blocking HTTP PUT request with ObjectRequest / Response types and specified query parameters and HTTP headers.
     * @param path Request path.
     * @param objectRequest Object request.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @return Response.
     * @throws RestClientException Thrown in case HTTP PUT request fails.
     */
    Response putObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers) throws RestClientException;

    /**
     * Execute a blocking HTTP PUT request with ObjectRequest / ObjectResponse types.
     * @param path Request path.
     * @param objectRequest Object request.
     * @param responseType Object response type.
     * @param <T> Response type.
     * @return Object response.
     * @throws RestClientException Thrown in case HTTP PUT request fails.
     */
    <T> ObjectResponse<T> putObject(String path, ObjectRequest<?> objectRequest, Class<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP PUT request with ObjectRequest / ObjectResponse types and specified query parameters and HTTP headers.
     * @param path Request path.
     * @param objectRequest Object request.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Object response type.
     * @param <T> Response type.
     * @return Object response.
     * @throws RestClientException Thrown in case HTTP PUT request fails.
     */
    <T> ObjectResponse<T> putObject(String path, ObjectRequest<?> objectRequest, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP DELETE request.
     * @param path Request path.
     * @param responseType Parameterized response type.
     * @param <T> Response type.
     * @return HTTP DELETE response.
     * @throws RestClientException Thrown in case HTTP DELETE request fails.
     */
    <T> ResponseEntity<T> delete(String path, ParameterizedTypeReference<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP DELETE request with specified HTTP headers.
     * @param path Request path.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Parameterized response type.
     * @param <T> Response type.
     * @return HTTP DELETE response.
     * @throws RestClientException Thrown in case HTTP DELETE request fails.
     */
    <T> ResponseEntity<T> delete(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType) throws RestClientException;

    /**
     * Execute a non-blocking HTTP DELETE request.
     * @param path Request path.
     * @param responseType Parameterized response type.
     * @param onSuccess Consumer used in case of success.
     * @param onError Consumer used in case of failure.
     * @param <T> Response type.
     * @throws RestClientException Thrown in case HTTP DELETE request fails.
     */
    <T> void deleteNonBlocking(String path, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException;

    /**
     * Execute a non-blocking HTTP DELETE request with specified HTTP headers.
     * @param path Request path.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Parameterized response type.
     * @param onSuccess Consumer used in case of success.
     * @param onError Consumer used in case of failure.
     * @param <T> Response type.
     * @throws RestClientException Thrown in case HTTP DELETE request fails.
     */
    <T> void deleteNonBlocking(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws RestClientException;

    /**
     * Execute a blocking HTTP DELETE request with ObjectRequest / Response types.
     * @param path Request path.
     * @return Response.
     * @throws RestClientException Thrown in case HTTP DELETE request fails.
     */
    Response deleteObject(String path) throws RestClientException;

    /**
     * Execute a blocking HTTP DELETE request with ObjectRequest / Response types and specified query parameters and HTTP headers.
     * @param path Request path.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @return Response.
     * @throws RestClientException Thrown in case HTTP DELETE request fails.
     */
    Response deleteObject(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers) throws RestClientException;

    /**
     * Execute a blocking HTTP DELETE request with ObjectRequest / ObjectResponse types.
     * @param path Request path.
     * @param responseType Object response type.
     * @param <T> Response type.
     * @return Object response.
     * @throws RestClientException Thrown in case HTTP DELETE request fails.
     */
    <T> ObjectResponse<T> deleteObject(String path, Class<T> responseType) throws RestClientException;

    /**
     * Execute a blocking HTTP DELETE request with ObjectRequest / ObjectResponse types and specified query parameters and HTTP headers.
     * @param path Request path.
     * @param queryParams Query parameters.
     * @param headers HTTP headers.
     * @param responseType Object response type.
     * @param <T> Response type.
     * @return Object response.
     * @throws RestClientException Thrown in case HTTP DELETE request fails.
     */
    <T> ObjectResponse<T> deleteObject(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, Class<T> responseType) throws RestClientException;

}
