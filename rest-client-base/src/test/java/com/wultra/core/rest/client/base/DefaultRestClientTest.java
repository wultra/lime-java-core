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

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.core.rest.client.base.model.TestRequest;
import com.wultra.core.rest.client.base.model.TestResponse;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link DefaultRestClient}.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DefaultRestClientTest {

    private static final String PUBLIC_PATH = "/public/api/test";
    private static final String PRIVATE_PATH = "/private/api/test";

    @LocalServerPort
    private int port;

    private RestClient restClient;

    private String publicBaseUrl;
    private String privateBaseUrl;

    // Timeout for synchronization of non-blocking calls using countdown latch
    private static final int SYNCHRONIZATION_TIMEOUT = 10000;

    @BeforeEach
    void initRestClient() throws RestClientException {
        final RestClientConfiguration config = prepareConfiguration();
        final String baseUrl = "https://localhost:" + port;
        publicBaseUrl = baseUrl + PUBLIC_PATH;
        privateBaseUrl = baseUrl + PRIVATE_PATH;
        config.setBaseUrl(publicBaseUrl);
        restClient = new DefaultRestClient(config);
    }

    private RestClientConfiguration prepareConfiguration() {
        RestClientConfiguration config = new RestClientConfiguration();
        config.setCertificateAuthEnabled(true);
        config.setUseCustomKeyStore(true);
        config.setKeyStoreLocation("classpath:ssl/keystore-client.jks");
        config.setKeyStorePassword("changeit");
        config.setKeyAlias("client");
        config.setKeyPassword("changeit");
        config.setUseCustomTrustStore(true);
        config.setTrustStoreLocation("classpath:ssl/truststore.jks");
        config.setTrustStorePassword("changeit");
        config.setHandshakeTimeout(Duration.ofSeconds(5));
        config.setHttpBasicAuthEnabled(true);
        config.setHttpBasicAuthUsername("test");
        config.setHttpBasicAuthPassword("test");
        config.setResponseTimeout(Duration.ofSeconds(10));
        config.setSimpleLoggingEnabled(true);
        return config;
    }

    @Test
    void testGetWithResponse() throws RestClientException {
        final ResponseEntity<Response> responseEntity = restClient.get("/response", new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    void testGetWithResponseObject() throws RestClientException {
        Response response = restClient.getObject("/response");
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testGetWithResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<Response>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.getNonBlocking("/response", new ParameterizedTypeReference<>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void testGetWithTestResponse() throws RestClientException {
        final ResponseEntity<TestResponse> responseEntity = restClient.get("/test-response", new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("test response", responseEntity.getBody().getResponse());
    }

    @Test
    void testGetWithObjectResponse() throws RestClientException {
        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.get("/object-response", new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals("object response", responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testGetWithObjectResponseObject() throws RestClientException {
        ObjectResponse<TestResponse> response = restClient.getObject("/object-response", TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals("object response", response.getResponseObject().getResponse());
    }

    @Test
    void testPostWithResponse() throws RestClientException {
        final ResponseEntity<Response> responseEntity = restClient.post("/response", null, new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    void testPostWithResponseObject() throws RestClientException {
        Response response = restClient.postObject("/response", null);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testPostWithResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<Response>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.postNonBlocking("/response", null, new ParameterizedTypeReference<>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void testPostWithTestResponse() throws RestClientException {
        final ResponseEntity<TestResponse> responseEntity = restClient.post("/test-response", null, new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("test response", responseEntity.getBody().getResponse());
    }

    @Test
    void testPostWithObjectResponse() throws RestClientException {
        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.post("/object-response", null, new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals("object response", responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testPostWithObjectResponseObject() throws RestClientException {
        ObjectResponse<TestResponse> response = restClient.postObject("/object-response", null, TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals("object response", response.getResponseObject().getResponse());
    }

    @Test
    void testPostWithObjectRequestResponse() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.post("/object-request-response", request, new ParameterizedTypeReference<>(){});
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getResponseObject());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals(requestData, responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testPostWithObjectRequestResponseObject() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        ObjectResponse<TestResponse> response = restClient.postObject("/object-request-response", request, TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals(requestData, response.getResponseObject().getResponse());
    }

    @Test
    void testPostWithObjectRequestResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        Consumer<ResponseEntity<ObjectResponse<TestResponse>>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertNotNull(responseEntity.getBody().getResponseObject());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.postNonBlocking("/object-request-response", request, new ParameterizedTypeReference<>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void testPutWithResponse() throws RestClientException {
        final ResponseEntity<Response> responseEntity = restClient.put("/response", null, new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    void testPutWithResponseObject() throws RestClientException {
        Response response = restClient.putObject("/response", null);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testPutWithResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<Response>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.putNonBlocking("/response", null, new ParameterizedTypeReference<>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void testPutWithTestResponse() throws RestClientException {
        final ResponseEntity<TestResponse> responseEntity = restClient.put("/test-response", null, new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("test response", responseEntity.getBody().getResponse());
    }

    @Test
    void testPutWithObjectResponse() throws RestClientException {
        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.put("/object-response", null, new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals("object response", responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testPutWithObjectResponseObject() throws RestClientException {
        ObjectResponse<TestResponse> response = restClient.putObject("/object-response", null, TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals("object response", response.getResponseObject().getResponse());
    }

    @Test
    void testPutWithObjectRequestResponse() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.put("/object-request-response", request, new ParameterizedTypeReference<>(){});
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getResponseObject());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals(requestData, responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testPutWithObjectRequestResponseObject() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        ObjectResponse<TestResponse> response = restClient.putObject("/object-request-response", request, TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals(requestData, response.getResponseObject().getResponse());
    }

    @Test
    void testPutWithObjectRequestResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        Consumer<ResponseEntity<ObjectResponse<TestResponse>>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertNotNull(responseEntity.getBody().getResponseObject());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.putNonBlocking("/object-request-response", request, new ParameterizedTypeReference<>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void testDeleteWithResponse() throws RestClientException {
        final ResponseEntity<Response> responseEntity = restClient.delete("/response", new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    void testDeleteWithResponseObject() throws RestClientException {
        Response response = restClient.deleteObject("/response");
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testDeleteWithResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<Response>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.deleteNonBlocking("/response", new ParameterizedTypeReference<>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void testDeleteWithTestResponse() throws RestClientException {
        final ResponseEntity<TestResponse> responseEntity = restClient.delete("/test-response", new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("test response", responseEntity.getBody().getResponse());
    }

    @Test
    void testDeleteWithObjectResponse() throws RestClientException {
        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.delete("/object-response", new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals("object response", responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testDeleteWithObjectResponseObject() throws RestClientException {
        ObjectResponse<TestResponse> response = restClient.deleteObject("/object-response", TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals("object response", response.getResponseObject().getResponse());
    }

    @Test
    void testPostWithErrorResponse() {
        try {
            restClient.post("/error-response", null, new ParameterizedTypeReference<ObjectResponse<TestResponse>>() {});
        } catch (RestClientException ex) {
            assertEquals(400, ex.getStatusCode().value());
            assertEquals("{\"status\":\"ERROR\",\"responseObject\":{\"code\":\"TEST_CODE\",\"message\":\"Test message\"}}", ex.getResponse());
            assertNotNull(ex.getErrorResponse());
            assertEquals("ERROR", ex.getErrorResponse().getStatus());
            assertEquals("TEST_CODE", ex.getErrorResponse().getResponseObject().getCode());
            assertEquals("Test message", ex.getErrorResponse().getResponseObject().getMessage());
        }
    }

    @Test
    void testPostWithErrorResponseObject() {
        try {
            restClient.postObject("/error-response", null, TestResponse.class);
        } catch (RestClientException ex) {
            assertEquals(400, ex.getStatusCode().value());
            assertEquals("{\"status\":\"ERROR\",\"responseObject\":{\"code\":\"TEST_CODE\",\"message\":\"Test message\"}}", ex.getResponse());
            assertNotNull(ex.getErrorResponse());
            assertEquals("ERROR", ex.getErrorResponse().getStatus());
            assertEquals("TEST_CODE", ex.getErrorResponse().getResponseObject().getCode());
            assertEquals("Test message", ex.getErrorResponse().getResponseObject().getMessage());
        }
    }

    @Test
    void testPostWithErrorResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<ObjectResponse<TestResponse>>> onSuccess = okResponse -> Assertions.fail();
        Consumer<Throwable> onError = error -> {
            RestClientException ex = (RestClientException) error;
            assertEquals(400, ex.getStatusCode().value());
            assertEquals("{\"status\":\"ERROR\",\"responseObject\":{\"code\":\"TEST_CODE\",\"message\":\"Test message\"}}", ex.getResponse());
            assertNotNull(ex.getErrorResponse());
            assertEquals("ERROR", ex.getErrorResponse().getStatus());
            assertEquals("TEST_CODE", ex.getErrorResponse().getResponseObject().getCode());
            assertEquals("Test message", ex.getErrorResponse().getResponseObject().getMessage());
            countDownLatch.countDown();
        };
        restClient.postNonBlocking("/error-response", null, new ParameterizedTypeReference<>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void testGetWithFullUrl() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        restClient = new DefaultRestClient(config);
        Response response = restClient.getObject(publicBaseUrl + "/response");
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testPostWithFullUrl() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        restClient = new DefaultRestClient(config);
        Response response = restClient.postObject(publicBaseUrl + "/response", null);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testPutWithFullUrl() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        restClient = new DefaultRestClient(config);
        Response response = restClient.putObject(publicBaseUrl + "/response", null);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testPatchWithResponse() throws RestClientException {
        final ResponseEntity<Response> responseEntity = restClient.patch("/response", null, new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    void testPatchWithResponseObject() throws RestClientException {
        Response response = restClient.patchObject("/response", null);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testPatchWithResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<Response>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.patchNonBlocking("/response", null, new ParameterizedTypeReference<>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void testPatchWithTestResponse() throws RestClientException {
        final ResponseEntity<TestResponse> responseEntity = restClient.patch("/test-response", null, new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("test response", responseEntity.getBody().getResponse());
    }

    @Test
    void testPatchWithObjectResponse() throws RestClientException {
        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.patch("/object-response", null, new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals("object response", responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testPatchWithObjectResponseObject() throws RestClientException {
        ObjectResponse<TestResponse> response = restClient.patchObject("/object-response", null, TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals("object response", response.getResponseObject().getResponse());
    }

    @Test
    void testPatchWithObjectRequestResponse() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.patch("/object-request-response", request, new ParameterizedTypeReference<>(){});
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getResponseObject());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals(requestData, responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testPatchWithObjectRequestResponseObject() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        ObjectResponse<TestResponse> response = restClient.patchObject("/object-request-response", request, TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals(requestData, response.getResponseObject().getResponse());
    }

    @Test
    void testPatchWithObjectRequestResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        Consumer<ResponseEntity<ObjectResponse<TestResponse>>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertNotNull(responseEntity.getBody().getResponseObject());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.patchNonBlocking("/object-request-response", request, new ParameterizedTypeReference<>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void testHeadWithResponse() throws RestClientException {
        final ResponseEntity<Response> responseEntity = restClient.head("/response", new ParameterizedTypeReference<>() {});
        assertFalse(responseEntity.getHeaders().isEmpty());
        assertNull(responseEntity.getBody());
    }

    @Test
    void testHeadWithResponseObject() throws RestClientException {
        Response response = restClient.headObject("/response");
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testHeadWithResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<Response>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getHeaders());
            assertFalse(responseEntity.getHeaders().isEmpty());
            assertNull(responseEntity.getBody());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.headNonBlocking("/response", new ParameterizedTypeReference<>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void testHeadWithTestResponse() throws RestClientException {
        final ResponseEntity<TestResponse> responseEntity = restClient.head("/test-response", new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getHeaders());
        assertFalse(responseEntity.getHeaders().isEmpty());
        assertNull(responseEntity.getBody());
    }

    @Test
    void testHeadWithObjectResponse() throws RestClientException {
        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.head("/object-response", new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getHeaders());
        assertFalse(responseEntity.getHeaders().isEmpty());
        assertNull(responseEntity.getBody());
    }

    @Test
    void testHeadWithObjectResponseObject() throws RestClientException {
        ObjectResponse<TestResponse> response = restClient.headObject("/object-response", TestResponse.class);
        assertEquals("OK", response.getStatus());
        assertNull(response.getResponseObject());
    }

    @Test
    void testHeadWithFullUrl() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        restClient = new DefaultRestClient(config);
        Response response = restClient.headObject(publicBaseUrl + "/response");
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testPatchWithFullUrl() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        restClient = new DefaultRestClient(config);
        Response response = restClient.patchObject(publicBaseUrl + "/response", null);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testDeleteWithFullUrl() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        restClient = new DefaultRestClient(config);
        Response response = restClient.deleteObject(publicBaseUrl + "/response");
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testPostWithDataBuffer() throws RestClientException, JsonProcessingException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] data = objectMapper.writeValueAsBytes(request);
        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        DefaultDataBuffer dataBuffer = factory.wrap(ByteBuffer.wrap(data));
        Object dataBufferFlux = Flux.just(dataBuffer);
        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.post("/object-request-response", dataBufferFlux, new ParameterizedTypeReference<>(){});
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getResponseObject());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals(requestData, responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testPostWithMultipartData() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        TestRequest testRequest = new TestRequest(requestData);
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("request", testRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity =
                restClient.post("/multipart-request-response", bodyBuilder.build(), null, headers, new ParameterizedTypeReference<>(){});
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getResponseObject());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals(requestData, responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testPostOctetStream() throws Exception {
        final byte[] request = {1, 2};

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity =
                restClient.post("/octet-stream", request, null, headers, new ParameterizedTypeReference<>(){});

        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getResponseObject());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals("length: 2", responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    void testPostWithLargeServerResponse() {
        final Logger defaultRestClientLogger = (Logger) LoggerFactory.getLogger(DefaultRestClient.class);
        final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        defaultRestClientLogger.addAppender(listAppender);

        final RestClientException exception = assertThrows(RestClientException.class,
                () -> restClient.post("/object-response-large", null, new ParameterizedTypeReference<Response>() {
                }));

        final List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertEquals(1, logsList.stream().filter(
                logEvent -> logEvent.getMessage().equals("Error while retrieving large server response")).count());
        assertNotNull(exception.getMessage());
    }


    @Test
    void testDefaultHttpHeaders() throws RestClientException {
        String headerName = "Header-Name";
        String headerVaue = "value";
        HttpHeaders headers = new HttpHeaders();
        headers.set(headerName, headerVaue);

        RestClientConfiguration config = prepareConfiguration();
        config.setBaseUrl(publicBaseUrl);
        config.setDefaultHttpHeaders(headers);
        RestClient restClient = new DefaultRestClient(config);

        final ResponseEntity<ObjectResponse<TestResponse>> responseEntity =
                restClient.post("/request-headers-response", null, new ParameterizedTypeReference<>(){});
        assertTrue(responseEntity.getHeaders().containsKey(headerName));
        assertEquals(headerVaue, responseEntity.getHeaders().getFirst(headerName));
    }

    @Test
    void testConfiguration() throws RestClientException {
        final RestClientConfiguration config = prepareConfiguration();
        final DefaultRestClient restClient = new DefaultRestClient(config);

        final Object handshakeTimeout = getField(restClient, "webClient.builder.connector.httpClient.config.sslProvider.handshakeTimeoutMillis");
        assertEquals(5000L, handshakeTimeout);

        final Object responseTimeout = getField(restClient, "webClient.builder.connector.httpClient.config.responseTimeout");
        assertEquals(Duration.ofSeconds(10), responseTimeout);
    }

    @Test
    void testCustomKeyStoreTrustStoreBytes() throws Exception {
        RestClientConfiguration config = prepareConfiguration();
        config.setBaseUrl(publicBaseUrl);
        configureCustomKeyStore(config);
        configureCustomTrustStore(config);

        restClient = new DefaultRestClient(config);

        testGetWithResponse();
    }

    @Test
    void testCustomKeyStoreBytes() throws Exception {
        RestClientConfiguration config = prepareConfiguration();
        config.setBaseUrl(publicBaseUrl);
        configureCustomKeyStore(config);

        restClient = new DefaultRestClient(config);

        testGetWithResponse();
    }

    @Test
    void testCustomTrustStoreBytes() throws Exception {
        RestClientConfiguration config = prepareConfiguration();
        config.setBaseUrl(publicBaseUrl);
        configureCustomTrustStore(config);

        restClient = new DefaultRestClient(config);

        testGetWithResponse();
    }

    @Test
    void testRedirectShouldNotFollowByDefault() throws Exception {
        RestClientConfiguration config = prepareConfiguration();
        config.setBaseUrl(publicBaseUrl);
        assertFalse(config.isFollowRedirectEnabled(), "Following HTTP redirects should be disabled by default");

        restClient = new DefaultRestClient(config);

        final ResponseEntity<Response> responseEntity = restClient.get("/redirect-to-response", new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.FOUND, responseEntity.getStatusCode());
    }

    @Test
    void testRedirectShouldFollowWhenEnabled() throws Exception {
        RestClientConfiguration config = prepareConfiguration();
        config.setBaseUrl(publicBaseUrl);
        config.setFollowRedirectEnabled(true);

        restClient = new DefaultRestClient(config);

        final ResponseEntity<Response> responseEntity = restClient.get("/redirect-to-response", new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    void testGetWithResponseDigest() throws RestClientException {
        final RestClientConfiguration config = prepareConfiguration();
        config.setBaseUrl(privateBaseUrl);
        config.setHttpBasicAuthEnabled(false);
        config.setHttpDigestAuthEnabled(true);
        config.setHttpDigestAuthUsername("test-digest-user");
        config.setHttpDigestAuthPassword("top-secret");
        final RestClient restClient = new DefaultRestClient(config);

        final ResponseEntity<Response> responseEntity = restClient.get("/response", new ParameterizedTypeReference<>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    void testGetWithResponseDigestAuthFailed() throws RestClientException {
        final RestClientConfiguration config = prepareConfiguration();
        config.setBaseUrl(privateBaseUrl);
        final RestClient restClient = new DefaultRestClient(config);

        final RestClientException exception = assertThrows(RestClientException.class,
                () -> restClient.get("/response", new ParameterizedTypeReference<Response>() {}));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    private static Object getField(final Object parentBean, String path) {
        final String[] pathParts = path.split("\\.");
        final String fieldName = pathParts[0];
        final Object childBean = ReflectionTestUtils.getField(parentBean, fieldName);
        if (pathParts.length == 1) {
            return childBean;
        } else {
            return getField(childBean, path.replace(fieldName + ".", ""));
        }
    }

    private void configureCustomKeyStore(RestClientConfiguration config) throws Exception {
        File keyStoreFile = ResourceUtils.getFile("classpath:ssl/keystore-client.jks");
        byte[] keystoreBytes = new byte[(int) keyStoreFile.length()];
        try (final InputStream inputStream = new FileInputStream(keyStoreFile)) {
            inputStream.read(keystoreBytes);
        }

        config.setKeyStoreLocation(null);
        config.setKeyStoreBytes(keystoreBytes);
        config.setKeyStorePassword("changeit");
        config.setKeyAlias("client");
        config.setKeyPassword("changeit");
    }

    private void configureCustomTrustStore(RestClientConfiguration config) throws Exception {
        File trustStoreFile = ResourceUtils.getFile("classpath:ssl/truststore.jks");
        byte[] trustStoreBytes = new byte[(int) trustStoreFile.length()];
        try (final InputStream inputStream = new FileInputStream(trustStoreFile)) {
            inputStream.read(trustStoreBytes);
        }

        config.setTrustStoreLocation(null);
        config.setTrustStoreBytes(trustStoreBytes);
        config.setTrustStorePassword("changeit");
    }

}
