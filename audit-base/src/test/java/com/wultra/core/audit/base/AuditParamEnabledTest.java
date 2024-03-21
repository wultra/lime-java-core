/*
 * Copyright 2022 Wultra s.r.o.
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

import com.wultra.core.audit.base.model.AuditDetail;
import com.wultra.core.audit.base.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.jdbc.Sql;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, properties = {
        "audit.db.table.param.enabled=true",
        "audit.db.cleanup.days=-1", // time shift to the future to enable cleanup test
        "powerauth.audit.cleanup.delay.initial=60000" // delay the job start due to slow builds
})
@Sql(scripts = "/db_schema.sql")
class AuditParamEnabledTest {

    private final AuditFactory auditFactory;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuditParamEnabledTest(AuditFactory auditFactory, JdbcTemplate jdbcTemplate) {
        this.auditFactory = auditFactory;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    public void cleanTestDb() {
        jdbcTemplate.execute("DELETE FROM audit_log");
        jdbcTemplate.execute("DELETE FROM audit_param");
    }

    @Test
    void testAuditOneParam() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.info("test message", AuditDetail.builder().param("my_id", "test_id").build());
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id=ap.audit_log_id");
        assertTrue(rs.next());
        assertNotNull(rs.getString("audit_log_id"));
        assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
        assertNull(rs.getString("audit_type"));
        assertEquals("my_id", rs.getString("param_key"));
        assertEquals("test_id", rs.getString("param_value"));
    }

    @Test
    void testAuditTypeAndTwoParams() {
        Audit audit = auditFactory.getAudit();
        String operationId = UUID.randomUUID().toString();
        AuditDetail detail = AuditDetail.builder()
                .type("TEST")
                .param("user_id", "test_id")
                .param("operation_id", operationId)
                .build();
        audit.info("test message", detail);
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'user_id' AND ap.param_value = 'test_id'");
        assertTrue(rs.next());
        assertNotNull(rs.getString("audit_log_id"));
        assertEquals("TEST", rs.getString("audit_type"));
        assertEquals("user_id", rs.getString("param_key"));
        assertEquals("test_id", rs.getString("param_value"));
        final SqlRowSet rs1 = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'operation_id'");
        assertTrue(rs1.next());
        assertNotNull(rs1.getString("audit_log_id"));
        assertEquals("operation_id", rs1.getString("param_key"));
        assertEquals(operationId, rs1.getString("param_value"));
    }

    @Test
    void testAuditMoreParams() {
        Audit audit = auditFactory.getAudit();
        String operationId = UUID.randomUUID().toString();
        Date timestamp = new Date();
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("user_id", "test_id");
        param.put("operation_id", operationId);
        param.put("session_id", "1A532637239A03B07199A54E8D531427");
        param.put("timestamp", timestamp);
        audit.info("test message", AuditDetail.builder().params(param).build());
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'user_id' AND ap.param_value = 'test_id'");
        assertTrue(rs.next());
        assertNotNull(rs.getString("audit_log_id"));
        assertEquals("user_id", rs.getString("param_key"));
        assertEquals("test_id", rs.getString("param_value"));
        final SqlRowSet rs1 = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'operation_id'");
        assertTrue(rs1.next());
        assertNotNull(rs1.getString("audit_log_id"));
        assertEquals(operationId, rs1.getString("param_value"));
        final SqlRowSet rs2 = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'session_id'");
        assertTrue(rs2.next());
        assertNotNull(rs2.getString("audit_log_id"));
        assertEquals("1A532637239A03B07199A54E8D531427", rs2.getString("param_value"));
        final SqlRowSet rs3 = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'timestamp'");
        assertTrue(rs3.next());
        assertNotNull(rs3.getString("audit_log_id"));
        assertEquals(new JsonUtil().serializeObject(timestamp), rs3.getString("param_value"));
    }

    @Test
    void testAuditCleanup() {
        final Audit audit = auditFactory.getAudit();
        audit.info("test message", AuditDetail.builder().param("my_id", "test_id").build());
        audit.flush();

        assertEquals(1, countAuditLogs());
        assertEquals(1, countAuditParams());

        audit.cleanup();

        assertEquals(0, countAuditLogs());
        assertEquals(0, countAuditParams());
    }

    private int countAuditLogs() {
        return count("audit_log");
    }

    private int countAuditParams() {
        return count("audit_param");
    }

    private int count(final String tableName) {
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT COUNT(*) FROM " + tableName);
        assertTrue(rs.next());
        return rs.getInt(1);
    }

}