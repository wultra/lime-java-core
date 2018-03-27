/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Class representing an error response.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class ErrorResponse extends ObjectResponse<Error> {

    /**
     * Default constructor.
     */
    public ErrorResponse() {
        super(Status.ERROR, new Error());
    }

    /**
     * Create a new error response with response object with provided code
     * and error message.
     *
     * @param code Error code.
     * @param message Error message.
     */
    public ErrorResponse(String code, String message) {
        super(Status.ERROR, new Error(code, message));
    }

    /**
     * Create a new error response with response object with provided code
     * and throwable, that is a source for the error message (t.getMessage()).
     *
     * @param code Error code.
     * @param t Throwable, whose message is used as an error message.
     */
    public ErrorResponse(String code, Throwable t) {
        super(Status.ERROR, new Error(code, t != null ? t.getMessage() : null));
    }

    /**
     * Create an error response with provided error as a response object.
     *
     * @param error Error response object.
     */
    public ErrorResponse(Error error) {
        super(Status.ERROR, error);
    }

}
