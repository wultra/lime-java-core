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
package com.wultra.core.rest.client.base;

import me.vzhilin.auth.DigestAuthenticator;
import me.vzhilin.auth.parser.ChallengeResponse;
import me.vzhilin.auth.parser.ChallengeResponseParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.text.ParseException;

/**
 * Specialization of {@link ExchangeFilterFunction} to support Digest Authentication according to
 * <a href="https://datatracker.ietf.org/doc/html/rfc7616">RFC 7616</a>
 * using {@code io.github.vzhn:netty-http-authenticator} library.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class DigestAuthenticationFilterFunction implements ExchangeFilterFunction {

    private final DigestAuthenticator digestAuthenticator;

    DigestAuthenticationFilterFunction(DigestAuthenticator digestAuthenticator) {
        this.digestAuthenticator = digestAuthenticator;
    }

    @Override
    public Mono<ClientResponse> filter(final ClientRequest request, final ExchangeFunction next) {
        return next.exchange(addAuthorizationHeader(request))
                .flatMap(response -> {
                    if (isUnauthorized(response)) {
                        updateAuthenticator(request, response);
                        return next.exchange(addAuthorizationHeader(request))
                                .doOnNext(it -> {
                                    if (isUnauthorized(it)) {
                                        updateAuthenticator(request, it);
                                    }
                                });
                    } else {
                        return Mono.just(response);
                    }
                });
    }

    private static boolean isUnauthorized(ClientResponse response) {
        return response.statusCode() == HttpStatus.UNAUTHORIZED;
    }

    private ClientRequest addAuthorizationHeader(final ClientRequest request) {
        final String authorization = digestAuthenticator.authorizationHeader(request.method().name(), request.url().getPath());
        if (authorization != null) {
            return ClientRequest.from(request)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .build();
        } else {
            return request;
        }
    }

    private void updateAuthenticator(final ClientRequest request, final ClientResponse response) {
        final HttpHeaders headers = response.headers().asHttpHeaders();
        final String authenticateHeader = headers.getFirst(HttpHeaders.WWW_AUTHENTICATE);
        if (authenticateHeader != null) {
            try {
                final ChallengeResponse challenge = new ChallengeResponseParser(authenticateHeader).parseChallenge();
                digestAuthenticator.onResponseReceived(challenge, HttpStatus.UNAUTHORIZED.value());
            } catch (ParseException e) {
                throw new WebClientRequestException(e, request.method(), request.url(), headers);
            }
        }
    }

    /**
     * Return a filter that applies HTTP Digest Authentication.
     *
     * @param username – the username
     * @param password – the password
     * @return the filter
     */
    public static ExchangeFilterFunction digestAuthentication(final String username, final String password) {
        return new DigestAuthenticationFilterFunction(new DigestAuthenticator(username, password));
    }
}
