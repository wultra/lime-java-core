/*
 * Copyright 2021 Wultra s.r.o.
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
package com.wultra.core.rest.client.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;

import java.util.UUID;

/**
 * Security configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
public class WebSecurityConfig {

    private static final String ENTRY_POINT_KEY = UUID.randomUUID().toString();

    private DigestAuthenticationEntryPoint authenticationEntryPoint() {
        final DigestAuthenticationEntryPoint entryPoint = new DigestAuthenticationEntryPoint();
        entryPoint.setRealmName("Test App Realm");
        entryPoint.setKey(ENTRY_POINT_KEY);
        return entryPoint;
    }

    private DigestAuthenticationFilter digestAuthenticationFilter() {
        final DigestAuthenticationFilter filter = new DigestAuthenticationFilter();
        filter.setUserDetailsService(userDetailsService());
        filter.setAuthenticationEntryPoint(authenticationEntryPoint());
        filter.setCreateAuthenticatedToken(true);
        return filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint()))
                .addFilter(digestAuthenticationFilter())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/private/**").authenticated()
                        .anyRequest().permitAll()
                ).build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> User.builder()
                .username("test-digest-user")
                .password("top-secret")
                .authorities("ROLE_USER")
                .build();
    }

}
