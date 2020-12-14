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
package com.wultra.core.rest.client.base.errorhandling;

import com.wultra.core.rest.client.base.model.error.RestException;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception handler.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@ControllerAdvice
public class RestControllerAdvice {

    /**
     * Handle REST exception.
     * @param ex REST exception.
     * @return Error response.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RestException.class)
    public @ResponseBody ErrorResponse handleRestException(RestException ex) {
        return new ErrorResponse("TEST_CODE", "Test message");
    }

}
