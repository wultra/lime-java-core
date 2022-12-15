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

/**
 * Configuration of auditing with default values.
 */
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

    @Value("${audit.db.table.log.name:audit_log}")
    private String dbTableNameAudit;

    @Value("${audit.db.table.param.enabled:false}")
    private boolean dbTableParamLoggingEnabled;

    @Value("${audit.db.table.param.name:audit_param}")
    private String dbTableNameParam;

    @Value("${spring.jpa.properties.hibernate.default_schema:}")
    private String dbDefaultSchema;

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

    /**
     * Get application name.
     * @return Application name.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Get version.
     * @return Version.
     */
    public String getVersion() {
        if (buildProperties == null) {
            return null;
        }
        return buildProperties.getVersion();
    }

    /**
     * Get build time.
     * @return Build time.
     */
    public Instant getBuildTime() {
        if (buildProperties == null) {
            return null;
        }
        return buildProperties.getTime();
    }

    /**
     * Get minimum audit level.
     * @return Minimum audit level.
     */
    public AuditLevel getMinimumLevel() {
        return minimumLevel;
    }

    /**
     * Get event queue size.
     * @return Event queue size.
     */
    public int getEventQueueSize() {
        return eventQueueSize;
    }

    /**
     * Get storage type.
     * @return Storage type.
     */
    public AuditStorageType getStorageType() {
        return storageType;
    }

    /**
     * Get database insert batch size.
     * @return Database insert batch size.
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Get number of days for retention of audit records in database.
     * @return Number of days for retention of audit records in database.
     */
    public Integer getDbCleanupDays() {
        return dbCleanupDays;
    }

    /**
     * Get database table name for audit log.
     * @return Database table name for audit log.
     */
    public String getDbTableNameAudit() {
        return dbTableNameAudit;
    }

    /**
     * Get default database schema.
     * @return Default database schema.
     */
    public String getDbDefaultSchema() {
        return dbDefaultSchema;
    }

    /**
     * Get database table name for audit parameters.
     * @return Database table name for audit parameters.
     */
    public String getDbTableNameParam() {
        return dbTableNameParam;
    }

    /**
     * Get enabled flag for detail logging to database table.
     * @return Flag for detail logging into audit database.
     */
    public boolean isDbTableParamLoggingEnabled() {
        return dbTableParamLoggingEnabled;
    }

}
