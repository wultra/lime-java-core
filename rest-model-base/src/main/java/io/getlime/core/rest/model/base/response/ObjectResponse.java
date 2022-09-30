/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.core.rest.model.base.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Generic response with status and object of a custom class.
 *
 * @author Petr Dvorak, petr@wultra.com
 *
 * @param <T> Type of the response object
 */
public class ObjectResponse<T> extends Response {

    @Valid
    @NotNull
    private T responseObject;

    /**
     * Default constructor
     */
    public ObjectResponse() {
        this.status = Status.OK;
    }

    /**
     * Constructor with OK response status and response object
     *
     * @param responseObject Response object.
     */
    public ObjectResponse(T responseObject) {
        this.status = Status.OK;
        this.responseObject = responseObject;
    }

    /**
     * Constructor with response status and response object
     *
     * @param status         Response status, use static constant from {@link io.getlime.core.rest.model.base.response.Response.Status} class.
     * @param responseObject Response object.
     */
    public ObjectResponse(String status, T responseObject) {
        this.status = status;
        this.responseObject = responseObject;
    }

    /**
     * Get response status.
     *
     * @return Response status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set response status.
     *
     * @param status Response status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get response object.
     *
     * @return Response object.
     */
    public T getResponseObject() {
        return responseObject;
    }

    /**
     * Set response object.
     *
     * @param responseObject Response object.
     */
    public void setResponseObject(T responseObject) {
        this.responseObject = responseObject;
    }

}
