/*
 * Copyright 2023 Wultra s.r.o.
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

import io.getlime.core.rest.model.base.response.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for tests.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@RestController
@RequestMapping("/private/api/test")
public class PrivateTestRestController {

    @GetMapping("/response")
    public Response testGetWithResponse() {
        return new Response();
    }
}
