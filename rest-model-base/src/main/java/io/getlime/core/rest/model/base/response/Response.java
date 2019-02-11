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

/**
 * Simple status only response object.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class Response {

    /**
     * Response status string.
     */
    public class Status {

        /**
         * In case response was OK.
         */
        public static final String OK = "OK";

        /**
         * In case an error response is sent.
         */
        public static final String ERROR = "ERROR";

    }

    protected String status;

    /**
     * Default constructor.
     */
    public Response() {
        this.status = Status.OK;
    }

    /**
     * Constructor with response status and response object
     *
     * @param status         Response status, use static constant from {@link Status} class.
     */
    public Response(String status) {
        this.status = status;
    }

    /**
     * Get response status.
     * @return Response status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set response status.
     * @param status Response status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

}
