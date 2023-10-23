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
package io.getlime.core.rest.model.base.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple class representing a request with an object.
 *
 * @author Petr Dvorak, petr@wultra.com
 *
 * @param <T> Type of the request object.
 */
@ToString
@EqualsAndHashCode
public class ObjectRequest<T> {

    @Valid
    @NotNull
    private T requestObject;

    /**
     * Default constructor.
     */
    public ObjectRequest() {
    }

    /**
     * Constructor with a given request object.
     * @param requestObject Request object.
     */
    public ObjectRequest(T requestObject) {
        this.requestObject = requestObject;
    }

    /**
     * Get request object.
     * @return Request object.
     */
    public T getRequestObject() {
        return requestObject;
    }

    /**
     * Set request object.
     * @param requestObject Request object.
     */
    public void setRequestObject(T requestObject) {
        this.requestObject = requestObject;
    }

}
