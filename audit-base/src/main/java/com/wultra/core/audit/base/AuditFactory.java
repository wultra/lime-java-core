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
package com.wultra.core.audit.base;

import com.wultra.core.audit.base.configuration.AuditConfiguration;
import com.wultra.core.audit.base.database.DatabaseAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for auditing services.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class AuditFactory {

    private final AuditConfiguration configuration;
    private final Audit databaseAudit;

    /**
     * Audit factory constructor.
     * @param configuration Audit configuration.
     * @param databaseAudit Database audit.
     */
    @Autowired
    public AuditFactory(AuditConfiguration configuration, DatabaseAudit databaseAudit) {
        this.configuration = configuration;
        this.databaseAudit = databaseAudit;
    }

    /**
     * Get audit interface.
     * @return Audit interface.
     */
    public Audit getAudit() {
        return switch (configuration.getStorageType()) {
            case DATABASE -> databaseAudit;
        };
    }

}
