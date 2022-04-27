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
package com.wultra.core.rest.client.base.util;

import com.wultra.core.rest.client.base.RestClientConfiguration;
import com.wultra.core.rest.client.base.RestClientException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SSL certificate utilities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class SslUtils {

    /**
     * Prepare SSL context for a REST client configuration.
     * @param config REST client configuration.
     * @return SSL context.
     * @throws RestClientException Thrown in case SSL configuration is invalid.
     */
    public static SslContext prepareSslContext(RestClientConfiguration config) throws RestClientException {

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();

            // Accept invalid certificates
            if (config.isAcceptInvalidSslCertificate()) {
                return sslContextBuilder
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
            }

            // Configure client TLS certificate authentication
            if (config.isCertificateAuthEnabled()) {

                // Extract private key from keystore
                if (config.useCustomKeyStore()) {
                    if (config.getKeyStorePassword() == null) {
                        throw new RestClientException("Keystore password is not configured");
                    }
                    if (config.getKeyAlias() == null) {
                        throw new RestClientException("Keystore key alias is not configured");
                    }
                    if (config.getKeyPassword() == null) {
                        throw new RestClientException("Keystore key password is not configured");
                    }
                    final char[] keyStorePassword = config.getKeyStorePassword().toCharArray();
                    final String keyAlias = config.getKeyAlias();
                    final char[] keyPassword = config.getKeyPassword().toCharArray();
                    final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    if (config.getKeyStoreValue() == null) {
                        if (config.getKeyStoreLocation() == null) {
                            throw new RestClientException("Keystore location is not configured");
                        }
                        final File keyStoreFile = ResourceUtils.getFile(config.getKeyStoreLocation());
                        if (!keyStoreFile.exists() || !keyStoreFile.canRead()) {
                            throw new RestClientException("Keystore is not accessible: " + keyStoreFile.getAbsolutePath());
                        }
                        try (final FileInputStream fis = new FileInputStream(keyStoreFile)) {
                            keyStore.load(fis, keyStorePassword);
                        }
                    } else {
                        byte[] keyStoreBytes;
                        try {
                            keyStoreBytes = Base64.getDecoder().decode(config.getKeyStoreValue());
                        } catch (Exception e) {
                            throw new RestClientException("Keystore value is not readable", e);
                        }
                        keyStore.load(new ByteArrayInputStream(keyStoreBytes), keyStorePassword);
                        config.setKeyStoreValue(null);
                    }
                    final PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword);
                    final Certificate[] certChain = keyStore.getCertificateChain(keyAlias);
                    if (certChain == null) {
                        throw new RestClientException("Invalid or missing key with alias: " + config.getKeyAlias());
                    }
                    final X509Certificate[] x509CertificateChain = Arrays.stream(certChain)
                            .map(certificate -> (X509Certificate) certificate)
                            .collect(Collectors.toList())
                            .toArray(new X509Certificate[certChain.length]);
                    sslContextBuilder.keyManager(privateKey, config.getKeyStorePassword(), x509CertificateChain);
                }

                // Override default truststore
                if (config.useCustomTrustStore()) {
                    final File trustStoreFile = ResourceUtils.getFile(config.getTrustStoreLocation());
                    if (!trustStoreFile.exists() || !trustStoreFile.canRead()) {
                        throw new RestClientException("Truststore is not accessible: " + trustStoreFile);
                    }
                    if (config.getTrustStorePassword() == null) {
                        throw new RestClientException("Truststore password is not configured");
                    }
                    final char[] trustStorePassword = config.getTrustStorePassword().toCharArray();
                    final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    try (final FileInputStream fis = new FileInputStream(trustStoreFile)) {
                        trustStore.load(fis, trustStorePassword);
                    }
                    final List<KeyStoreException> keyStoreExceptions = new ArrayList<>();
                    final X509Certificate[] certificates = Collections.list(trustStore.aliases())
                            .stream()
                            .filter(t -> {
                                try {
                                    return trustStore.isCertificateEntry(t);
                                } catch (KeyStoreException ex) {
                                    keyStoreExceptions.add(ex);
                                    return false;
                                }
                            })
                            .map(t -> {
                                try {
                                    return (X509Certificate) trustStore.getCertificate(t);
                                } catch (KeyStoreException ex) {
                                    keyStoreExceptions.add(ex);
                                    return null;
                                }
                            }).toArray(X509Certificate[]::new);
                    if (!keyStoreExceptions.isEmpty()) {
                        throw new RestClientException("Invalid truststore: " + trustStoreFile.getAbsolutePath());
                    }
                    sslContextBuilder.trustManager(certificates);
                }

                return sslContextBuilder.build();
            }
        } catch (IOException | GeneralSecurityException ex) {
            throw new RestClientException("SSL configuration failed, error: " + ex.getMessage());
        }
        return null;
    }
}
