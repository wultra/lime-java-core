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
package com.wultra.core.audit.base.configuration;

import com.wultra.core.audit.base.model.AuditLevel;
import com.wultra.core.audit.base.model.AuditStorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
@ConfigurationProperties("ext")
public class AuditConfiguration {

    private BuildProperties buildProperties;

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Value("${audit.level:INFO}")
    private AuditLevel minimumLevel;

    @Value("${audit.event.queue.size:100000}")
    private int eventQueueSize;

    @Value("${audit.storage.type:DATABASE}")
    private AuditStorageType storageType;

    @Value("${audit.db.cleanup.days:365}")
    private Integer dbCleanupDays;

    @Value("${audit.db.table.name:audit_log}")
    private String dbTableNameAudit;

    @Value("${audit.db.table.name:audit_param}")
    private String dbTableNameParam;

    @Value("${audit.db.batch.size:1000}")
    private int batchSize;

    /**
     * Set build information.
     * @param buildProperties Build properties.
     */
    @Autowired(required = false)
    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getVersion() {
        if (buildProperties == null) {
            return null;
        }
        return buildProperties.getVersion();
    }

    public Instant getBuildTime() {
        if (buildProperties == null) {
            return null;
        }
        return buildProperties.getTime();
    }

    public AuditLevel getMinimumLevel() {
        return minimumLevel;
    }

    public int getEventQueueSize() {
        return eventQueueSize;
    }

    public AuditStorageType getStorageType() {
        return storageType;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public Integer getDbCleanupDays() {
        return dbCleanupDays;
    }

    public String getDbTableNameAudit() {
        return dbTableNameAudit;
    }

    public String getDbTableNameParam() {
        return dbTableNameParam;
    }
}
