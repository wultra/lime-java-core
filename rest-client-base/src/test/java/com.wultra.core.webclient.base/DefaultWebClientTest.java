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
package com.wultra.core.webclient.base;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertTrue;

/**
 * REST client tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class DefaultWebClientTest {

    @Test
    public void testSimpleGet() throws RestClientException {
        DefaultRestClient webClient = new DefaultRestClient("https://www.wultra.com");
        ResponseEntity<String> response = webClient.get("/", new ParameterizedTypeReference<String>() {});
        assertTrue(response.toString().contains("<title>Secure Digital Finance - Authentication, Compliance, Mobile Security</title>"));
    }
}
