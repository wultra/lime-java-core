/*
 * Copyright 2024 Wultra s.r.o.
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

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * An ExchangeFilterFunction for adding traceparent header to WebClient requests.
 * It also propagates trace ID through Reactor context and MDC for logging purposes.
 *
 * @author Jan Dusil, jan.dusil@wultra.com
 */
public class TraceparentFilterFunction implements ExchangeFilterFunction {

    private static final String TRACEPARENT_HEADER_KEY = "traceparent";

    /**
     * Applies the filter to the given ClientRequest and ExchangeFunction.
     *
     * @param request The client request.
     * @param next    The next exchange function in the chain.
     * @return a Mono<ClientResponse> after applying the filter.
     */
    @Override
    public Mono<ClientResponse> filter(final ClientRequest request, final ExchangeFunction next) {
        return next.exchange(addTraceparentHeader(request)).contextWrite(
                context ->
                {
                    final Span currentSpan = Span.current();
                    if (currentSpan != null) {
                        final SpanContext spanContext = currentSpan.getSpanContext();
                        if (spanContext != null) {
                            final Context contextTmp = context.put("traceId", spanContext.getTraceId());
                            MDC.put("traceId", spanContext.getTraceId());
                            return contextTmp;
                        }
                    }
                    return context;
                }
        );
    }

    /**
     * Adds a traceparent header to the ClientRequest if a current span context is available.
     *
     * @param request The client request.
     * @return a modified ClientRequest with the traceparent header added.
     */
    private ClientRequest addTraceparentHeader(final ClientRequest request) {
        final Span currentSpan = Span.current();
        if (currentSpan != null) {
            final SpanContext spanContext = currentSpan.getSpanContext();
            if (spanContext != null) {
                final String traceId = spanContext.getTraceId();
                final String spanId = spanContext.getSpanId();
                final TraceFlags traceFlags = spanContext.getTraceFlags();
                if (traceId != null && spanId != null && traceFlags != null) {
                    final String headerValue = String.format("00-%s-%s-%s",
                            traceId,
                            spanId,
                            traceFlags.asHex());
                    return ClientRequest.from(request)
                            .headers(headers -> headers.set(TRACEPARENT_HEADER_KEY, headerValue))
                            .build();
                }
            }
        }
        return request;
    }

    /**
     * Factory method to create an instance of TraceparentFilterFunction.
     *
     * @return a new instance of TraceparentFilterFunction.
     */
    public static ExchangeFilterFunction handleTraceparentContext() {
        return new TraceparentFilterFunction();
    }
}
