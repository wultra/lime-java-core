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
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * REST client tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultRestClientTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    // Timeout for synchronization of non-blocking calls using countdown latch
    private static final int SYNCHRONIZATION_TIMEOUT = 10000;

    @BeforeEach
    public void initRestClient() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        config.setBaseUrl("https://localhost:" + port + "/api/test");
        restClient = new DefaultRestClient(config);
    }

    private RestClientConfiguration prepareConfiguration() {
        RestClientConfiguration config = new RestClientConfiguration();
        config.setClientCertificateAuthenticationEnabled(true);
        config.setUseCustomKeyStore(true);
        config.setKeyStoreLocation("classpath:ssl/keystore-client.jks");
        config.setKeyStorePassword("changeit");
        config.setKeyAlias("client");
        config.setKeyPassword("changeit");
        config.setUseCustomTrustStore(true);
        config.setTrustStoreLocation("classpath:ssl/truststore.jks");
        config.setTrustStorePassword("changeit");
        config.setHttpBasicAuthEnabled(true);
        config.setHttpBasicAuthUsername("test");
        config.setHttpBasicAuthPassword("test");
        return config;
    }

    @Test
    public void testGetWithResponse() throws RestClientException {
        ResponseEntity<Response> responseEntity = restClient.get("/response", new ParameterizedTypeReference<Response>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    public void testGetWithResponseObject() throws RestClientException {
        Response response = restClient.getObject("/response");
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    public void testGetWithResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<Response>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.getNonBlocking("/response", new ParameterizedTypeReference<Response>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testGetWithTestResponse() throws RestClientException {
        ResponseEntity<TestResponse> responseEntity = restClient.get("/test-response", new ParameterizedTypeReference<TestResponse>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("test response", responseEntity.getBody().getResponse());
    }

    @Test
    public void testGetWithObjectResponse() throws RestClientException {
        ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.get("/object-response", new ParameterizedTypeReference<ObjectResponse<TestResponse>>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals("object response", responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    public void testGetWithObjectResponseObject() throws RestClientException {
        ObjectResponse<TestResponse> response = restClient.getObject("/object-response", TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals("object response", response.getResponseObject().getResponse());
    }

    @Test
    public void testPostWithResponse() throws RestClientException {
        ResponseEntity<Response> responseEntity = restClient.post("/response", null, new ParameterizedTypeReference<Response>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    public void testPostWithResponseObject() throws RestClientException {
        Response response = restClient.postObject("/response", null);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    public void testPostWithResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<Response>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.postNonBlocking("/response", null, new ParameterizedTypeReference<Response>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testPostWithTestResponse() throws RestClientException {
        ResponseEntity<TestResponse> responseEntity = restClient.post("/test-response", null, new ParameterizedTypeReference<TestResponse>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("test response", responseEntity.getBody().getResponse());
    }

    @Test
    public void testPostWithObjectResponse() throws RestClientException {
        ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.post("/object-response", null, new ParameterizedTypeReference<ObjectResponse<TestResponse>>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals("object response", responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    public void testPostWithObjectResponseObject() throws RestClientException {
        ObjectResponse<TestResponse> response = restClient.postObject("/object-response", null, TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals("object response", response.getResponseObject().getResponse());
    }

    @Test
    public void testPostWithObjectRequestResponse() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.post("/object-request-response", request, new ParameterizedTypeReference<ObjectResponse<TestResponse>>(){});
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getResponseObject());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals(requestData, responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    public void testPostWithObjectRequestResponseObject() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        ObjectResponse<TestResponse> response = restClient.postObject("/object-request-response", request, TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals(requestData, response.getResponseObject().getResponse());
    }

    @Test
    public void testPostWithObjectRequestResponseNonBlocking() throws RestClientException, InterruptedException {
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
        restClient.postNonBlocking("/object-request-response", request, new ParameterizedTypeReference<ObjectResponse<TestResponse>>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testPutWithResponse() throws RestClientException {
        ResponseEntity<Response> responseEntity = restClient.put("/response", null, new ParameterizedTypeReference<Response>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    public void testPutWithResponseObject() throws RestClientException {
        Response response = restClient.putObject("/response", null);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    public void testPutWithResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<Response>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.putNonBlocking("/response", null, new ParameterizedTypeReference<Response>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testPutWithTestResponse() throws RestClientException {
        ResponseEntity<TestResponse> responseEntity = restClient.put("/test-response", null, new ParameterizedTypeReference<TestResponse>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("test response", responseEntity.getBody().getResponse());
    }

    @Test
    public void testPutWithObjectResponse() throws RestClientException {
        ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.put("/object-response", null, new ParameterizedTypeReference<ObjectResponse<TestResponse>>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals("object response", responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    public void testPutWithObjectResponseObject() throws RestClientException {
        ObjectResponse<TestResponse> response = restClient.putObject("/object-response", null, TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals("object response", response.getResponseObject().getResponse());
    }

    @Test
    public void testPutWithObjectRequestResponse() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.put("/object-request-response", request, new ParameterizedTypeReference<ObjectResponse<TestResponse>>(){});
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getResponseObject());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals(requestData, responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    public void testPutWithObjectRequestResponseObject() throws RestClientException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        ObjectResponse<TestResponse> response = restClient.putObject("/object-request-response", request, TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals(requestData, response.getResponseObject().getResponse());
    }

    @Test
    public void testPutWithObjectRequestResponseNonBlocking() throws RestClientException, InterruptedException {
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
        restClient.putNonBlocking("/object-request-response", request, new ParameterizedTypeReference<ObjectResponse<TestResponse>>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testDeleteWithResponse() throws RestClientException {
        ResponseEntity<Response> responseEntity = restClient.delete("/response", new ParameterizedTypeReference<Response>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
    }

    @Test
    public void testDeleteWithResponseObject() throws RestClientException {
        Response response = restClient.deleteObject("/response");
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    public void testDeleteWithResponseNonBlocking() throws RestClientException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<ResponseEntity<Response>> onSuccess = responseEntity -> {
            assertNotNull(responseEntity);
            assertNotNull(responseEntity.getBody());
            assertEquals("OK", responseEntity.getBody().getStatus());
            countDownLatch.countDown();
        };
        Consumer<Throwable> onError = error -> Assertions.fail(error.getMessage());
        restClient.deleteNonBlocking("/response", new ParameterizedTypeReference<Response>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testDeleteWithTestResponse() throws RestClientException {
        ResponseEntity<TestResponse> responseEntity = restClient.delete("/test-response", new ParameterizedTypeReference<TestResponse>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("test response", responseEntity.getBody().getResponse());
    }

    @Test
    public void testDeleteWithObjectResponse() throws RestClientException {
        ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.delete("/object-response", new ParameterizedTypeReference<ObjectResponse<TestResponse>>() {});
        assertNotNull(responseEntity.getBody());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals("object response", responseEntity.getBody().getResponseObject().getResponse());
    }

    @Test
    public void testDeleteWithObjectResponseObject() throws RestClientException {
        ObjectResponse<TestResponse> response = restClient.deleteObject("/object-response", TestResponse.class);
        assertNotNull(response.getResponseObject());
        assertEquals("object response", response.getResponseObject().getResponse());
    }

    @Test
    public void testPostWithErrorResponse() {
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
    public void testPostWithErrorResponseObject() {
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
    public void testPostWithErrorResponseNonBlocking() throws RestClientException, InterruptedException {
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
        restClient.postNonBlocking("/error-response", null, new ParameterizedTypeReference<ObjectResponse<TestResponse>>(){}, onSuccess, onError);
        assertTrue(countDownLatch.await(SYNCHRONIZATION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testGetWithFullUrl() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        restClient = new DefaultRestClient(config);
        Response response = restClient.getObject("https://localhost:" + port + "/api/test/response");
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    public void testPostWithFullUrl() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        restClient = new DefaultRestClient(config);
        Response response = restClient.postObject("https://localhost:" + port + "/api/test/response", null);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    public void testPutWithFullUrl() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        restClient = new DefaultRestClient(config);
        Response response = restClient.putObject("https://localhost:" + port + "/api/test/response", null);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    public void testDeleteWithFullUrl() throws RestClientException {
        RestClientConfiguration config = prepareConfiguration();
        restClient = new DefaultRestClient(config);
        Response response = restClient.deleteObject("https://localhost:" + port + "/api/test/response");
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
    }

    @Test
    public void testPostWithDataBuffer() throws RestClientException, JsonProcessingException {
        String requestData = String.valueOf(System.currentTimeMillis());
        ObjectRequest<TestRequest> request = new ObjectRequest<>(new TestRequest(requestData));
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] data = objectMapper.writeValueAsBytes(request);
        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        DefaultDataBuffer dataBuffer = factory.wrap(ByteBuffer.wrap(data));
        Object dataBufferFlux = Flux.just(dataBuffer);
        ResponseEntity<ObjectResponse<TestResponse>> responseEntity = restClient.post("/object-request-response", dataBufferFlux, new ParameterizedTypeReference<ObjectResponse<TestResponse>>(){});
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getResponseObject());
        assertEquals("OK", responseEntity.getBody().getStatus());
        assertEquals(requestData, responseEntity.getBody().getResponseObject().getResponse());
    }

}
