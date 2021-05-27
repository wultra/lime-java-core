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
package com.wultra.core.audit.base.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Detailed audit information.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuditDetail {

    private String type;
    private final Map<String, Object> param = new LinkedHashMap<>();

    public AuditDetail() {
    }

    public AuditDetail(String type) {
        this.type = type;
    }

    public AuditDetail(String type, Map<String, Object> param) {
        this.type = type;
        this.param.putAll(param);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final AuditDetail auditDetail = new AuditDetail();

        public Builder type(String type) {
            auditDetail.setType(type);
            return this;
        }

        public Builder param(String key, Object value) {
            auditDetail.getParam().put(key, value);
            return this;
        }

        public Builder params(Map<String, Object> params) {
            auditDetail.getParam().putAll(params);
            return this;
        }

        public AuditDetail build() {
            return auditDetail;
        }

    }
}
