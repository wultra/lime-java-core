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
package com.wultra.core.rest.client.base.controller;

import com.wultra.core.rest.client.base.model.TestRequest;
import com.wultra.core.rest.client.base.model.TestResponse;
import com.wultra.core.rest.client.base.model.error.RestException;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Rest controller for tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("/public/api/test")
public class PublicTestRestController {

    @GetMapping("/response")
    public Response testGetWithResponse() {
        return new Response();
    }

    @GetMapping("/test-response")
    public TestResponse testGetWithTestResponse() {
        return new TestResponse("test response");
    }

    @GetMapping("/object-response")
    public ObjectResponse<TestResponse> testGetWithObjectResponse() {
        TestResponse testResponse = new TestResponse("object response");
        return new ObjectResponse<>(testResponse);
    }

    @PostMapping("/response")
    public Response testPostWithResponse() {
        return new Response();
    }

    @PostMapping("/test-response")
    public TestResponse testPostWithTestResponse() {
        return new TestResponse("test response");
    }

    @PostMapping("/object-response")
    public ObjectResponse<TestResponse> testPostWithObjectResponse() {
        TestResponse testResponse = new TestResponse("object response");
        return new ObjectResponse<>(testResponse);
    }

    @PostMapping("/object-request-response")
    public ObjectResponse<TestResponse> testPostWithObjectRequestAndResponse(@RequestBody ObjectRequest<TestRequest> request) {
        TestResponse testResponse = new TestResponse(request.getRequestObject().getRequest());
        return new ObjectResponse<>(testResponse);
    }

    @PostMapping(value = "/multipart-request-response", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ObjectResponse<TestResponse> testPostWithMultipartRequestAndResponse(@RequestPart TestRequest request) {
        TestResponse testResponse = new TestResponse(request.getRequest());
        return new ObjectResponse<>(testResponse);
    }

    @PostMapping(value = "/octet-stream", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ObjectResponse<TestResponse> testPostOctetStream(@RequestBody byte[] request) {
        final TestResponse testResponse = new TestResponse("length: " + request.length);
        return new ObjectResponse<>(testResponse);
    }

    @PostMapping("/object-response-large")
    public ObjectResponse<TestResponse> testPostWithLargeServerResponse() {
        TestResponse testResponse = new TestResponse(Arrays.toString(new byte[10 * 1024 * 1024]));
        return new ObjectResponse<>(testResponse);
    }

    @PutMapping("/response")
    public Response testPutWithResponse() {
        return new Response();
    }

    @PutMapping("/test-response")
    public TestResponse testPutWithTestResponse() {
        return new TestResponse("test response");
    }

    @PutMapping("/object-response")
    public ObjectResponse<TestResponse> testPutWithObjectResponse() {
        TestResponse testResponse = new TestResponse("object response");
        return new ObjectResponse<>(testResponse);
    }

    @PutMapping("/object-request-response")
    public ObjectResponse<TestResponse> testPutWithObjectRequestAndResponse(@RequestBody ObjectRequest<TestRequest> request) {
        TestResponse testResponse = new TestResponse(request.getRequestObject().getRequest());
        return new ObjectResponse<>(testResponse);
    }

    @PatchMapping("/response")
    public Response testPatchWithResponse() {
        return new Response();
    }

    @PatchMapping("/test-response")
    public TestResponse testPatchWithTestResponse() {
        return new TestResponse("test response");
    }

    @PatchMapping("/object-response")
    public ObjectResponse<TestResponse> testPatchWithObjectResponse() {
        TestResponse testResponse = new TestResponse("object response");
        return new ObjectResponse<>(testResponse);
    }

    @PatchMapping("/object-request-response")
    public ObjectResponse<TestResponse> testPatchWithObjectRequestAndResponse(@RequestBody ObjectRequest<TestRequest> request) {
        TestResponse testResponse = new TestResponse(request.getRequestObject().getRequest());
        return new ObjectResponse<>(testResponse);
    }

    @DeleteMapping("/response")
    public Response testDeleteWithResponse() {
        return new Response();
    }

    @DeleteMapping("/test-response")
    public TestResponse testDeleteWithTestResponse() {
        return new TestResponse("test response");
    }

    @DeleteMapping("/object-response")
    public ObjectResponse<TestResponse> testDeleteWithObjectResponse() {
        TestResponse testResponse = new TestResponse("object response");
        return new ObjectResponse<>(testResponse);
    }

    @PostMapping("/error-response")
    public ObjectResponse<TestResponse> testErrorResponse() throws RestException {
        throw new RestException();
    }

    @RequestMapping(value = "/request-headers-response", method = { RequestMethod.DELETE, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT })
    public Response testRequestHeadersResponse(HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> headerNamesIterator = request.getHeaderNames();
        while (headerNamesIterator.hasMoreElements()) {
            String headerName = headerNamesIterator.nextElement();
            response.setHeader(headerName, request.getHeader(headerName));
        }
        return new Response();
    }

    @GetMapping("/redirect-to-response")
    public ResponseEntity<Void> testRedirect() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/public/api/test/response"))
                .build();
    }

}
