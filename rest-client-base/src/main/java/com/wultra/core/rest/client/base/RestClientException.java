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

import io.getlime.core.rest.model.base.response.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * REST client exception.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class RestClientException extends Exception {

    private HttpStatus statusCode;
    private String response;
    private HttpHeaders responseHeaders;
    private ErrorResponse errorResponse;

    /**
     * Constructor with message.
     * @param message Exception message.
     */
    public RestClientException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     * @param message Exception message.
     * @param cause Original cause.
     */
    public RestClientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with message and HTTP status code.
     * @param message Exception message.
     * @param statusCode HTTP status code.
     * @param response Raw response.
     * @param responseHeaders Response HTTP headers.
     */
    public RestClientException(String message, HttpStatus statusCode, String response, HttpHeaders responseHeaders) {
        super(message);
        this.statusCode = statusCode;
        this.response = response;
        this.responseHeaders = responseHeaders;
    }

    /**
     * Constructor with message, HTTP status code, and error response.
     * @param message Exception message.
     * @param statusCode HTTP status code.
     * @param response Raw response.
     * @param responseHeaders Response HTTP headers.
     * @param errorResponse Error response.
     */
    public RestClientException(String message, HttpStatus statusCode, String response, HttpHeaders responseHeaders, ErrorResponse errorResponse) {
        super(message);
        this.statusCode = statusCode;
        this.response = response;
        this.responseHeaders = responseHeaders;
        this.errorResponse = errorResponse;
    }

    /**
     * Get HTTP status code.
     * @return HTTP status code.
     */
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    /**
     * Get raw response.
     * @return Raw response.
     */
    public String getResponse() {
        return response;
    }

    /**
     * Get response HTTP headers.
     * @return Response HTTP headers.
     */
    public HttpHeaders getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Get the error response.
     * @return Error response.
     */
    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
